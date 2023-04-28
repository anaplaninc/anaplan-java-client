package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.dto.responses.RefreshTokenResp;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.transport.ConnectionProperties;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAuthenticator implements Authenticator {

  private static final Logger LOG = LoggerFactory
      .getLogger(AbstractAuthenticator.class.getName());

  private static final int TOKEN_EXPIRATION_REFRESH_WINDOW = 5 * 60 * 1000;
  private static final int TOKEN_EXPIRED_WINDOW = 60 * 1000;

  protected ConnectionProperties connectionProperties;
  protected AnaplanAuthenticationAPI authClient;

  protected Long authTokenExpiresAt;
  protected byte[] authToken;

  protected AbstractAuthenticator(ConnectionProperties connectionProperties,
      AnaplanAuthenticationAPI authClient) {
    this.connectionProperties = connectionProperties;
    this.authClient = authClient;
  }

  /**
   * Fetches auth token from Anaplan Auth Service, checks to see if its expired and accordingly
   * fetches a fresh new token or refreshes the existing token, exactly 1 minute before it expires,
   * as defined by TOKEN_EXPIRED_WINDOW.
   *
   * @return AuthenticationResp
   */
  @Override
  public String authToken() {
    if (authToken == null || authTokenExpiresAt == null
        || System.currentTimeMillis() - authTokenExpiresAt > TOKEN_EXPIRED_WINDOW) {
      authToken = authenticate();
    } else if (authTokenExpiresAt - System.currentTimeMillis() < TOKEN_EXPIRATION_REFRESH_WINDOW) {
      authToken = refreshToken();
    }

    return new String(authToken);
  }

  byte[] refreshToken() {
    LOG.info("Refreshing auth token...");
    try {
      RefreshTokenResp refreshTokenResp = authClient.refreshToken(new String(authToken));
      authTokenExpiresAt = refreshTokenResp.getItem().getExpiresAt();
      return refreshTokenResp.getItem().getTokenValue().getBytes();
    } catch (Exception e) {
      throw new AnaplanAPIException("Token Refresh failed!", e);
    }
  }

  public void setAuthToken(byte[] authToken){
    this.authToken = authToken;
  }

}
