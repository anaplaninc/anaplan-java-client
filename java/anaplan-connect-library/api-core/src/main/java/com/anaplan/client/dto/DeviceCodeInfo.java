package com.anaplan.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class DeviceCodeInfo implements Serializable {

  @JsonProperty("device_code")
  private String deviceCode;
  @JsonProperty("user_code")
  private String userCode;
  @JsonProperty("verification_uri")
  private String verificationUri;
  @JsonProperty("expires_in")
  private Integer expiresIn;
  @JsonProperty("interval")
  private Integer interval;
  @JsonProperty("verification_uri_complete")
  private String verificationUriComplete;

  public String getDeviceCode() {
    return deviceCode;
  }

  public void setDeviceCode(String deviceCode) {
    this.deviceCode = deviceCode;
  }

  public String getUserCode() {
    return userCode;
  }

  public void setUserCode(String userCode) {
    this.userCode = userCode;
  }

  public String getVerificationUri() {
    return verificationUri;
  }

  public void setVerificationUri(String verificationUri) {
    this.verificationUri = verificationUri;
  }

  public Integer getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Integer expiresIn) {
    this.expiresIn = expiresIn;
  }

  public Integer getInterval() {
    return interval;
  }

  public void setInterval(Integer interval) {
    this.interval = interval;
  }

  public String getVerificationUriComplete() {
    return verificationUriComplete;
  }

  public void setVerificationUriComplete(String verificationUriComplete) {
    this.verificationUriComplete = verificationUriComplete;
  }
}
