package com.anaplan.client.transport;

import com.anaplan.client.Constants;
import com.anaplan.client.DeviceTypeToken;
import com.anaplan.client.auth.Credentials;
import java.net.URI;

/**
 * Created by Spondon Saha Date: 4/17/18 Time: 3:39 PM
 */
public class ConnectionProperties {

  private URI apiServicesUri;
  private URI authServiceUri;
  private Credentials apiCredentials;
  private URI proxyLocation;
  private Credentials proxyCredentials;
  private int retryTimeout;
  private int maxRetryCount;
  private int httpTimeout;
  private String clientId;
  private String refreshType = DeviceTypeToken.NON_ROTATABLE.name();
  private boolean forceRegister;
  private String major = Constants.TWO;
  private String minor = Constants.ZERO;

  public String getMajor() {
    return major;
  }

  public void setMajor(String major) {
    this.major = major;
  }

  public String getMinor() {
    return minor;
  }

  public void setMinor(String minor) {
    this.minor = minor;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getRefreshType() {
    return refreshType;
  }

  public void setRefreshType(String refreshType) {
    this.refreshType = refreshType;
  }

  public boolean isForceRegister() {
    return forceRegister;
  }

  public void setForceRegister(boolean forceRegister) {
    this.forceRegister = forceRegister;
  }

  public URI getApiServicesUri() {
    return apiServicesUri;
  }

  public void setApiServicesUri(URI apiServicesUri) {
    this.apiServicesUri = apiServicesUri;
  }

  public URI getAuthServiceUri() {
    return authServiceUri;
  }

  public void setAuthServiceUri(URI authServiceUri) {
    this.authServiceUri = authServiceUri;
  }

  public Credentials getApiCredentials() {
    return apiCredentials;
  }

  public void setApiCredentials(Credentials apiCredentials) {
    this.apiCredentials = apiCredentials;
  }

  public URI getProxyLocation() {
    return proxyLocation;
  }

  public void setProxyLocation(URI proxyLocation) {
    this.proxyLocation = proxyLocation;
  }

  public Credentials getProxyCredentials() {
    return proxyCredentials;
  }

  public void setProxyCredentials(Credentials proxyCredentials) {
    this.proxyCredentials = proxyCredentials;
  }

  public int getRetryTimeout() {
    return retryTimeout;
  }

  public void setRetryTimeout(int retryTimeout) {
    this.retryTimeout = retryTimeout;
  }

  public int getMaxRetryCount() {
    return maxRetryCount;
  }

  public void setMaxRetryCount(int maxRetryCount) {
    this.maxRetryCount = maxRetryCount;
  }

  public int getHttpTimeout() {
    return httpTimeout;
  }

  public void setHttpTimeout(int httpTimeout) {
    this.httpTimeout = httpTimeout;
  }
}
