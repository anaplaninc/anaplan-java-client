package com.anaplan.client.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;

import com.anaplan.client.BaseTest;
import com.anaplan.client.Constants;
import com.anaplan.client.FeignAuthenticationAPIProvider;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.ConnectionProperties;
import feign.Client;
import feign.Request;
import feign.Request.Options;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Created by Spondon Saha User: spondonsaha Date: 2/13/18 Time: 3:24 AM
 */
public class AuthRetryTest extends BaseTest {

  private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
  private final String mockUsername = "someusername";
  private final String mockPassword = "asdasdasdsa";
  private final Long AuthTokenExpiresAt = System.currentTimeMillis() + 300001;
  @Mock
  private Client mockClient;
  private FeignAuthenticationAPIProvider feignAuthenticationAPIProvider;
  private AnaplanAuthenticationAPI authClient;
  private BasicAuthenticator basicAuthenticator;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    ConnectionProperties properties = new ConnectionProperties();
    properties.setApiCredentials(new Credentials(mockUsername, mockPassword));
    properties.setAuthServiceUri(new URI(mockAuthServiceUrl));
    properties.setMaxRetryCount(Constants.MIN_RETRY_COUNT);
    properties.setRetryTimeout(Constants.MIN_RETRY_TIMEOUT_SECS);
    properties.setHttpTimeout(Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS);
    Supplier<Client> clientSupplier = () -> mockClient;
    feignAuthenticationAPIProvider = new FeignAuthenticationAPIProvider(properties, clientSupplier);
    authClient = feignAuthenticationAPIProvider.getAuthClient();
    basicAuthenticator = new BasicAuthenticator(properties, authClient);
  }

  public void testRetry(String message) throws IOException {
    try {
      basicAuthenticator.authToken();
      fail("Not failing");
    } catch (Exception e) {
      assertEquals(message, e.getCause().getMessage());
    } finally {
      Mockito.verify(mockClient, Mockito.times(4))
          .execute(Mockito.any(Request.class), Mockito.any(Options.class));
    }
  }

  @Test
  public void testGetAuthTokenRetryAndFail() throws IOException {
    doThrow(new UnknownHostException())
        .when(mockClient).execute(Mockito.any(Request.class), Mockito.any(Options.class));
    testRetry("null executing POST http://mock-auth.anaplan.com/token/authenticate");
  }

  @Test
  public void testRefreshAuthTokenRetryAndFail() throws IOException {
    doThrow(new UnknownHostException())
        .when(mockClient).execute(Mockito.any(Request.class), Mockito.any(Options.class));
    basicAuthenticator.authToken = "aaaaa".getBytes();
    basicAuthenticator.authTokenExpiresAt = AuthTokenExpiresAt;
    testRetry("null executing POST http://mock-auth.anaplan.com/token/refresh");
  }
}
