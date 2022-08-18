package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.ConnectionProperties;

/**
 * Created by Spondon Saha User: spondonsaha Date: 12/12/17 Time: 1:17 AM
 */
public class AuthenticatorFactoryUtil {

  private AuthenticatorFactoryUtil() {}

  public static Authenticator getAuthenticator(ConnectionProperties properties,
      AnaplanAuthenticationAPI authClient) throws UnknownAuthenticationException {
    switch (properties.getApiCredentials().getScheme()) {
      case BASIC:
      case NTLM:
        return new BasicAuthenticator(properties, authClient);
      case CA_CERTIFICATE:
        return new CertificateAuthenticator(properties, authClient);
      case DEVICE:
        return new DeviceAuthenticator(properties, authClient);
      default:
        throw new UnknownAuthenticationException(
            "Unknown authentication scheme: " + properties.getApiCredentials().getScheme());
    }
  }

}
