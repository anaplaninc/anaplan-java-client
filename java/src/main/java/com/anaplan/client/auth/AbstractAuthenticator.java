package com.anaplan.client.auth;

import com.anaplan.client.Constants;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.dto.responses.RefreshTokenResp;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.transport.AnaplanApiProvider;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.retryer.FeignApiRetryer;
import com.anaplan.client.transport.interceptors.AConnectHeaderInjector;
import feign.Feign;
import feign.FeignException;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/1/17
 * Time: 5:29 PM
 */
public abstract class AbstractAuthenticator extends AnaplanApiProvider implements Authenticator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAuthenticator.class.getName());
    private static final int TOKEN_EXPIRATION_REFRESH_WINDOW = 5 * 60 * 1000;
    private static final int TOKEN_EXPIRED_WINDOW =  60 * 1000;
    private AnaplanAuthenticationAPI authClient;
    private byte[] authToken;
    private Long authTokenExpiresAt;
    private ConnectionProperties properties;

    AbstractAuthenticator(ConnectionProperties connectionProperties) {
        super(connectionProperties, null);
        this.properties = connectionProperties;
    }

    /**
     * Fetches auth token from Anaplan Auth Service, checks to see if its expired
     * and accordingly fetches a fresh new token or refreshes the existing token, exactly
     * 1 minute before it expires, as defined by TOKEN_EXPIRED_WINDOW.
     *
     * @return AuthenticationResp
     */
    @Override
    public String getAuthToken() {
        if (authToken == null || authTokenExpiresAt == null || System.currentTimeMillis() - authTokenExpiresAt > TOKEN_EXPIRED_WINDOW) {
            authToken = authenticate();
        } else if (authTokenExpiresAt - System.currentTimeMillis() < TOKEN_EXPIRATION_REFRESH_WINDOW) {
            authToken = refreshToken();
        }
        return new String(authToken);
    }

    @Override
    public AnaplanAuthenticationAPI getAuthClient() {
        if (authClient == null) {
            authClient = Feign.builder()
                    .client(createFeignClient())
                    .encoder(new JacksonEncoder())
                    .decoder(new JacksonDecoder())
                    .requestInterceptor(new AConnectHeaderInjector())
                    .retryer(new FeignApiRetryer(
                            (long) (properties.getRetryTimeout() * 1000),
                            (long) Constants.MAX_RETRY_TIMEOUT_SECS * 1000,
                            properties.getMaxRetryCount(),
                            FeignApiRetryer.DEFAULT_BACKOFF_MULTIPLIER))
                    .target(AnaplanAuthenticationAPI.class, properties.getAuthServiceUri().toString());
        }
        return authClient;
    }

    @Override
    public void setAuthClient(AnaplanAuthenticationAPI authClient) {
        this.authClient = authClient;
    }

    public void setAuthTokenExpiresAt(Long authTokenExpiresAt) {
        this.authTokenExpiresAt = authTokenExpiresAt;
    }

    public Long getAuthTokenExpiresAt() {
        return authTokenExpiresAt;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken.getBytes();
    }

    private byte[] refreshToken() {
        LOG.info("Refreshing auth token...");
        try {
            RefreshTokenResp refreshTokenResp = getAuthClient().refreshToken(new String(authToken));
            authTokenExpiresAt = refreshTokenResp.getItem().getExpiresAt();
            return refreshTokenResp.getItem().getTokenValue().getBytes();
        } catch (FeignException e) {
            throw new AnaplanAPIException("Token Refresh failed!", e);
        }
    }
}
