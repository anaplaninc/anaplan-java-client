package com.anaplan.client;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.api.AnaplanAuthenticationAPIFeign;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.interceptors.AConnectHeaderInjector;
import com.anaplan.client.transport.retryer.FeignApiRetryer;
import feign.Client;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.util.function.Supplier;

/**
 * Provides an AnaplanAuthenticationAPI implementation that internally uses feign
 */
public class FeignAuthenticationAPIProvider {

  protected ConnectionProperties connectionProperties;
  private final Supplier<Client> clientSupplier;
  private AnaplanAuthenticationAPI authClient;
  private String clientKey;
  private String clientValue;

  /**
   * Initializes API Provider with parameter
   * @param connectionProperties
   * @param clientSupplier
   */
  public FeignAuthenticationAPIProvider(ConnectionProperties connectionProperties,
      Supplier<Client> clientSupplier, String clientKey, String clientValue) {
    this.connectionProperties = connectionProperties;
    this.clientSupplier = clientSupplier;
    this.clientKey = clientKey;
    this.clientValue = clientValue;
  }

  /**
   * Provides the Client Authentication API
   * @return {@link AnaplanAuthenticationAPI}
   */
  public AnaplanAuthenticationAPI getAuthClient() {
    if (authClient == null) {
      authClient = Feign.builder()
          .client(clientSupplier.get())
          .encoder(new JacksonEncoder())
          .decoder(new JacksonDecoder())
          .requestInterceptor(new AConnectHeaderInjector(clientKey, clientValue))
          .retryer(new FeignApiRetryer(
              connectionProperties.getRetryTimeout() == null ? null : connectionProperties.getRetryTimeout() * 1000L,
              Constants.MAX_RETRY_TIMEOUT_SECS * 1000L,
              connectionProperties.getMaxRetryCount(),
              FeignApiRetryer.DEFAULT_BACKOFF_MULTIPLIER))
          .target(AnaplanAuthenticationAPIFeign.class,
              connectionProperties.getAuthServiceUri().toString());
    }
    return authClient;
  }

  public void setAuthClient(AnaplanAuthenticationAPI authClient) {
    this.authClient = authClient;
  }
}
