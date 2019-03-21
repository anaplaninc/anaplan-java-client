package com.anaplan.client.auth;

import com.anaplan.client.transport.ConnectionProperties;
import feign.Client;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 2/13/18
 * Time: 4:07 PM
 */
public class MockRetryBasicAuthenticator extends BasicAuthenticator {

    private Client mockClient;

    MockRetryBasicAuthenticator(ConnectionProperties properties) {
        super(properties);
    }

    @Override
    protected Client createFeignClient() {
        return mockClient;
    }

    public Client getMockClient() {
        return mockClient;
    }

    public void setMockClient(Client mockClient) {
        this.mockClient = mockClient;
    }
}
