package sample.clientapp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "clientapp.config")
public class ClientAppConfiguration {
    private String keycloakUrl;
    private String apiserverUrl;
    private String clientappUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String scope;

    public String getKeycloakUrl() {
        return keycloakUrl;
    }

    public void setKeycloakUrl(String value) {
        keycloakUrl = value;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String value) {
        realm = value;
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