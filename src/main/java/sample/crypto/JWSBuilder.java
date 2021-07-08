package sample.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import sample.clientapp.AsymmetricSignatureSignerContext;
import sample.util.OauthUtil;

public class JWSBuilder {
    String type;
    String kid;
    String contentType;
    byte[] contentBytes;

    public JWSBuilder jsonContent(Object object) {
        try {
        	ObjectMapper mapper = new ObjectMapper();
            this.contentBytes = mapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public String sign(AsymmetricSignatureSignerContext signer) {
        kid = signer.getKid();

        StringBuilder buffer = new StringBuilder();
        byte[] data = contentBytes;
        encode(signer.getAlgorithm(), data, buffer);
        byte[] signature = null;
        try {
            signature = signer.sign(buffer.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return encodeAll(buffer, signature);
    }

    protected String encodeAll(StringBuilder encoding, byte[] signature) {
        encoding.append('.');
        if (signature != null) {
        	encoding.append(OauthUtil.encodeToBase64Url(signature));
        }
        return encoding.toString();
    }

    protected void encode(String sigAlgName, byte[] data, StringBuilder encoding) {
        encoding.append(encodeHeader(sigAlgName));
        encoding.append('.');
        encoding.append(OauthUtil.encodeToBase64Url(data));
    }

    protected String encodeHeader(String sigAlgName) {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"alg\":\"").append(sigAlgName).append("\"");

        if (type != null) builder.append(",\"typ\" : \"").append(type).append("\"");
        if (kid != null) builder.append(",\"kid\" : \"").append(kid).append("\"");
        if (contentType != null) builder.append(",\"cty\":\"").append(contentType).append("\"");
        builder.append("}");
        return OauthUtil.encodeToBase64Url(builder.toString().getBytes(StandardCharsets.UTF_8));
    }
}
