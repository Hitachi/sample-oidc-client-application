package sample.clientapp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonProcessingException;

import sample.config.ClientAppConfiguration;
import sample.config.SslConfiguration;
import sample.crypto.JWSBuilder;
import sample.crypto.KeyWrapper;
import sample.jwt.RequestObject;
import sample.jwt.WellKnownInfo;
import sample.util.KeyUtils;
import sample.util.OauthUtil;

public class AuthorizationRequest {

	private HttpSession session;
	private ClientAppConfiguration clientConfig;
	private SslConfiguration sslConfig;
	private WellKnownInfo wellKnownInfo;

	public AuthorizationRequest(HttpSession session, ClientAppConfiguration clientConfig, SslConfiguration sslConfig, WellKnownInfo wellKnownInfo) {
		this.session = session;
		this.clientConfig = clientConfig;
		this.sslConfig = sslConfig;
		this.wellKnownInfo = wellKnownInfo;
	}

	public String getAuthorizationUrl() {
		StringBuilder authorizationUrl = new StringBuilder();
		authorizationUrl.append(wellKnownInfo.getAuthorizationEndpoint());

		String scope;
		String requestObject;
		try {
			scope = URLEncoder.encode(clientConfig.getScope(), "UTF-8");
			requestObject = createRequestObject();
		} catch (JsonProcessingException | SignatureException | UnsupportedEncodingException e) {
			return "";
		}

		authorizationUrl.append("?response_type=code+id_token").append("&client_id=").append(clientConfig.getClientId())
				.append("&request=").append(requestObject).append("&scope=").append(scope);


		return authorizationUrl.toString();
	}

    private String createRequestObject() throws JsonProcessingException, SignatureException {
    	String keyId = sslConfig.getKeyAlias();
    	KeyUtils keyUtils = new KeyUtils(sslConfig);
        KeyWrapper keyWrapper = new KeyWrapper();
        keyWrapper.setAlgorithm("PS256");
        keyWrapper.setKid(keyId);
        keyWrapper.setPrivateKey(keyUtils.getPrivateKey());
        AsymmetricSignatureSignerContext signer = new AsymmetricSignatureSignerContext(keyWrapper);

        RequestObject json = createPayload();
    	
    	JWSBuilder jws = new JWSBuilder();
    	String jwt = jws.jsonContent(json).sign(signer);
    	return jwt;
    }

    private RequestObject createPayload() throws JsonProcessingException {
    	RequestObject requestObject = new RequestObject();
    	requestObject.setResponseType(clientConfig.getResponseType());
    	requestObject.setResponseMode("form_post");
    	requestObject.setClientId(clientConfig.getClientId());
    	requestObject.setRedirectUri(clientConfig.getRedirectUrl());

	    String state = UUID.randomUUID().toString();
        session.setAttribute("state", state);	    
    	requestObject.setState(state);
	    
        String nonce = UUID.randomUUID().toString();
        session.setAttribute("nonce", nonce);
    	requestObject.setNonce(nonce);

        String codeVerifier = OauthUtil.generateCodeVerifier();
        session.setAttribute("codeVerifier", codeVerifier);
        String codeChallenge = OauthUtil.generateCodeChallenge(codeVerifier);
    	requestObject.setCodeChallenge(codeChallenge);
    	requestObject.setCodeChallengeMethod("S256");

    	requestObject.setScope(clientConfig.getScope());
    	requestObject.setIssuer(clientConfig.getClientId());
    	requestObject.setAudience(wellKnownInfo.getIssuer());

    	long now = System.currentTimeMillis() / 1000L;
    	requestObject.setExp(now + 600L);
    	requestObject.setNbf(now);
        return requestObject;
    }
}
