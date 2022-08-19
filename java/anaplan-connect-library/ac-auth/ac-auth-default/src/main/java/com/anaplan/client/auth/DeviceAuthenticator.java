package com.anaplan.client.auth;

import com.anaplan.client.CryptoUtil;
import com.anaplan.client.Utils;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.dto.DeviceCodeInfo;
import com.anaplan.client.dto.OauthTokenInfo;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.transport.ConnectionProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Objects;

/**
 * Authenticate by device
 */
public class DeviceAuthenticator extends AbstractAuthenticator {

  public static final String ROTATABLE = "rotatable";

  private static final Logger LOG = LoggerFactory.getLogger(DeviceAuthenticator.class);
  private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code";
  private static final String DEVICE_SCOPE = "openid profile email offline_access";
  private static final String REFRESH_TOKEN = "refresh_token";
  private static final String JKS = ".jks";
  private final String refreshTokenKeyStorePath;
  private final String refreshTokenKeyStoreName;
  private final char[] keystorePass;
  private OauthTokenInfo oauthTokenInfo;

  public DeviceAuthenticator(ConnectionProperties connectionProperties,
                             AnaplanAuthenticationAPI authClient) {
    super(connectionProperties, authClient);
    refreshTokenKeyStoreName = String.format("ks_%s", Utils.bytesToHex(Utils.createHash(connectionProperties.getClientId())));
    String keystoreDir = System.getProperty("user.home");
    if (StringUtils.isNotBlank(System.getenv("AC_OAUTH_KEYSTORE_DIR"))) {
      keystoreDir = System.getenv("AC_OAUTH_KEYSTORE_DIR");
    }
    refreshTokenKeyStorePath = keystoreDir + FileSystems.getDefault().getSeparator() + refreshTokenKeyStoreName + JKS;
    keystorePass = CryptoUtil.encrypt(this.connectionProperties.getClientId()).toCharArray();
  }

  @Override
  public byte[] authenticate() {
    LOG.info("Authenticating via Device...");
    String refreshToken = getDecodedRefreshToken();
    if (connectionProperties.isForceRegister()) {
      clearRefreshTokenEntry();
      deviceRegistration();
      return authToken;
    } else if (StringUtils.isEmpty(refreshToken)) {
      deviceRegistration();
      return authToken;
    } else {
      synchronized (this) {
        return refreshToken();
      }
    }
  }

  public void clearRefreshTokenEntry() {
    LOG.info("Deleting already existing JKS and re-registering the device...");

    try {
      Files.deleteIfExists(Paths.get(refreshTokenKeyStorePath));
    } catch (IOException e) {
      LOG.error("The JKS file with the Refresh Token was not found at that location. {}", e.getMessage());
    }
  }

  /**
   * Refresh the oauth token
   *
   * @return token in bytes
   */
  @Override
  public byte[] refreshToken() {
    String refreshToken = getDecodedRefreshToken(); // get refreshToken from JKS
    if (StringUtils.isBlank(refreshToken)) {
      deviceRegistration();
    } else {
      final String clientId = connectionProperties.getClientId();
      oauthTokenInfo = authClient.oauthRefreshToken(REFRESH_TOKEN, clientId, refreshToken);
      boolean isRotatable = ROTATABLE.equalsIgnoreCase(connectionProperties.getRefreshType());
      if (isRotatable && !Objects.equals(oauthTokenInfo.getRefreshToken(), refreshToken)) {
        encodeAndSetRefreshToken(oauthTokenInfo.getRefreshToken());
      }
      final String accessToken = oauthTokenInfo.getAccessToken();
      authTokenExpiresAt = System.currentTimeMillis() + oauthTokenInfo.getExpiresIn().longValue() * 1000;
      authToken = accessToken.getBytes();
    }
    return authToken;
  }

