package com.anaplan.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class OauthTokenInfo implements Serializable {
  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("id_token")
  private String idToken;
  @JsonProperty("refresh_token")
  private String refreshToken;
  @JsonProperty("scope")
  private String scope;
  @JsonProperty("expires_in")
  private Integer expiresIn;
  @JsonProperty("token_type")
  private String tokenType;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getIdToken() {
    return idToken;
  }

  public void setIdToken(String idToken) {
    this.idToken = idToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public Integer getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Integer expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }
}
