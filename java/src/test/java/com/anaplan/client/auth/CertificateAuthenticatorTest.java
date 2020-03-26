package com.anaplan.client.auth;

import com.anaplan.client.BaseTest;
import com.anaplan.client.Program;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.ex.PrivateKeyException;
import com.anaplan.client.transport.ConnectionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.StringBufferInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

import static com.anaplan.client.CertConstants.CERT_PREFIX;
import static com.anaplan.client.CertConstants.CERT_SUFFIX;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 12/13/17
 * Time: 1:49 PM
 */
public class CertificateAuthenticatorTest extends BaseTest {

    private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
    private final static String CERT_FILEPATH = "auth/sample_cert.pem";
    private final static String PEM_PRIVATE_KEY_FILEPATH = "src/test/resources/auth/sample_privateKey.pem";
    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public static final String passphrase = "anaplanconnector";
    private final String mockAuthToken = "authentication-token";
    private static final String KEY_ALGORITHM = "RSA";
    private CertificateAuthenticator certAuth;
    private AnaplanAuthenticationAPI mockAuthApi;
    private String authResponse = "responses/auth_response.json";
    private ObjectMapper objectMapper = new ObjectMapper();
    private RSAPrivateKey privateKey;
    private X509Certificate certificate;

    private static String TEST_CERT_PEM = "-----BEGIN CERTIFICATE-----\n"+
            "MIIFQTCCBCmgAwIBAgIQBelzh8tHzypbHiYsvT+TdTANBgkqhkiG9w0BAQsFADCB\n"+
            "lzELMAkGA1UEBhMCR0IxGzAZBgNVBAgTEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4G\n"+
            "A1UEBxMHU2FsZm9yZDEaMBgGA1UEChMRQ09NT0RPIENBIExpbWl0ZWQxPTA7BgNV\n"+
            "BAMTNENPTU9ETyBSU0EgQ2xpZW50IEF1dGhlbnRpY2F0aW9uIGFuZCBTZWN1cmUg\n"+
            "RW1haWwgQ0EwHhcNMTcxMTI5MDAwMDAwWhcNMTgxMTI5MjM1OTU5WjAsMSowKAYJ\n"+
            "KoZIhvcNAQkBFhtjZXJ0YXV0aHRlc3R1c2VyMUBnbWFpbC5jb20wggEiMA0GCSqG\n"+
            "SIb3DQEBAQUAA4IBDwAwggEKAoIBAQCr9byAU+ednyaOOZw+Mmgi1V6ve2C9hYfK\n"+
            "FcAdBRtu+0nNaYNktH9iFaw7g0N03gI5ul9hBrS88veyR10u219eENRNzCI2IhLq\n"+
            "rpRdGAUmiuL3GsHzbAAPKZkT/PWf8PTC8cV/w2pmYU4xxbagCGMCMoLxpxR1EO3w\n"+
            "Hxww4+fXrALOQVg65NM6DvUrYD8Scg9TXbfEMhJY89d/5f41T+yCe+qQAepyVFpf\n"+
            "6NenZT26Z2+9mmJPc+y6hZqkTK5zNEaWxh9lO495PYOejuIuWhf7RMXT80Gyyqp2\n"+
            "AXqZpxHeuamUbBkUs1wDxUyIfgqMfQT+vruWkdSGplHNNbaIvnfzAgMBAAGjggHx\n"+
            "MIIB7TAfBgNVHSMEGDAWgBSCr2yM+MX+lmF86B89K3FIXsSLwDAdBgNVHQ4EFgQU\n"+
            "N5p0dV9UQFiPBZq1pjk+9FBqzCAwDgYDVR0PAQH/BAQDAgWgMAwGA1UdEwEB/wQC\n"+
            "MAAwIAYDVR0lBBkwFwYIKwYBBQUHAwQGCysGAQQBsjEBAwUCMBEGCWCGSAGG+EIB\n"+
            "AQQEAwIFIDBGBgNVHSAEPzA9MDsGDCsGAQQBsjEBAgEBATArMCkGCCsGAQUFBwIB\n"+
            "Fh1odHRwczovL3NlY3VyZS5jb21vZG8ubmV0L0NQUzBaBgNVHR8EUzBRME+gTaBL\n"+
            "hklodHRwOi8vY3JsLmNvbW9kb2NhLmNvbS9DT01PRE9SU0FDbGllbnRBdXRoZW50\n"+
            "aWNhdGlvbmFuZFNlY3VyZUVtYWlsQ0EuY3JsMIGLBggrBgEFBQcBAQR/MH0wVQYI\n"+
            "KwYBBQUHMAKGSWh0dHA6Ly9jcnQuY29tb2RvY2EuY29tL0NPTU9ET1JTQUNsaWVu\n"+
            "dEF1dGhlbnRpY2F0aW9uYW5kU2VjdXJlRW1haWxDQS5jcnQwJAYIKwYBBQUHMAGG\n"+
            "GGh0dHA6Ly9vY3NwLmNvbW9kb2NhLmNvbTAmBgNVHREEHzAdgRtjZXJ0YXV0aHRl\n"+
            "c3R1c2VyMUBnbWFpbC5jb20wDQYJKoZIhvcNAQELBQADggEBAAC++8/u3tNK3o5n\n"+
            "oE1D6w/GzfAE0usXrF7gp/hn6lqSkq//p4clGCoYFyLuzNIdF8vzf3+MfM027K2j\n"+
            "/dN5kvASDM2KWwvsNFS1EDJMEfgWs/Nnj5scVJBV4NP4zZ1AriYSsz9/Z7nI3Zof\n"+
            "afxl9M6GSd/keViMp2pVi8IQECy+jnyj7cgY6MV+6aLHOSxdFYOuvpX9M7+4KERN\n"+
            "cj71mTgx4lKz1zmXJRXgrBreLOp08Te3Jxs9s8RHOVBVYNIxuwtEdOXsPJZyzCcW\n"+
            "V5DAt3vIm+lKIE8Q1vF20rb8xWrtVBtsfRnLJw7+nOVDL2y9z8J+Tr5Ydb/lbGHg\n"+
            "Uo8q658=\n"+
            "-----END CERTIFICATE-----";

