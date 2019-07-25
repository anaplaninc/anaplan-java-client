package com.anaplan.client.auth;

import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.BaseTest;
import com.anaplan.client.Constants;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.ConnectionProperties;
import feign.Client;
import feign.Request;
import feign.Request.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 2/13/18
 * Time: 3:24 AM
 */
public class AuthRetryTest extends BaseTest {

    @Mock
    private Client mockClient;

    private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
    private final String mockUsername = "someusername";
    private final String mockPassword = "asdasdasdsa";
    private final Long AuthTokenExpiresAt = System.currentTimeMillis()+300001;
    private AnaplanAuthenticationAPI mockAuthApi;
    private MockRetryBasicAuthenticator basicAuth;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockAuthApi = Mockito.mock(AnaplanAuthenticationAPI.class);
        ConnectionProperties properties = new ConnectionProperties();
        properties.setApiCredentials(new Credentials(mockUsername, mockPassword));
        properties.setAuthServiceUri(new URI(mockAuthServiceUrl));
        properties.setMaxRetryCount(Constants.MIN_RETRY_COUNT);
        properties.setRetryTimeout(Constants.MIN_RETRY_TIMEOUT_SECS);
        properties.setHttpTimeout(Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS);
        basicAuth = Mockito.spy(new MockRetryBasicAuthenticator(properties));
    }

    @After
    public void tearDown() {
        Mockito.reset(mockAuthApi);
    }

    public void testRetry(String message) throws IOException {
        try {
            basicAuth.getAuthToken();
            fail("Not failing");
        } catch (AnaplanAPIException e) {
            assertEquals(e.getCause().getMessage(), message);
        } finally {
            Mockito.verify(mockClient, Mockito.times(4))
                    .execute(Mockito.any(Request.class), Mockito.any(Options.class));
        }
    }

    @Test
    public void testGetAuthTokenRetryAndFail() throws IOException {
        basicAuth.setMockClient(mockClient);
        doThrow(new UnknownHostException())
                .when(mockClient).execute(Mockito.any(Request.class), Mockito.any(Options.class));
        testRetry("null executing POST http://mock-auth.anaplan.com/token/authenticate");
    }

    @Test
    public void testRefreshAuthTokenRetryAndFail() throws IOException {
        basicAuth.setMockClient(mockClient);
        doThrow(new UnknownHostException())
                .when(mockClient).execute(Mockito.any(Request.class), Mockito.any(Options.class));
        basicAuth.setAuthToken("asdasdsa");
        basicAuth.setAuthTokenExpiresAt(AuthTokenExpiresAt);
        testRetry("null executing POST http://mock-auth.anaplan.com/token/refresh");
    }
}
