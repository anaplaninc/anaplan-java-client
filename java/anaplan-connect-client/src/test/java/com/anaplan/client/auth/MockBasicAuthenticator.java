package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.ConnectionProperties;

/**
 * Created by Spondon Saha User: spondonsaha Date: 9/19/17 Time: 10:56 AM
 */
//TODO: move to tests
public class MockBasicAuthenticator extends BasicAuthenticator {

  private final AnaplanAuthenticationAPI mockClient;

  MockBasicAuthenticator(ConnectionProperties properties, AnaplanAuthenticationAPI mockClient) {
    super(properties, mockClient);
    this.mockClient = mockClient;
  }

  public AnaplanAuthenticationAPI getAuthClient() {
    return mockClient;
  }

  public void setAuthToken(String authToken){
    this.authToken=authToken.getBytes();
  }

  public void setAuthTokenExpiresAt(Long authTokenExpiresAt) {
    this.authTokenExpiresAt = authTokenExpiresAt;
  }
}
