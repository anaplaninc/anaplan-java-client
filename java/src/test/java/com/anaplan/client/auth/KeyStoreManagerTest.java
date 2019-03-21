package com.anaplan.client.auth;

import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.BaseTest;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 1/15/18
 * Time: 6:03 PM
 */
public class KeyStoreManagerTest extends BaseTest {

    private final String keyStorePath = "src/test/resources/auth/sample_keystore.jks";
    private final static String PKCS8_PRIVATE_KEY_FILEPATH = "src/test/resources/auth/sample_privateKey.pkcs8";
    private static final String SIGNING_ALGORITHM = "SHA256withRSA";
    private static final String KEYSTORE_TYPE = "JKS";
    private static final String CERT_TYPE = "X.509";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String KEYSTORE_ALIAS = "cert_pvkey";
    private KeyStoreManager keyStoreManager;

    @Before
    public void setUp() {
        keyStoreManager = new KeyStoreManager();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetGoodKeyStore() throws Exception {
        KeyStore sampleKeyStore = keyStoreManager.getKeystore(keyStorePath, KEYSTORE_PASSWORD);
        assertNotNull(sampleKeyStore);
        assertEquals(KEYSTORE_TYPE, sampleKeyStore.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInvalidKeyStore() throws Exception {
        keyStoreManager.getKeyStorePrivateKey("some/bad/path", KEYSTORE_PASSWORD, KEYSTORE_ALIAS);
    }

    @Test
    public void testGetKeyStoreCertificate() throws Exception {
        X509Certificate cert = keyStoreManager.getKeyStoreCertificate(keyStorePath, KEYSTORE_PASSWORD, KEYSTORE_ALIAS);
        assertNotNull(cert);
        assertEquals(SIGNING_ALGORITHM, cert.getSigAlgName());
        assertEquals(CERT_TYPE, cert.getType());
    }

    @Test(expected = AnaplanAPIException.class)
    public void testGetKeyStoreCertificateBadAlias() throws Exception {
        try {
            keyStoreManager.getKeyStoreCertificate(keyStorePath, KEYSTORE_PASSWORD, "bad_alias");
        } catch (AnaplanAPIException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("Invalid certificate-alias provided!"));
            throw e;
        }
    }

    @Test(expected = AnaplanAPIException.class)
    public void testGetKeyStoreCertificateBadPassword() throws Exception {
        try {
            keyStoreManager.getKeyStoreCertificate(keyStorePath, "bad_password", KEYSTORE_ALIAS);
        } catch (AnaplanAPIException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("Unable to read keystore file!"));
            throw e;
        }
    }

    @Test
    public void testGetKeyStorePrivateKey() throws Exception {
        RSAPrivateKey privateKey = keyStoreManager.getKeyStorePrivateKey(keyStorePath, KEYSTORE_PASSWORD, KEYSTORE_ALIAS);
        assertNotNull(privateKey);
        assertEquals(KEY_ALGORITHM, privateKey.getAlgorithm());
    }

    @Test(expected = AnaplanAPIException.class)
    public void testGetKeyStorePrivateKeyBadAlias() throws Exception {
        try {
            keyStoreManager.getKeyStorePrivateKey(keyStorePath, KEYSTORE_PASSWORD, "bad_alias");
        } catch (AnaplanAPIException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("Invalid alias provided to read Private-Key: "));
            throw e;
        }
    }

    @Test
    public void testPrivateKeys() throws Exception {
        RSAPrivateKey privateKeyFromFile = openPkcs8PKFile(PKCS8_PRIVATE_KEY_FILEPATH);
        RSAPrivateKey privateKeyFromKeyStore = keyStoreManager.getKeyStorePrivateKey(keyStorePath, KEYSTORE_PASSWORD, KEYSTORE_ALIAS);
        assertNotNull(privateKeyFromFile);
        assertNotNull(privateKeyFromKeyStore);
        assertEquals(privateKeyFromFile, privateKeyFromKeyStore);
    }
}
