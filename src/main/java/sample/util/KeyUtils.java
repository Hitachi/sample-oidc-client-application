package sample.util;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import sample.config.SslConfiguration;

public class KeyUtils {

	private SslConfiguration sslConfig;

    public KeyUtils(SslConfiguration sslConfig) {
		this.sslConfig = sslConfig;
	}

	public PrivateKey getPrivateKey() {
		String alias = sslConfig.getKeyAlias();
		String privateKeyPass = sslConfig.getKeyPassword();

		PrivateKey privateKey = null;
		try {
			KeyStore ks = getKeyStore();
			privateKey = (PrivateKey) ks.getKey(alias, privateKeyPass.toCharArray());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return privateKey;
	}

	public PublicKey getPublicKey() {
		String alias = sslConfig.getKeyAlias();

		PublicKey publicKey = null;
		try {
			KeyStore ks = getKeyStore();
			X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);
			publicKey = certificate.getPublicKey();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return publicKey;
	}

	private KeyStore getKeyStore() throws Exception{
		String ksType = sslConfig.getKeyStoreType();
		String keyStoreFile = sslConfig.getKeyStorePath();
		String keyStorePass = sslConfig.getKeyStorePassword();

		KeyStore ks = null;
		ks = KeyStore.getInstance(ksType);
		//ks.load(new FileInputStream(keyStoreFile), keyStorePass.toCharArray());
		ks.load(getClass().getResourceAsStream(keyStoreFile), keyStorePass.toCharArray());

		return ks;
	}
}
