package com.anaplan.client.auth;

import com.anaplan.client.dto.responses.AuthenticationResp;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.transport.ConnectionProperties;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 12/11/17
 * Time: 5:57 PM
 */
public class BasicAuthenticator extends AbstractAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(BasicAuthenticator.class);

    BasicAuthenticator(ConnectionProperties properties) {
        super(properties);
    }

    @Override
    public byte[] authenticate() {
        LOG.info("Authenticating via Basic...");
        try {
            AuthenticationResp authResponse = getAuthClient()
                    .authenticateBasic(okhttp3.Credentials.basic(getCredentials().getUserName(), getCredentials().getPassPhrase()));
            setAuthTokenExpiresAt(authResponse.getItem().getExpiresAt());
            return authResponse.getItem().getTokenValue().getBytes();
        } catch (FeignException e) {
            throw new AnaplanAPIException("Basic Authentication failed!", e);
        }
    }
}
