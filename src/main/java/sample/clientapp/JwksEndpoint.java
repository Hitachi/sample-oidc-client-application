package sample.clientapp;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import sample.config.SslConfiguration;
import sample.jwt.JSONWebKeySet;
import sample.jwt.RSAPublicJWK;
import sample.util.KeyUtils;
import sample.util.OauthUtil;

public class JwksEndpoint {

    private SslConfiguration sslConfig;
    
    public JwksEndpoint(SslConfiguration sslConfig) {
		this.sslConfig = sslConfig;
	}

    public ResponseEntity<Map<String, Object>> returnResponse() {
    	KeyUtils keyUtils = new KeyUtils(sslConfig);
		PublicKey publicKey = keyUtils.getPublicKey();
		RSAPublicKey rsaKey = (RSAPublicKey) publicKey;
		RSAPublicJWK jwk = new RSAPublicJWK();
		jwk.setKeyType("RSA");
		jwk.setPublicKeyUse("sig");
		jwk.setAlgorithm("PS256");
		jwk.setKeyId(sslConfig.getKeyAlias());
		jwk.setModulus(OauthUtil.encodeToBase64Url(toIntegerBytes(rsaKey.getModulus())));
		jwk.setPublicExponent(OauthUtil.encodeToBase64Url(toIntegerBytes(rsaKey.getPublicExponent())));

		JSONWebKeySet jwks = new JSONWebKeySet();
		jwks.setKeys(new RSAPublicJWK[] { jwk });

		@SuppressWarnings("unchecked")
		Map<String, Object> map = new ObjectMapper().convertValue(jwks, Map.class);

		return new ResponseEntity<>(map, HttpStatus.OK);
    }

	private static byte[] toIntegerBytes(final BigInteger bigInt) {
		int bitlen = bigInt.bitLength();
		// round bitlen
		bitlen = ((bitlen + 7) >> 3) << 3;
		final byte[] bigBytes = bigInt.toByteArray();

		if (((bigInt.bitLength() % 8) != 0) && (((bigInt.bitLength() / 8) + 1) == (bitlen / 8))) {
			return bigBytes;
		}
		// set up params for copying everything but sign bit
		int startSrc = 0;
		int len = bigBytes.length;

		// if bigInt is exactly byte-aligned, just skip signbit in copy
		if ((bigInt.bitLength() % 8) == 0) {
			startSrc = 1;
			len--;
		}
		final int startDst = bitlen / 8 - len; // to pad w/ nulls as per spec
		final byte[] resizedBytes = new byte[bitlen / 8];
		System.arraycopy(bigBytes, startSrc, resizedBytes, startDst, len);
		return resizedBytes;
	}
}
