package com.anaplan.client.api;

import com.anaplan.client.dto.DeviceCodeInfo;
import com.anaplan.client.dto.OauthTokenInfo;
import com.anaplan.client.dto.responses.AuthenticationResp;
import com.anaplan.client.dto.responses.DeviceCodeResp;
import com.anaplan.client.dto.responses.OauthTokenResp;
import com.anaplan.client.dto.responses.RefreshTokenResp;
import com.anaplan.client.dto.responses.ValidationTokenResp;

/**
 * Relevant Anaplan Auth-API
 */
public interface AnaplanAuthenticationAPI {

  /**
   * Validate the token
   * @param token the token
   * @return {@link ValidationTokenResp}
   */
  ValidationTokenResp validateToken(String token);

  /**
   * Authenticate with credentials
   * @param username the user identificator
   * @param password the password
   * @return {@link AuthenticationResp}
   */
  AuthenticationResp authenticateBasic(String username, String password);

  /**
   * Authenticate with token
   * @param basicHash the token
   * @return {@link AuthenticationResp}
   */
  AuthenticationResp authenticateBasic(String basicHash);

  /**
   * Authenticate with certificate
   * @param certificateHash the certification
   * @param certNonceVerificationData cert_nonce_verification_data parameter
   * @return {@link AuthenticationResp}
   */
  AuthenticationResp authenticateCertificate(String certificateHash,
      String certNonceVerificationData);

  /**
   * Refresh token
   * @param token the token
   * @return {@link RefreshTokenResp}
   */
  RefreshTokenResp refreshToken(String token);

  /**
   * Log out
   * @param token the token
   */
  void logout(String token);

  /**
   * Device Code Response
   * @param scope the scope request
   * @param clientId the client's id
   * @return {@link DeviceCodeInfo}
   */
  DeviceCodeInfo deviceCode(String scope, String clientId);

  /**
   * OAuth token Response
   * @param grantType the grant type
   * @param deviceCode the device code
   * @param clientId the client's id
   * @return {@link OauthTokenInfo}
   */
  OauthTokenInfo oauthToken(String grantType, String deviceCode, String clientId);

  /**
   * OAuth Refresh token Response
   * @param grantType the grant type
   * @param clientId the client's id
   * @param refreshToken the refresh token
   * @return {@link OauthTokenInfo}
   */
  OauthTokenInfo oauthRefreshToken(String grantType, String clientId, String refreshToken);
}
