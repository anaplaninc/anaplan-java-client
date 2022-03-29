package com.anaplan.client.transport.client;

import com.anaplan.client.transport.ConnectionProperties;
import feign.Client;

/**
 * Interface for providing a Feign client
 * This abstraction allows to change the configured types of client libs
 */
public interface FeignClientProvider {

  Client createFeignClient(ConnectionProperties properties);

}
