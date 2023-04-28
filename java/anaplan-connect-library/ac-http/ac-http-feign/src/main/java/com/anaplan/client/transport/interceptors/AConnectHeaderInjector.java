package com.anaplan.client.transport.interceptors;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Injects the X-AConnect-Client header
 */
public class AConnectHeaderInjector implements RequestInterceptor {

  private String clientKey;
  private String clientValue;

  /**
   * Constructor
   * @param headerKey the client key
   * @param headerValue the client value
   */
  public AConnectHeaderInjector(final String headerKey, final String headerValue) {
    this.clientKey = headerKey;
    this.clientValue = headerValue;
  }

  @Override
  public void apply(RequestTemplate template) {
    template.header(clientKey, clientValue);

  }
}
