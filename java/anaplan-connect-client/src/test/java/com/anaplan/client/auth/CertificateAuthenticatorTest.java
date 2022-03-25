package com.anaplan.client.auth;

import com.anaplan.client.BaseTest;
import com.anaplan.client.Program;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.exceptions.PrivateKeyException;
import com.anaplan.client.transport.ConnectionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

import static com.anaplan.client.CertConstants.CERT_PREFIX;
import static com.anaplan.client.CertConstants.CERT_SUFFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by Spondon Saha User: spondonsaha Date: 12/13/17 Time: 1:49 PM
 */
public class CertificateAuthenticatorTest extends BaseTest {

  public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
  public static final String END_CERT = "-----END CERTIFICATE-----";
  private final static String CERT_FILEPATH = "auth/sample_cert.pem";
  private final static String PEM_PRIVATE_KEY_FILEPATH = "src/test/resources/auth/sample_privateKey.pem";
  public static final String PEM_PRIVATE_PASS_PHRASE = "anaplanconnector";
  private final static String PEM_PRIVATE_KEY_NO_PASS_FILEPATH = "src/test/resources/auth/sample_private_key_without_pass.pem";
  private static final CertificateFactory certFactory;
  private static final String TEST_CERT_PEM = "-----BEGIN CERTIFICATE-----\n" +
      "MIIFQTCCBCmgAwIBAgIQBelzh8tHzypbHiYsvT+TdTANBgkqhkiG9w0BAQsFADCB\n" +
      "lzELMAkGA1UEBhMCR0IxGzAZBgNVBAgTEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4G\n" +
      "A1UEBxMHU2FsZm9yZDEaMBgGA1UEChMRQ09NT0RPIENBIExpbWl0ZWQxPTA7BgNV\n" +
      "BAMTNENPTU9ETyBSU0EgQ2xpZW50IEF1dGhlbnRpY2F0aW9uIGFuZCBTZWN1cmUg\n" +
      "RW1haWwgQ0EwHhcNMTcxMTI5MDAwMDAwWhcNMTgxMTI5MjM1OTU5WjAsMSowKAYJ\n" +
      "KoZIhvcNAQkBFhtjZXJ0YXV0aHRlc3R1c2VyMUBnbWFpbC5jb20wggEiMA0GCSqG\n" +
      "SIb3DQEBAQUAA4IBDwAwggEKAoIBAQCr9byAU+ednyaOOZw+Mmgi1V6ve2C9hYfK\n" +
      "FcAdBRtu+0nNaYNktH9iFaw7g0N03gI5ul9hBrS88veyR10u219eENRNzCI2IhLq\n" +
      "rpRdGAUmiuL3GsHzbAAPKZkT/PWf8PTC8cV/w2pmYU4xxbagCGMCMoLxpxR1EO3w\n" +
      "Hxww4+fXrALOQVg65NM6DvUrYD8Scg9TXbfEMhJY89d/5f41T+yCe+qQAepyVFpf\n" +
      "6NenZT26Z2+9mmJPc+y6hZqkTK5zNEaWxh9lO495PYOejuIuWhf7RMXT80Gyyqp2\n" +
      "AXqZpxHeuamUbBkUs1wDxUyIfgqMfQT+vruWkdSGplHNNbaIvnfzAgMBAAGjggHx\n" +
      "MIIB7TAfBgNVHSMEGDAWgBSCr2yM+MX+lmF86B89K3FIXsSLwDAdBgNVHQ4EFgQU\n" +
      "N5p0dV9UQFiPBZq1pjk+9FBqzCAwDgYDVR0PAQH/BAQDAgWgMAwGA1UdEwEB/wQC\n" +
      "MAAwIAYDVR0lBBkwFwYIKwYBBQUHAwQGCysGAQQBsjEBAwUCMBEGCWCGSAGG+EIB\n" +
      "AQQEAwIFIDBGBgNVHSAEPzA9MDsGDCsGAQQBsjEBAgEBATArMCkGCCsGAQUFBwIB\n" +
      "Fh1odHRwczovL3NlY3VyZS5jb21vZG8ubmV0L0NQUzBaBgNVHR8EUzBRME+gTaBL\n" +
      "hklodHRwOi8vY3JsLmNvbW9kb2NhLmNvbS9DT01PRE9SU0FDbGllbnRBdXRoZW50\n" +
      "aWNhdGlvbmFuZFNlY3VyZUVtYWlsQ0EuY3JsMIGLBggrBgEFBQcBAQR/MH0wVQYI\n" +
      "KwYBBQUHMAKGSWh0dHA6Ly9jcnQuY29tb2RvY2EuY29tL0NPTU9ET1JTQUNsaWVu\n" +
      "dEF1dGhlbnRpY2F0aW9uYW5kU2VjdXJlRW1haWxDQS5jcnQwJAYIKwYBBQUHMAGG\n" +
      "GGh0dHA6Ly9vY3NwLmNvbW9kb2NhLmNvbTAmBgNVHREEHzAdgRtjZXJ0YXV0aHRl\n" +
      "c3R1c2VyMUBnbWFpbC5jb20wDQYJKoZIhvcNAQELBQADggEBAAC++8/u3tNK3o5n\n" +
      "oE1D6w/GzfAE0usXrF7gp/hn6lqSkq//p4clGCoYFyLuzNIdF8vzf3+MfM027K2j\n" +
      "/dN5kvASDM2KWwvsNFS1EDJMEfgWs/Nnj5scVJBV4NP4zZ1AriYSsz9/Z7nI3Zof\n" +
      "afxl9M6GSd/keViMp2pVi8IQECy+jnyj7cgY6MV+6aLHOSxdFYOuvpX9M7+4KERN\n" +
      "cj71mTgx4lKz1zmXJRXgrBreLOp08Te3Jxs9s8RHOVBVYNIxuwtEdOXsPJZyzCcW\n" +
      "V5DAt3vIm+lKIE8Q1vF20rb8xWrtVBtsfRnLJw7+nOVDL2y9z8J+Tr5Ydb/lbGHg\n" +
      "Uo8q658=\n" +
      "-----END CERTIFICATE-----";
  private static final X509Certificate TEST_CERT;

