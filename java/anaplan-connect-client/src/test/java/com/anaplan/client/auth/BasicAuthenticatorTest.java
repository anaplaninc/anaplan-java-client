package com.anaplan.client.auth;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.anaplan.client.BaseTest;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.dto.responses.AuthenticationResp;
import com.anaplan.client.dto.responses.RefreshTokenResp;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.transport.ConnectionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import jcifs.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by Spondon Saha User: spondonsaha Date: 6/2/17 Time: 3:54 PM
 */
public class BasicAuthenticatorTest extends BaseTest {

  private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
  private final Long mockAuthTokenExpiresAt = System.currentTimeMillis() + 300001;
  private final String mockAuthToken = "authentication-token";
  private final String mockRefreshToken = "refresh-token-value";
  private final String mockUsername = "someusername";
  private final String mockPassword = "asdasdasdsa";
  private final String basicHash =
      "Basic " + Base64.encode((mockUsername + ":" + mockPassword).getBytes());
  private final ObjectMapper objectMapper = new ObjectMapper();
  private AbstractAuthenticator basicAuth;
  private AnaplanAuthenticationAPI mockAuthApi;

  @Before
  public void setUp() throws Exception {
    mockAuthApi = Mockito.mock(AnaplanAuthenticationAPI.class);
    ConnectionProperties props = new ConnectionProperties();
    props.setApiCredentials(new Credentials(mockUsername, mockPassword));
    props.setAuthServiceUri(new URI(mockAuthServiceUrl));
    basicAuth = Mockito.spy(new MockBasicAuthenticator(props, mockAuthApi));
  }

  @After
  public void tearDown() {
    Mockito.reset(mockAuthApi);
  }

  @Test
  public void testGetAuthTokenNew() throws IOException {
    AuthenticationResp authenticationResp = objectMapper
        .readValue(getFixture("responses/auth_response.json"), AuthenticationResp.class);
    doReturn(authenticationResp)
        .when(mockAuthApi)
        .authenticateBasic(basicHash);
    assertEquals(mockAuthToken, new String(basicAuth.authenticate(), "utf-8"));
  }

  @Test(expected = AnaplanAPIException.class)
  public void testAuthFailed() {
    when(mockAuthApi.authenticateBasic(basicHash))
        .thenThrow(new RuntimeException("asdasdsa"));
    basicAuth.authenticate();
  }

  @Test
  public void testRefreshToken() throws IOException {
    RefreshTokenResp refreshTokenResp = objectMapper
        .readValue(getFixture("responses/auth_refresh_token.json"), RefreshTokenResp.class);
    doReturn(refreshTokenResp)
        .when(mockAuthApi)
        .refreshToken(mockAuthToken);
    ((MockBasicAuthenticator)basicAuth).setAuthToken(mockAuthToken);
    ((MockBasicAuthenticator)basicAuth).setAuthTokenExpiresAt(mockAuthTokenExpiresAt);
    assertEquals(mockRefreshToken, basicAuth.authToken());
  }

  @Test(expected = AnaplanAPIException.class)
  public void testRefreshTokenFailed() throws IOException {
    RefreshTokenResp refreshTokenResp = objectMapper
        .readValue(getFixture("responses/auth_refresh_token.json"), RefreshTokenResp.class);
    doReturn(refreshTokenResp)
        .when(mockAuthApi)
        .refreshToken(mockAuthToken);
    ((MockBasicAuthenticator)basicAuth).setAuthToken(mockAuthToken);
    ((MockBasicAuthenticator)basicAuth).setAuthTokenExpiresAt(mockAuthTokenExpiresAt);
    Mockito.doThrow(new RuntimeException("asdasds"))
        .when(mockAuthApi)
        .refreshToken(mockAuthToken);
    basicAuth.authenticate();
  }
}
