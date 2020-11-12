package sample.clientapp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "clientapp.config")
public class ClientAppConfiguration {
    private String authserverUrl;
    private String apiserverUrl;
    private String clientappUrl;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private String scope;

    public String getAuthserverUrl() {
        return authserverUrl;
    }

    public void setAuthserverUrl(String value) {
        authserverUrl = value;
    }

    public String getApiserverUrl() {
        return apiserverUrl;
    }

    public void setApiserverUrl(String value) {
        apiserverUrl = value;
    }

    public String getClientappUrl() {
        return clientappUrl;
    }

    public void setClientappUrl(String value) {
        clientappUrl = value;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String value) {
        clientId = value;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}