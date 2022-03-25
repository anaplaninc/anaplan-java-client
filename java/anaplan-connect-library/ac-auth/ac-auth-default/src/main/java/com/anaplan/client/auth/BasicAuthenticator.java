package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.dto.responses.AuthenticationResp;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.transport.ConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Spondon Saha User: spondonsaha Date: 12/11/17 Time: 5:57 PM
 */
public class BasicAuthenticator extends AbstractAuthenticator {

  private static final Logger LOG = LoggerFactory.getLogger(BasicAuthenticator.class);

  public BasicAuthenticator(ConnectionProperties connectionProperties,
      AnaplanAuthenticationAPI authClient) {
    super(connectionProperties, authClient);
  }

  @Override
  public byte[] authenticate() {
    LOG.info("Authenticating via Basic...");
    try {
      Credentials apiCredentials = connectionProperties.getApiCredentials();
      //TODO: compute this using java core so we do not depend on ok http here
      String basic = okhttp3.Credentials
          .basic(apiCredentials.getUserName(), apiCredentials.getPassPhrase());
      AuthenticationResp authResponse = authClient.authenticateBasic(basic);
      authTokenExpiresAt = authResponse.getItem().getExpiresAt();
      return authResponse.getItem().getTokenValue().getBytes();
    } catch (Exception e) {
      throw new AnaplanAPIException("Basic Authentication failed!", e);
    }
  }
}
