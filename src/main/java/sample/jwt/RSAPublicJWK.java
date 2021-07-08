package sample.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RSAPublicJWK {
	@JsonProperty("kid")
	private String keyId;
	@JsonProperty("kty")
	private String keyType;
	@JsonProperty("alg")
	private String algorithm;
	@JsonProperty("use")
	private String publicKeyUse;
	@JsonProperty("n")
	private String modulus;
	@JsonProperty("e")
	private String publicExponent;

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getPublicKeyUse() {
		return publicKeyUse;
	}

	public void setPublicKeyUse(String publicKeyUse) {
		this.publicKeyUse = publicKeyUse;
	}

	public String getModulus() {
		return modulus;
	}

	public void setModulus(String modulus) {
		this.modulus = modulus;
	}

	public String getPublicExponent() {
		return publicExponent;
	}

	public void setPublicExponent(String publicExponent) {
		this.publicExponent = publicExponent;
	}
}