    private static X509Certificate TEST_CERT;

    private static final CertificateFactory certFactory;

    static {
        try {
            certFactory = CertificateFactory.getInstance("X509");
            TEST_CERT = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(
                    TEST_CERT_PEM.getBytes()));
        } catch (CertificateException e) {
            throw new RuntimeException("Unable to create CertificateFactory", e);
        }
    }
    
    @Before
    public void setUp() throws Exception {
        mockAuthApi = Mockito.mock(AnaplanAuthenticationAPI.class);
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

    @Test(expected = PrivateKeyException.class)
    public void testNullPrivateKeys() throws PrivateKeyException {
        try {
            Program.loadPrivateKeyFromFile(null,passphrase);
        } catch (PrivateKeyException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("Could not load your privateKey"));
            throw e;
        }
    }

    @Test(expected = PrivateKeyException.class)
    public void testNullPassphrase() throws PrivateKeyException {
        try {
            Program.loadPrivateKeyFromFile(PEM_PRIVATE_KEY_FILEPATH,null);
        } catch (PrivateKeyException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("Could not load your privateKey"));
            throw e;
        }
    }

    @Test(expected = PrivateKeyException.class)
    public void testNullPassphrasePrivateKeys() throws PrivateKeyException {
        try {
            Program.loadPrivateKeyFromFile(null,null);
        } catch (PrivateKeyException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("Could not load your privateKey"));
            throw e;
        }
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
                new CertificateAuthenticator(mock(ConnectionProperties.class));

        String certPemFormat = certificateAuthenticator.generateCertHash(TEST_CERT);

        byte[] certPemDecoded = Base64.getDecoder().decode(certPemFormat);

        String certPemDecodedStr = new String(certPemDecoded);
        
        String certPemDecodedTrimmedStr = certPemDecodedStr.trim();
        
        assertTrue(certPemDecodedTrimmedStr.startsWith(CERT_PREFIX));
        assertTrue(certPemDecodedTrimmedStr.endsWith(CERT_SUFFIX));

        // try to regenerate the cert object from the pem format
        assertEquals(TEST_CERT, certFactory.generateCertificate(new StringBufferInputStream(certPemDecodedStr)));
    }
}
