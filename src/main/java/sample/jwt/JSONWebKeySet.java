package sample.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JSONWebKeySet {
    @JsonProperty("keys")
    private RSAPublicJWK[] keys;

    public RSAPublicJWK[] getKeys() {
        return keys;
    }

    public void setKeys(RSAPublicJWK[] keys) {
        this.keys = keys;
    }

}
