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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an AnaplanAuthenticationAPI implementation that internally uses feign
 */
public class FeignAuthenticationAPIProvider {

  private static final Logger LOG = LoggerFactory
      .getLogger(FeignAuthenticationAPIProvider.class.getName());

  protected ConnectionProperties connectionProperties;
  private Supplier<Client> clientSupplier;
  private AnaplanAuthenticationAPI authClient;

  public FeignAuthenticationAPIProvider(ConnectionProperties connectionProperties,
      Supplier<Client> clientSupplier) {
    this.connectionProperties = connectionProperties;
    this.clientSupplier = clientSupplier;
  }

  public AnaplanAuthenticationAPI getAuthClient() {
    if (authClient == null) {
      authClient = Feign.builder()
          .client(clientSupplier.get())
          .encoder(new JacksonEncoder())
          .decoder(new JacksonDecoder())
          .requestInterceptor(new AConnectHeaderInjector())
          .retryer(new FeignApiRetryer(
              (long) (connectionProperties.getRetryTimeout() * 1000),
              (long) Constants.MAX_RETRY_TIMEOUT_SECS * 1000,
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
