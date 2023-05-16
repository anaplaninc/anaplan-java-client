package com.anaplan.client;

import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.api.AnaplanAPIFeign;
import com.anaplan.client.auth.Authenticator;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.decoders.AnaplanApiDecoder;
import com.anaplan.client.transport.encoders.AnaplanApiEncoder;
import com.anaplan.client.transport.interceptors.AConnectHeaderInjector;
import com.anaplan.client.transport.interceptors.AuthTokenInjector;
import com.anaplan.client.transport.interceptors.CompressPutBodyInjector;
import com.anaplan.client.transport.interceptors.UserAgentInjector;
import com.anaplan.client.transport.retryer.AnaplanErrorDecoder;
import com.anaplan.client.transport.retryer.FeignApiRetryer;
import feign.Client;
import feign.Feign;
import feign.Request;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Manages the Anaplan API connection with the help of Feign and Jackson encoder/decoders. Raw bytes from downloading
 * file-chunks or uploading raw bytes of file-chunks, are managed by their respective AnaplanByteArraySerializer and
 * AnaplanByteArrayDeserializer classes. If proxy details are provided, then tunnels the API connection through it using
 * either regular or NTLM proxy.
 */
public class AnaplanApiProviderImpl implements Supplier<AnaplanAPI> {

  private final Authenticator authenticator;
  private ConnectionProperties connectionProperties;
  private Supplier<Client> clientSupplier;
  private AnaplanAPI apiClient;
  private String clientKey;
  private String clientValue;

  public AnaplanApiProviderImpl(ConnectionProperties connectionProperties,
      Supplier<Client> clientSupplier, Authenticator authenticator, String clientKey, String clientValue) {
    this.connectionProperties = connectionProperties;
    this.clientSupplier = clientSupplier;
    this.authenticator = authenticator;
    this.clientKey = clientKey;
    this.clientValue = clientValue;
  }

  /**
   * Generates the Feign client for communicating with Anaplan APIs
   *
   * @return AnaplanAPi
   */
  @Override
  public AnaplanAPI get() {
    if (apiClient == null) {
      int httpTimeout = connectionProperties.getHttpTimeout() == null ? 0 : connectionProperties.getHttpTimeout() * 1000;
      apiClient = Feign.builder()
          .client(clientSupplier.get())
          .encoder(new AnaplanApiEncoder(ObjectMapperProvider.getObjectMapper()))
          .decoder(new AnaplanApiDecoder(ObjectMapperProvider.getObjectMapper()))
          .requestInterceptors(Arrays.asList(
              new AuthTokenInjector(authenticator),
              new UserAgentInjector(),
              new AConnectHeaderInjector(clientKey, clientValue),
              new CompressPutBodyInjector()))
          .options(new Request.Options(
              httpTimeout,
              httpTimeout
          ))
          .retryer(new FeignApiRetryer(
              connectionProperties.getRetryTimeout() == null ? null : connectionProperties.getRetryTimeout() * 1000L,
              Constants.MAX_RETRY_TIMEOUT_SECS * 1000L,
              connectionProperties.getMaxRetryCount(),
              FeignApiRetryer.DEFAULT_BACKOFF_MULTIPLIER))
          .errorDecoder(new AnaplanErrorDecoder(authenticator))
          .target(AnaplanAPIFeign.class,
              connectionProperties.getApiServicesUri().toString() + "/" + connectionProperties.getMajor() + "/"
                  + connectionProperties.getMinor());
    }
    return apiClient;
  }

  public void setApiClient(AnaplanAPI apiClient) {
    this.apiClient = apiClient;
  }

}
