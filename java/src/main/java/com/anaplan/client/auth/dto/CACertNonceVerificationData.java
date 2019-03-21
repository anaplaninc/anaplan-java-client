package com.anaplan.client.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

/**
 * Java representation of the JSON payload sent during cert auth. Payload structure is of this format:
 * <p>
 * {
 *    "encodedData":"base-64 encoded nonce string here",
 *    "encodedSignedData":"base-64 encoded signed nonce string here"
 * }
 * <p>
 * The supported algorithm for signing is specified in auth.properties file.
 */
public class CACertNonceVerificationData implements Serializable {

    private static final String NONCE_SIGNATURE_ALGORITHM = "SHA512withRSA";

    @JsonProperty
    private String encodedData;

    @JsonProperty
    private String encodedSignedData;

    @SuppressWarnings("unused")
    private CACertNonceVerificationData() {
    }

    public CACertNonceVerificationData(byte[] decodedData, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] decodedSignedData = sign(privateKey, decodedData);
        encodedData = Base64.getEncoder().encodeToString(decodedData);
        encodedSignedData = Base64.getEncoder().encodeToString(decodedSignedData);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CACertNonceVerificationData{");
        sb.append("encodedData='").append(encodedData).append('\'');
        sb.append(", encodedSignedData='").append(encodedSignedData).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String toJson() throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(this);
    }

    private byte[] sign(PrivateKey privateKey, byte[] dataBytes)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance(NONCE_SIGNATURE_ALGORITHM);

        sig.initSign(privateKey);
        sig.update(dataBytes);

        return sig.sign();
    }
}
