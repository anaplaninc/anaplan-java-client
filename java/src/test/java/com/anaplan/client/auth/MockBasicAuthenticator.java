package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.ConnectionProperties;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 9/19/17
 * Time: 10:56 AM
 */
class MockBasicAuthenticator extends BasicAuthenticator {
    private AnaplanAuthenticationAPI mockApi;

    MockBasicAuthenticator(ConnectionProperties properties, AnaplanAuthenticationAPI mockApi) {
        super(properties);
        this.mockApi = mockApi;
    }

    @Override
    public AnaplanAuthenticationAPI getAuthClient() {
        return mockApi;
    }
}
