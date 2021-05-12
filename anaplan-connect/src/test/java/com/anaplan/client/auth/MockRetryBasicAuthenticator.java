package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.ConnectionProperties;
import feign.Client;

/**
 * Created by Spondon Saha User: spondonsaha Date: 2/13/18 Time: 4:07 PM
 */
//TODO: move to tests
public class MockRetryBasicAuthenticator extends BasicAuthenticator {

  private Client mockClient;

  MockRetryBasicAuthenticator(ConnectionProperties properties,
      AnaplanAuthenticationAPI authApi) {
    super(properties, authApi);
  }

  public Client createClient() {
    return mockClient;
  }

  public Client getMockClient() {
    return mockClient;
  }

  public void setMockClient(Client mockClient) {
    this.mockClient = mockClient;
  }

  public void setAuthToken(String authToken){
    this.authToken=authToken.getBytes();
  }

  public void setAuthTokenExpiresAt(Long authTokenExpiresAt) {
    this.authTokenExpiresAt = authTokenExpiresAt;
  }

}