  static {
    try {
      certFactory = CertificateFactory.getInstance("X509");
      TEST_CERT = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(
          TEST_CERT_PEM.getBytes()));
    } catch (CertificateException e) {
      throw new RuntimeException("Unable to create CertificateFactory", e);
    }
  }

  private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
  private final String mockAuthToken = "authentication-token";
  private final String authResponse = "responses/auth_response.json";
  private final ObjectMapper objectMapper = new ObjectMapper();
  private CertificateAuthenticator certAuth;
  private AnaplanAuthenticationAPI mockAuthApi;
  private RSAPrivateKey privateKey;
  private X509Certificate certificate;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    mockAuthApi = mock(AnaplanAuthenticationAPI.class);
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    certificate = (X509Certificate) certificateFactory.generateCertificate(
        new ByteArrayInputStream(
            Base64.getDecoder().decode(
                new String(getFixture(CERT_FILEPATH), StandardCharsets.UTF_8)
                    .replaceAll("\n", "")
                    .split(BEGIN_CERT)[1]
                    .split(END_CERT)[0]
            )
        )
    );
  }

  @After
  public void tearDown() {
    Mockito.reset(mockAuthApi);
  }

//    @Test
//    public void testGetAuthToken() throws Exception {
//        ConnectionProperties props = new ConnectionProperties();
//        props.setApiCredentials(new Credentials(certificate, Program.loadPrivateKeyFromFile(PEM_PRIVATE_KEY_FILEPATH,passphrase)));
//        props.setAuthServiceUri(new URI(mockAuthServiceUrl));
//        certAuth = Mockito.spy(new MockCertificateAuthenticator(props, mockAuthApi));
//        AuthenticationResp authenticationResp = objectMapper.readValue(getFixture(authResponse), AuthenticationResp.class);
//        doReturn(authenticationResp)
//                .when(mockAuthApi)
//                .authenticateCertificate(Mockito.anyString(), Mockito.anyString());
//        assertEquals(mockAuthToken, certAuth.getAuthToken());
//        assertEquals(authenticationResp.getItem().getExpiresAt(), certAuth.getAuthTokenExpiresAt());
//    }

