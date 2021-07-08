package sample.clientapp;

import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import sample.config.ClientAppConfiguration;
import sample.config.OauthConfiguration;
import sample.config.SslConfiguration;
import sample.jwt.TokenResponse;
import sample.jwt.WellKnownInfo;

@Controller
public class ClientAppController {

    @Autowired
    HttpSession session;

    @Autowired
    ClientAppConfiguration clientConfig;

    @Autowired
    OauthConfiguration oauthConfig;

    @Autowired
    SslConfiguration sslConfig;

    @Autowired
    RestTemplate restTemplate;

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

    	WellKnownGetter wellKnownGetter = new WellKnownGetter(clientConfig, restTemplate);
    	WellKnownInfo wellKnownInfo = wellKnownGetter.getWellKnown();
    	AuthorizationRequest authorizationRequest = new AuthorizationRequest(session, clientConfig, sslConfig, wellKnownInfo);
        model.addAttribute("authorizationUrl", authorizationRequest.getAuthorizationUrl());

        String accessToken = (String) session.getAttribute("accessToken");
        String tokenStatus = "null";
        if (accessToken != null) {
            tokenStatus = accessToken.substring(0, 20) + "...";
        }
        model.addAttribute("tokenStatus", tokenStatus);

        return "index";
    }

    @RequestMapping(value = "/gettoken", method = RequestMethod.POST)
    public String getTokenFormPost(@RequestParam("session_state") String session_state, @RequestParam("code") String code,
        @RequestParam(name = "state", required = false) String state, Model model,
        @ModelAttribute("tokenData") TokenResponse sessionData) {

        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(session, clientConfig, restTemplate);
        return authorizationCodeGrant.processAuthorizationCodeGrant(code, state, model);
    }

    @RequestMapping(value = "/jwks", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getJwks(TokenResponse sessionData) {
    	JwksEndpoint jwksEndpoint = new JwksEndpoint(sslConfig);
    	ResponseEntity<Map<String, Object>> jwks = jwksEndpoint.returnResponse();
        return jwks;
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