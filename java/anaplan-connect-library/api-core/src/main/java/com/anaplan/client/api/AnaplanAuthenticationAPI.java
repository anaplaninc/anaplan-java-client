package com.anaplan.client.api;

import com.anaplan.client.dto.responses.AuthenticationResp;
import com.anaplan.client.dto.responses.RefreshTokenResp;
import com.anaplan.client.dto.responses.ValidationTokenResp;

/**
 * Relevant Anaplan Auth-API
 */
public interface AnaplanAuthenticationAPI {

  ValidationTokenResp validateToken(String token);

  AuthenticationResp authenticateBasic(String username, String password);

  AuthenticationResp authenticateBasic(String basicHash);

  AuthenticationResp authenticateCertificate(String certificateHash,
      String certNonceVerificationData);

  RefreshTokenResp refreshToken(String token);

  void logout(String token);
}
