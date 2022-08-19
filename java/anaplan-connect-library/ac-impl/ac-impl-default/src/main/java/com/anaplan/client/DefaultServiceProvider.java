package com.anaplan.client;

import com.anaplan.client.auth.Authenticator;
import com.anaplan.client.auth.AuthenticatorFactoryUtil;
import com.anaplan.client.auth.DeviceAuthenticator;
import com.anaplan.client.auth.UnknownAuthenticationException;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.client.OkHttpFeignClientProvider;
import feign.Client;
import java.util.function.Supplier;

public class DefaultServiceProvider {

  private DefaultServiceProvider(){}

  public static Service getService(ConnectionProperties properties)
      throws UnknownAuthenticationException {

    OkHttpFeignClientProvider okHttpClientProvider = new OkHttpFeignClientProvider();
    Supplier<Client> clientSupplier = () -> okHttpClientProvider.createFeignClient(properties);

    FeignAuthenticationAPIProvider authApiProvider = new FeignAuthenticationAPIProvider(properties,
        clientSupplier);
    Authenticator authenticator = AuthenticatorFactoryUtil
        .getAuthenticator(properties, authApiProvider.getAuthClient());

    AnaplanApiProviderImpl apiProvider = new AnaplanApiProviderImpl(properties, clientSupplier, authenticator);

    return new Service(properties, authenticator, apiProvider);
  }

  /**
   * Get the authenticator based on properties
   * @param properties the connection properties
   * @return {@link DeviceAuthenticator}
   */
  public static DeviceAuthenticator getDeviceAuthenticator(ConnectionProperties properties) {
    OkHttpFeignClientProvider okHttpClientProvider = new OkHttpFeignClientProvider();
    Supplier<Client> clientSupplier = () -> okHttpClientProvider.createFeignClient(properties);

    FeignAuthenticationAPIProvider authApiProvider = new FeignAuthenticationAPIProvider(properties,
        clientSupplier);
    return new DeviceAuthenticator(properties, authApiProvider.getAuthClient());
  }
}
