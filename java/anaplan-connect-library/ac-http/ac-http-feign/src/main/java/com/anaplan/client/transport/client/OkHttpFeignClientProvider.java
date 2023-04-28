package com.anaplan.client.transport.client;

import com.anaplan.client.auth.Credentials;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.NtlmAuthenticator;
import feign.Client;
import feign.okhttp.OkHttpClient;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OkHttpFeignClientProvider implements FeignClientProvider {

  private static final Logger LOG = LoggerFactory.getLogger(OkHttpFeignClientProvider.class);

  private static final String PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization";

  /**
   * Creates a Feign/OkHttp client for speaking to Auth-Service and sets up the appropriate proxy
   * handler.
   *
   * @return A Feign/OkHttp client
   */
  @Override
  public Client createFeignClient(ConnectionProperties properties) {
    okhttp3.OkHttpClient.Builder okHttpBuilder = new okhttp3.OkHttpClient.Builder();
    if (properties.getProxyLocation() != null) {
      LOG.info("Setting up proxy...");
      setupProxy(okHttpBuilder, properties);
    }
    // setting the read and write timeouts as well
    int timeoutHTTP = Optional.ofNullable(properties.getHttpTimeout()).orElse(0);
    okHttpBuilder.connectTimeout(timeoutHTTP, TimeUnit.SECONDS)
        .readTimeout(timeoutHTTP, TimeUnit.SECONDS)
        .writeTimeout(timeoutHTTP, TimeUnit.SECONDS);
    return new OkHttpClient(okHttpBuilder.build());
  }

  /**
   * Sets up an NTLM proxy or a regular proxy based on credential types.
   *
   * @param okHttpBuilder {@link okhttp3.OkHttpClient.Builder}
   */
  private void setupProxy(okhttp3.OkHttpClient.Builder okHttpBuilder,
      ConnectionProperties properties) {
    Credentials proxyCredentials = properties.getProxyCredentials();

    okHttpBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
        properties.getProxyLocation().getHost(),
        properties.getProxyLocation().getPort())));

    if (proxyCredentials != null) {
      if (proxyCredentials.isNtlm()) {
        okHttpBuilder
            .proxyAuthenticator(new NtlmAuthenticator(
                proxyCredentials.getUserName(),
                proxyCredentials.getPassPhrase(),
                proxyCredentials.getDomain(),
                proxyCredentials.getWorkstation()));
      } else {
        okHttpBuilder
            .proxyAuthenticator((route, response) -> response.request().newBuilder()
                .header("Connection", "close")
                .header(PROXY_AUTHORIZATION_HEADER, okhttp3.Credentials.basic(
                    proxyCredentials.getUserName(),
                    proxyCredentials.getPassPhrase()))
                .build());
      }
    }
  }

}
