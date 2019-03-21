package com.anaplan.client.api;

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
public interface AnaplanAuthenticationAPI {

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
    AuthenticationResp authenticateCertificate(@Param("certhash") String certificateHash, @Param("cert_nonce_verification_data") String certNonceVerificationData);

    @RequestLine("POST /token/refresh")
    @Headers({"AUTHORIZATION: AnaplanAuthToken {token}"})
    RefreshTokenResp refreshToken(@Param("token") String token);

    @RequestLine("POST /token/logout")
    @Headers({"AUTHORIZATION: AnaplanAuthToken {token}"})
    void logout(@Param("token") String token);
}