//    @Test
//    public void testNotNullPrivateKeys() throws Exception {
//        RSAPrivateKey privateKeyFromFile = Program.loadPrivateKeyFromFile(PEM_PRIVATE_KEY_FILEPATH,passphrase);
//        assertNotNull(privateKeyFromFile);
//    }


  @Test
  public void encrypted_or_unencrypted_cert_null_path_with_no_passphrase_should_fail() {
    expect(IllegalArgumentException.class, "Please check the value(s).", true);
    ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", null, "");
  }

  @Test
  public void encrypted_or_unencrypted_cert_incorrect_path_should_fail() {
    expect(IllegalArgumentException.class, "Private key file doesn't exist on path", true);
    ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", "random/path/that/doesnot/exist.pem", "");
  }

  @Test
  public void encrypted_or_unencrypted_cert_empty_path_should_fail() {
    expect(IllegalArgumentException.class, "Private key file doesn't exist on path", true);
    ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", "", "");
  }

  @Test
  public void encrypted_cert_valid_path_null_passphrase_should_fail() {
    expect(IllegalArgumentException.class, "Please check the value(s).", true);
    ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", PEM_PRIVATE_KEY_FILEPATH, null);
  }

  @Test
  public void encrypted_cert_valid_path_no_passphrase_should_fail() {
    expect(IllegalArgumentException.class, "Empty passphrase provided for private key. Please check the passphrase.", true);
    ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", PEM_PRIVATE_KEY_FILEPATH, "");
  }

  @Test
  public void encrypted_cert_valid_path_incorrect_passphrase_should_fail() {
    expect(IllegalArgumentException.class, "Incorrect passphrase provided for private key. Please check the passphrase.", true);
    ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", PEM_PRIVATE_KEY_FILEPATH, "wrongPassphrase");
  }

  @Test
  public void encrypted_cert_valid_path_correct_passphrase_should_pass() {
    RSAPrivateKey rsaPrivateKey = ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", PEM_PRIVATE_KEY_FILEPATH, PEM_PRIVATE_PASS_PHRASE);
    assertThat(rsaPrivateKey, is(notNullValue()));
  }

  @Test
  public void unencrypted_cert_valid_path_null_passphrase_should_fail() {
    expect(IllegalArgumentException.class, "Please check the value(s).", true);
    ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", PEM_PRIVATE_KEY_NO_PASS_FILEPATH, null);
  }

  @Test
  public void unencrypted_cert_valid_path_no_passphrase_should_pass() {
    RSAPrivateKey rsaPrivateKey = ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", PEM_PRIVATE_KEY_NO_PASS_FILEPATH, "");
    assertThat(rsaPrivateKey, is(notNullValue()));
  }

  @Test
  public void unencrypted_cert_valid_path_random_passphrase_should_fail() throws PrivateKeyException {
    expect(PrivateKeyException.class, "Could not load your privateKey", true);
    ReflectionTestUtils.invokeMethod(Program.class, "loadPrivateKeyFromFile", PEM_PRIVATE_KEY_NO_PASS_FILEPATH, "randomPassphrase");
  }

//    @Test(expected = AnaplanAPITransportException.class)
//    public void testCreateNonceVerificationDataBadPrivateKey() throws Exception {
//        ConnectionProperties props = new ConnectionProperties();
//        props.setApiCredentials(new Credentials(certificate, Program.loadPrivateKeyFromFile(PEM_PRIVATE_KEY_FILEPATH,passphrase)));
//        props.setAuthServiceUri(new URI(mockAuthServiceUrl));
//        privateKey = Mockito.mock(RSAPrivateKey.class);
//        doReturn(KEY_ALGORITHM)
//                .when(privateKey)
//                .getAlgorithm();
//        certAuth = Mockito.spy(new MockCertificateAuthenticator(props, mockAuthApi));
//        try {
//            certAuth.createNonceVerificationData(privateKey);
//        } catch (AnaplanAPITransportException e) {
//            assertThat(e.getMessage(), CoreMatchers.containsString("Could not create certificate Nonce verification data!"));
//            throw e;
//        }
//    }

  @Test
  public void testGenerateCertHash() throws Exception {
    CertificateAuthenticator certificateAuthenticator =
        new CertificateAuthenticator(mock(ConnectionProperties.class), mock(AnaplanAuthenticationAPI.class));

    String certPemFormat = certificateAuthenticator.generateCertHash(TEST_CERT);

    byte[] certPemDecoded = Base64.getDecoder().decode(certPemFormat);

    String certPemDecodedStr = new String(certPemDecoded);

    String certPemDecodedTrimmedStr = certPemDecodedStr.trim();

    assertTrue(certPemDecodedTrimmedStr.startsWith(CERT_PREFIX));
    assertTrue(certPemDecodedTrimmedStr.endsWith(CERT_SUFFIX));

    // try to regenerate the cert object from the pem format
    assertEquals(TEST_CERT,
        certFactory.generateCertificate(new ByteArrayInputStream(certPemDecodedStr.getBytes(StandardCharsets.UTF_8))));
  }

  private void expect(final Class<? extends Throwable> throwable, final String messageSubstring, final boolean checkMsg) {
    expectedException.expect(throwable);
    if (checkMsg)
      expectedException.expectMessage(CoreMatchers.containsString(messageSubstring));
  }

}
