package com.anaplan.client.auth;

import com.anaplan.client.transport.ConnectionProperties;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 12/12/17
 * Time: 1:17 AM
 */
public class AuthenticatorFactory {

    public static Authenticator getAuthenticator(ConnectionProperties properties) {
        switch (properties.getApiCredentials().getScheme()) {
            case BASIC:
            case NTLM:
                return new BasicAuthenticator(properties);
            case CA_CERTIFICATE:
                return new CertificateAuthenticator(properties);
            default:
                throw new RuntimeException("Unknown authentication scheme: " + properties.getApiCredentials().getScheme());
        }
    }
}
