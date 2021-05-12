package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.ConnectionProperties;

/**
 * Created by Spondon Saha User: spondonsaha Date: 12/13/17 Time: 1:46 PM
 */
//TODO: move to tests
class MockCertificateAuthenticator extends CertificateAuthenticator {

  private final AnaplanAuthenticationAPI mockClient;

  MockCertificateAuthenticator(ConnectionProperties properties,
      AnaplanAuthenticationAPI mockClient) {
    super(properties, mockClient);
    this.mockClient = mockClient;
  }

  public AnaplanAuthenticationAPI getAuthClient() {
    return mockClient;
  }
}
