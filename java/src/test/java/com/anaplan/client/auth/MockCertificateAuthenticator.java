package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.ConnectionProperties;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 12/13/17
 * Time: 1:46 PM
 */
class MockCertificateAuthenticator extends CertificateAuthenticator {

    private AnaplanAuthenticationAPI mockFeignClient;

    MockCertificateAuthenticator(ConnectionProperties properties, AnaplanAuthenticationAPI mockFeignClient) {
        super(properties);
        this.mockFeignClient = mockFeignClient;
    }

    @Override
    public AnaplanAuthenticationAPI getAuthClient() {
        return mockFeignClient;
    }
}
