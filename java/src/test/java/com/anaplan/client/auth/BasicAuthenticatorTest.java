package com.anaplan.client.auth;

import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.BaseTest;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.dto.responses.AuthenticationResp;
import com.anaplan.client.dto.responses.RefreshTokenResp;
import com.anaplan.client.transport.ConnectionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jcifs.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/2/17
 * Time: 3:54 PM
 */
public class BasicAuthenticatorTest extends BaseTest {

    private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
    private final Long mockAuthTokenExpiresAt = System.currentTimeMillis()+300001;
    private final String mockAuthToken = "authentication-token";
    private final String mockRefreshToken = "refresh-token-value";
    private AbstractAuthenticator basicAuth;
    private AnaplanAuthenticationAPI mockAuthApi;
    private String mockUsername = "someusername";
    private String mockPassword = "asdasdasdsa";
    private String basicHash = "Basic " + Base64.encode((mockUsername + ":" + mockPassword).getBytes());
    private ObjectMapper objectMapper = new ObjectMapper();

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
        AuthenticationResp authenticationResp = objectMapper.readValue(getFixture("responses/auth_response.json"), AuthenticationResp.class);
        doReturn(authenticationResp)
                .when(mockAuthApi)
                .authenticateBasic(basicHash);
        assertEquals(mockAuthToken, basicAuth.getAuthToken());
    }

    @Test(expected = AnaplanAPIException.class)
    public void testAuthFailed() {
        Mockito.doThrow(new MockFeignException("asdasdsa"))
                .when(mockAuthApi)
                .authenticateBasic(basicHash);
        basicAuth.getAuthToken();
    }

    @Test
    public void testRefreshToken() throws IOException {
        RefreshTokenResp refreshTokenResp = objectMapper.readValue(getFixture("responses/auth_refresh_token.json"), RefreshTokenResp.class);
        doReturn(refreshTokenResp)
                .when(mockAuthApi)
                .refreshToken(mockAuthToken);
        basicAuth.setAuthToken(mockAuthToken);
        basicAuth.setAuthTokenExpiresAt(mockAuthTokenExpiresAt);
        assertEquals(mockRefreshToken, basicAuth.getAuthToken());
    }

    @Test(expected = AnaplanAPIException.class)
    public void testRefreshTokenFailed() throws IOException {
        RefreshTokenResp refreshTokenResp = objectMapper.readValue(getFixture("responses/auth_refresh_token.json"), RefreshTokenResp.class);
        doReturn(refreshTokenResp)
                .when(mockAuthApi)
                .refreshToken(mockAuthToken);
        basicAuth.setAuthToken(mockAuthToken);
        basicAuth.setAuthTokenExpiresAt(mockAuthTokenExpiresAt);
        Mockito.doThrow(new MockFeignException("asdasds"))
                .when(mockAuthApi)
                .refreshToken(mockAuthToken);
        basicAuth.getAuthToken();
    }
}
