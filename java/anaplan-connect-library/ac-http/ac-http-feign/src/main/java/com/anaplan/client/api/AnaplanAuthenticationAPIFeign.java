package com.anaplan.client.api;

import com.anaplan.client.dto.DeviceCodeInfo;
import com.anaplan.client.dto.OauthTokenInfo;
import com.anaplan.client.dto.responses.AuthenticationResp;
import com.anaplan.client.dto.responses.RefreshTokenResp;
import com.anaplan.client.dto.responses.ValidationTokenResp;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * Relevant Anaplan Auth-API
 */
@Headers({"Content-Type: application/json"})
public interface AnaplanAuthenticationAPIFeign extends AnaplanAuthenticationAPI{

  @RequestLine("GET /token/validate")
  @Headers({"AUTHORIZATION: AnaplanAuthToken {token}"})
  ValidationTokenResp validateToken(@Param("token") String token);

  @RequestLine("POST /token/authenticate")
  AuthenticationResp authenticateBasic(@Param("username") String username, @Param("password") String password);

  @RequestLine("POST /token/authenticate")
  @Headers({"AUTHORIZATION: {basichash}"})
  AuthenticationResp authenticateBasic(@Param("basichash") String basicHash);

  @RequestLine("POST /token/authenticate")
  @Headers({"AUTHORIZATION: CACertificate {certhash}"})
  @Body("{cert_nonce_verification_data}")
  AuthenticationResp authenticateCertificate(@Param("certhash") String certificateHash,
      @Param("cert_nonce_verification_data") String certNonceVerificationData);

  @RequestLine("POST /token/refresh")
  @Headers({"AUTHORIZATION: AnaplanAuthToken {token}"})
  RefreshTokenResp refreshToken(@Param("token") String token);

  @RequestLine("POST /token/logout")
  @Headers({"AUTHORIZATION: AnaplanAuthToken {token}"})
  void logout(@Param("token") String token);

  @RequestLine("POST /oauth/device/code")
  DeviceCodeInfo deviceCode(@Param("scope") String scope, @Param("client_id") String clientId);

  @RequestLine("POST /oauth/token")
  OauthTokenInfo oauthToken(
      @Param("grant_type") String grantType,
      @Param("device_code") String deviceCode,
      @Param("client_id") String clientId);

  @RequestLine("POST /oauth/token")
  OauthTokenInfo oauthRefreshToken(
      @Param("grant_type") String grantType,
      @Param("client_id") String clientId,
      @Param("refresh_token") String refreshToken);
}