  /**
   * Get refresh token from oauth 2
   *
   * @return {@link OauthTokenInfo}
   */
  public OauthTokenInfo getAuthToken() {
    final DeviceCodeInfo deviceCodeResp = registerDevice();

    final OauthTokenInfo tokenInfo = getAuthTokenFromServer(deviceCodeResp.getDeviceCode());
    if (StringUtils.isNotBlank(tokenInfo.getRefreshToken())) {
      encodeAndSetRefreshToken(tokenInfo.getRefreshToken());
    }

    return tokenInfo;
  }

  private DeviceCodeInfo registerDevice() {
    final DeviceCodeInfo deviceCodeResp = authClient.deviceCode(DEVICE_SCOPE, connectionProperties.getClientId());
    LOG.info("Please activate device using following url: {}", deviceCodeResp.getVerificationUriComplete());
    LOG.info("User code: {}", deviceCodeResp.getUserCode());
    return deviceCodeResp;
  }

  private OauthTokenInfo getAuthTokenFromServer(final String deviceCode) {
    OauthTokenInfo oauthTokenResp = null;
    int retry = 0;

    synchronized (this) {
      while (retry < 30) {
        retry++;
        try {
          oauthTokenResp = authClient.oauthToken(GRANT_TYPE, deviceCode,
              connectionProperties.getClientId());
          if (null == oauthTokenResp) {
            throw new AnaplanAPIException("Null response from API received");
          }
          break;
        } catch (final RuntimeException exception) {
          LOG.error("{} attempt. User has yet to authorize the device code in browser.", retry);
          try {
            Thread.sleep(connectionProperties.getRetryTimeout() * 1000L);
          } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            LOG.error(interruptedException.getMessage());
          }
        }
      }
    }

    if (oauthTokenResp == null) {
      throw new AnaplanAPIException(String.format("Application terminated. User did not authorize the device after %d attempts.", retry));
    }
    return oauthTokenResp;
  }

  private void deviceRegistration() {
    LOG.info("Registering device...");
    oauthTokenInfo = getAuthToken();
    String accessToken = oauthTokenInfo.getAccessToken();
    authToken = accessToken.getBytes();
    if (oauthTokenInfo.getExpiresIn() != null) {
      authTokenExpiresAt =
          System.currentTimeMillis() + oauthTokenInfo.getExpiresIn().longValue() * 1000;
    }
    connectionProperties.setForceRegister(false);
  }

  private void encodeAndSetRefreshToken(String refreshToken) {
    int attempts = 0;
    byte[] encodedKey = Base64.getEncoder().encode(refreshToken.getBytes());
    do {
      attempts++;
      try (FileOutputStream fileOutputStream = new FileOutputStream(refreshTokenKeyStorePath)) {
        KeyStore ks = Utils.saveEntryInKeyStore(encodedKey, refreshTokenKeyStoreName, keystorePass);
        ks.store(fileOutputStream, keystorePass);
        return;
      } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
        LOG.error("Could not encode the refresh token or the token does not exist. {}", e.getMessage());
      }
    } while (attempts < 10);
    throw new AnaplanAPIException("Unable to save refresh token after " + attempts + " attempts.");
  }

  private String getDecodedRefreshToken() {
    try (FileInputStream fileInputStream = new FileInputStream(refreshTokenKeyStorePath)) {
      Key secretKeyAlias = Utils.loadKeystore(fileInputStream, refreshTokenKeyStoreName, keystorePass);

      byte[] rawData = secretKeyAlias.getEncoded();
      byte[] decodedKey = Base64.getDecoder().decode(rawData);
      return new String(decodedKey);
    } catch (FileNotFoundException e) {
      LOG.error("Refresh token is not set for your client id.");
    } catch (Exception e) {
      LOG.error("Could not decode the refresh token or the token does not exist. {}", e.getMessage());
    }
    return StringUtils.EMPTY;
  }

  public OauthTokenInfo getOauthTokenInfo() {
    return this.oauthTokenInfo;
  }
}
