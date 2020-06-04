package sample.clientapp;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Controller
public class ClientAppController {

    @Autowired
    HttpSession session;

    @Autowired
    ClientAppConfiguration clientConfig;

    @Autowired
    OauthConfiguration oauthConfig;

    @Autowired
    RestTemplate restTemplate;

    private static final String AuthorizationEndpoint = "/protocol/openid-connect/auth";
    private static final String TokenEndpoint = "/protocol/openid-connect/token";

    private String getAuthorizationUrl() {
        StringBuilder authorizationUrl = new StringBuilder();
        authorizationUrl.append(clientConfig.getKeycloakUrl()).append("/auth/realms/").append(clientConfig.getRealm())
            .append(AuthorizationEndpoint);

        String redirectUrl;
        String scope;
        try {
            redirectUrl = URLEncoder.encode(clientConfig.getClientappUrl() + "/gettoken", "UTF-8");
            scope = URLEncoder.encode(clientConfig.getScope(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }

        authorizationUrl.append("?response_type=code").append("&client_id=").append(clientConfig.getClientId())
            .append("&redirect_uri=").append(redirectUrl).append("&scope=").append(scope);

        if (oauthConfig.isState()) {
            String state = UUID.randomUUID().toString();
            session.setAttribute("state", state);
            authorizationUrl.append("&state=").append(state);
        }

        if (oauthConfig.isNonce()) {
            String nonce = UUID.randomUUID().toString();
            session.setAttribute("nonce", nonce);
            authorizationUrl.append("&nonce=").append(nonce);
        }

        if (oauthConfig.isPkce()) {
            String codeVerifier = OauthUtil.generateCodeVerifier();
            session.setAttribute("codeVerifier", codeVerifier);
            String codeChallenge = OauthUtil.generateCodeChallenge(codeVerifier);
            authorizationUrl.append("&code_challenge_method=S256&code_challenge=").append(codeChallenge);
        }

        if (oauthConfig.isFormPost()) {
            authorizationUrl.append("&response_mode=form_post");
        }

        return authorizationUrl.toString();
    }

    private TokenResponse requestToken(String authorizationCode) {
        StringBuilder tokenRequestUrl = new StringBuilder();
        tokenRequestUrl.append(clientConfig.getKeycloakUrl()).append("/auth/realms/").append(clientConfig.getRealm()).append(TokenEndpoint);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("code", authorizationCode);
        params.add("client_id", clientConfig.getClientId());
        params.add("client_secret", clientConfig.getClientSecret());
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", clientConfig.getClientappUrl() + "/gettoken");

        if (oauthConfig.isPkce()) {
            params.add("code_verifier", (String) session.getAttribute("codeVerifier"));
        }

        RequestEntity<?> req = new RequestEntity<>(params, headers, HttpMethod.POST, URI.create(tokenRequestUrl.toString()));
        TokenResponse token = null;
        try {
            ResponseEntity<TokenResponse> res = restTemplate.exchange(req, TokenResponse.class);
            token = res.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("!! response code=" + e.getStatusCode());
            System.out.println(e.getResponseBodyAsString());
        }

        return token;
    }

    private String callApi(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null) {
            headers.setBearerAuth(accessToken);
        }

        RequestEntity<?> req = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        String response = null;
        try {
            ResponseEntity<String> res = restTemplate.exchange(req, String.class);
            response = res.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("!! response code=" + e.getStatusCode());
            System.out.println(e.getResponseBodyAsString());
            response = e.getStatusCode().toString();
        }

        return response;
    }

    @RequestMapping("/")
    public String index(Model model, @ModelAttribute("tokenData") TokenResponse sessionData) {

        model.addAttribute("authorizationUrl", getAuthorizationUrl());

        String accessToken = (String) session.getAttribute("accessToken");
        String tokenStatus = "null";
        if (accessToken != null) {
            tokenStatus = accessToken.substring(0, 20) + "...";
        }
        model.addAttribute("tokenStatus", tokenStatus);

        return "index";
    }

    @RequestMapping(value = "/gettoken", method = RequestMethod.GET)
    public String getToken(@RequestParam("session_state") String session_state, @RequestParam("code") String code,
        @RequestParam(name = "state", required = false) String state, Model model,
        @ModelAttribute("tokenData") TokenResponse sessionData) {

        if (oauthConfig.isFormPost()) {
            return "gettoken";
        }

        return processAuthorizationCodeGrant(code, state, model);
    }

    @RequestMapping(value = "/gettoken", method = RequestMethod.POST)
    public String getTokenFormPost(@RequestParam("session_state") String session_state, @RequestParam("code") String code,
        @RequestParam(name = "state", required = false) String state, Model model,
        @ModelAttribute("tokenData") TokenResponse sessionData) {

        if (!oauthConfig.isFormPost()) {
            return "gettoken";
        }

        return processAuthorizationCodeGrant(code, state, model);
    }

    private String processAuthorizationCodeGrant(String code, String state, Model model) {
        if (oauthConfig.isState()) {
            if (state == null || !state.equals(session.getAttribute("state"))) {
                return "gettoken";
            }
        }

        TokenResponse token = requestToken(code);
        if (token == null) {
            return "gettoken";
        }

        if (oauthConfig.isNonce()) {
            IdToken idToken = OauthUtil.readJsonContent(OauthUtil.decodeFromBase64Url(token.getIdToken()), IdToken.class);
            if (idToken.getNonce() == null || !idToken.getNonce().equals(session.getAttribute("nonce"))) {
                return "gettoken";
            }
        }

        model.addAttribute("accessToken", token.getAccessToken());
        session.setAttribute("accessToken", token.getAccessToken());

        return "gettoken";
    }

    @RequestMapping("/callecho")
    public String callEcho(Model model) {
        String accessToken = (String) session.getAttribute("accessToken");
        String uri = clientConfig.getApiserverUrl() + "/echo";
        String response = callApi(uri, accessToken);
        model.addAttribute("result", response);
        return "forward:/";
    }
}