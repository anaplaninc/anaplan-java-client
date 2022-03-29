package com.anaplan.client.api;

import com.anaplan.client.dto.responses.AuthenticationResp;
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
}
