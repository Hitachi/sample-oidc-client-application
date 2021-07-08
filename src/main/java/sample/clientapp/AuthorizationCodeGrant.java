package sample.clientapp;

import java.net.URI;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import sample.config.ClientAppConfiguration;
import sample.jwt.IdToken;
import sample.jwt.TokenResponse;
import sample.util.OauthUtil;

public class AuthorizationCodeGrant {
	private HttpSession session;
	private ClientAppConfiguration clientConfig;
	private RestTemplate restTemplate;

	public AuthorizationCodeGrant(HttpSession session, ClientAppConfiguration clientConfig, RestTemplate restTemplate) {
		this.session = session;
		this.clientConfig = clientConfig;
		this.restTemplate = restTemplate;
	}

	public String processAuthorizationCodeGrant(String code, String state, Model model) {
		if (state == null || !state.equals(session.getAttribute("state"))) {
			return "gettoken";
		}

		TokenResponse token = requestToken(code);
		if (token == null) {
			return "gettoken";
		}

		IdToken idToken = OauthUtil.readJsonContent(OauthUtil.decodeFromBase64Url(token.getIdToken()), IdToken.class);
		if (idToken.getNonce() == null || !idToken.getNonce().equals(session.getAttribute("nonce"))) {
			return "gettoken";
		}

		model.addAttribute("accessToken", token.getAccessToken());
		session.setAttribute("accessToken", token.getAccessToken());

		return "gettoken";
	}

	private TokenResponse requestToken(String authorizationCode) {
		StringBuilder tokenRequestUrl = new StringBuilder();
		tokenRequestUrl.append(clientConfig.getAuthserverUrl()).append(clientConfig.getTokenEndpoint());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("code", authorizationCode);
		params.add("client_id", clientConfig.getClientId());
		params.add("client_secret", clientConfig.getClientSecret());
		params.add("grant_type", "authorization_code");
		params.add("redirect_uri", clientConfig.getClientappUrl() + "/gettoken");

		params.add("code_verifier", (String) session.getAttribute("codeVerifier"));

		RequestEntity<?> req = new RequestEntity<>(params, headers, HttpMethod.POST,
				URI.create(tokenRequestUrl.toString()));
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
}
