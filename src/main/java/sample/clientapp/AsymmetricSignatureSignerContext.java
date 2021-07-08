package sample.clientapp;

import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import sample.crypto.KeyWrapper;

public class AsymmetricSignatureSignerContext {

    private final KeyWrapper key;

    public AsymmetricSignatureSignerContext(KeyWrapper key) throws SignatureException {
        this.key = key;
    }

    public String getKid() {
        return key.getKid();
    }

    public String getAlgorithm() {
        return key.getAlgorithm();
    }

	public byte[] sign(byte[] data) throws SignatureException {
		try {
			Security.addProvider(new BouncyCastleProvider());
			Signature signature = Signature.getInstance("SHA256withRSAandMGF1");
			signature.initSign((PrivateKey) key.getPrivateKey());
			signature.update(data);
			return signature.sign();
		} catch (Exception e) {
			throw new SignatureException("Signing failed", e);
		}
	}
}
