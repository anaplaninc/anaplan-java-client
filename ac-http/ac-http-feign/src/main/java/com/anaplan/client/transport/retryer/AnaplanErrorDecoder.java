package com.anaplan.client.transport.retryer;

import com.anaplan.client.exceptions.AnaplanAPIException;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnaplanErrorDecoder implements ErrorDecoder {

  private static final Logger LOG = LoggerFactory.getLogger(AnaplanErrorDecoder.class);

  /**
   * Overwrite the Decode Method to handle custom error cases
   *
   * @param methodKey Method Key
   * @param response  Response
   * @return Exception
   */
  @Override
  public Exception decode(String methodKey, Response response) {
    int status = response.status();
    String message = null;
    if (status >= 500) {
      message = "HTTP " + response.status() + " " + response.reason() + ": " + response.request().url();
      return new RetryableException(message, response.request().httpMethod(), null);
    }
    //Handling Anaplan API response for 400 BAD REQUEST
    if (status == 400 && response.body() != null) {
      try {
        message = Util.toString(response.body().asReader());
        if (message != null) {
          return new AnaplanAPIException(message);
        }
      } catch (IOException e) {
        LOG.error("Error Deserializing response body from failed feign request response.", e);
      }
    }
    return new AnaplanAPIException(response.reason());
  }
}
