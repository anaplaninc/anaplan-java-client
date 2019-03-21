package com.anaplan.client.auth;

import com.anaplan.client.ex.AnaplanAPIException;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

/**
 * Used for reading a Java Keystore (JKS) file to fetch any Private-keys or CA-Certificates that
 * might be stored, keyed by an alias and protected by a KeyStore-password.
 */
public class KeyStoreManager {

    /**
     * Reads the Keystore using provided keyStorePath and unlocks it using the
     * provided keyStorePassword
     *
     * @param keyStorePath
     * @param keyStorePassword
     * @return
     * @throws KeyStoreException
     */
    public KeyStore getKeystore(String keyStorePath, String keyStorePassword)
            throws KeyStoreException {

        Preconditions.checkNotNull(keyStorePath, "Keystore path cannot be null!");
        Preconditions.checkNotNull(keyStorePassword, "Keystore password cannot be null!");

        // load the key store containing the client certificate
        KeyStore keyStore = KeyStore.getInstance("JKS");
        File clientKeyStoreLocation = new File(keyStorePath);
        if (clientKeyStoreLocation.isFile()) {
            try (InputStream keystoreInput = new FileInputStream(clientKeyStoreLocation)) {
                keyStore.load(keystoreInput, keyStorePassword.toCharArray());
                return keyStore;
            } catch (IOException e) {
                throw new AnaplanAPIException("Unable to read keystore file!", e);
            } catch (NoSuchAlgorithmException | CertificateException e) {
                throw new AnaplanAPIException("Unable to read Certificate!", e);
            }
        } else {
            throw new IllegalArgumentException("The specified key store path '" + keyStorePath
                    + "' is invalid");
        }
    }

    /**
     * Fetches the X509 CA certificate from the Java Keystore using the provided
     * Keystore password and alias.
     *
     * @param keyStorePath
     * @param keyStorePassword
     * @param keyStoreAlias
     * @return
     * @throws KeyStoreException
     */
    public X509Certificate getKeyStoreCertificate(String keyStorePath, String keyStorePassword,
                                                  String keyStoreAlias) throws KeyStoreException {

        Preconditions.checkNotNull(keyStorePath, "Keystore path cannot be null!");
        Preconditions.checkNotNull(keyStorePassword, "Keystore password cannot be null!");
        Preconditions.checkNotNull(keyStoreAlias, "Keystore alias cannot be null!");

        KeyStore keyStore = getKeystore(keyStorePath, keyStorePassword);

        if (keyStore.containsAlias(keyStoreAlias)) {
            return (X509Certificate) keyStore.getCertificate(keyStoreAlias);
        } else {
            throw new AnaplanAPIException("Invalid certificate-alias provided!");
        }
    }

    /**
     * Fetches the RSA private-key from the Java keystore using the provided
     * keystore-password and keystore-alias.
     *
     * @param keyStorePath
     * @param keyStorePassword
     * @param keyStoreAlias
     * @return
     * @throws KeyStoreException
     */
    public RSAPrivateKey getKeyStorePrivateKey(String keyStorePath, String keyStorePassword,
                                               String keyStoreAlias) throws KeyStoreException {

        Preconditions.checkNotNull(keyStorePath, "Keystore path cannot be null!");
        Preconditions.checkNotNull(keyStorePassword, "Keystore password cannot be null!");
        Preconditions.checkNotNull(keyStoreAlias, "Keystore alias cannot be null!");

        KeyStore keyStore = getKeystore(keyStorePath, keyStorePassword);

        try {
            KeyStore.PasswordProtection password = new KeyStore.PasswordProtection(
                    keyStorePassword.toCharArray());
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                    keyStoreAlias, password);

            if (privateKeyEntry != null) {
                return (RSAPrivateKey) privateKeyEntry.getPrivateKey();
            } else {
                throw new AnaplanAPIException("Invalid alias provided to read Private-Key: "
                        + keyStoreAlias);
            }
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new AnaplanAPIException("Invalid Private key alias provided!", e);
        }
    }
}
