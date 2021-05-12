package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.auth.dto.CACertNonceVerificationData;
import com.anaplan.client.dto.responses.AuthenticationResp;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.exceptions.AnaplanAPITransportException;
import com.anaplan.client.transport.ConnectionProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CertificateAuthenticator extends AbstractAuthenticator {

  private static final Logger LOG = LoggerFactory.getLogger(CertificateAuthenticator.class);
  private static final int MIN_NONCE_LENGTH = 100;
  private static final int MAX_NONCE_LENGTH = 200;

  public CertificateAuthenticator(ConnectionProperties connectionProperties,
      AnaplanAuthenticationAPI authClient) {
    super(connectionProperties, authClient);
  }

  @Override
  public byte[] authenticate() {
    LOG.info("Authenticating via Certificate...");
    try {
      Credentials apiCredentials = connectionProperties.getApiCredentials();
      X509Certificate certificate = apiCredentials.getCertificate();
      RSAPrivateKey privateKey = apiCredentials.getPrivateKey();
      String certificateHash = generateCertHash(certificate);
      String nonceVerificationData = createNonceVerificationData(privateKey);
      AuthenticationResp authResponse = authClient.authenticateCertificate(
          certificateHash,
          nonceVerificationData);

      authTokenExpiresAt = authResponse.getItem().getExpiresAt();

      String tokenValue = authResponse.getItem().getTokenValue();

      return tokenValue.getBytes();
    } catch (Exception ex) {
      throw new AnaplanAPIException("Certificate Authentication failed!", ex);
    }
  }

  /**
   * Generate a random byte-array to help with the private-key challenge
   *
   * @param count
   */
  public static byte[] getRandomBytes(int count) {
    SecureRandom random = new SecureRandom();
    random.setSeed(System.currentTimeMillis());

    byte bytes[] = new byte[count];
    random.nextBytes(bytes);

    return bytes;
  }

  /**
   * This is required by Auth-service to verify the private-key provided for the CA-Cert provided by
   * the customer for API authentication.
   *
   * @param privateKey Private key object read from KeyStore
   */
  String createNonceVerificationData(RSAPrivateKey privateKey) {
    try {
      byte[] randomBytes = getRandomBytes((MIN_NONCE_LENGTH + MAX_NONCE_LENGTH) / 2);
      CACertNonceVerificationData challengeData = new CACertNonceVerificationData(randomBytes,
          privateKey);
      return toJson(challengeData);
    } catch (InvalidKeyException | SignatureException e) {
      throw new AnaplanAPITransportException(
          "Could not create certificate Nonce verification data!", e);
    } catch (NoSuchAlgorithmException e) {
      throw new AnaplanAPITransportException("Unable to extract private-key: ", e);
    } catch (Exception e) {
      throw new AnaplanAPITransportException("Unable to create JSON nonce data!", e);
    }
  }

  /**
   * Generates the Base64 encoded value from the certificate contents
   *
   * @param certificate
   */
  String generateCertHash(X509Certificate certificate) {
    // use bouncycastle library to generate cert in PEM format 
    Writer stringWriter = new StringWriter();
    try (PemWriter pemWriter = new PemWriter(stringWriter)) {
      PemObject certPem = new PemObject("CERTIFICATE", certificate.getEncoded());
      pemWriter.writeObject(certPem);
    } catch (CertificateEncodingException | IOException ex) {
      throw new AnaplanAPITransportException("Failed to encode user certificate: " + ex);
    }

    // now base-64 encode the pem string as that is how auth-service expects it to be
    String certHash = Base64.getEncoder().encodeToString(stringWriter.toString().getBytes());
    return certHash;
  }

  public String toJson(CACertNonceVerificationData caCertNonceVerificationData) throws JsonProcessingException {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return ow.writeValueAsString(caCertNonceVerificationData);
  }
}
