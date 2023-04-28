package com.anaplan.client.transport.retryer;

import com.anaplan.client.Constants;
import com.anaplan.client.Utils;
import com.anaplan.client.auth.Authenticator;
import com.anaplan.client.exceptions.AnaplanAPIException;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnaplanErrorDecoder implements ErrorDecoder {

  private static final Logger LOG = LoggerFactory.getLogger(AnaplanErrorDecoder.class);
  private Authenticator authenticator;

  public AnaplanErrorDecoder(Authenticator authenticator){
    this.authenticator = authenticator;
  }

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
    String message;
    Collection<String> retryAfter = response.headers().get(Constants.RETRY_AFTER);
    Date retryDate = null;
    if (CollectionUtils.isNotEmpty(retryAfter)) {
      retryDate = Utils.getDateFromRetryAfter(retryAfter.iterator().next());
    }
    if (status >= 500 || status == 429) {
      message = "HTTP " + response.status() + " " + response.reason() + ": " + response.request().url();
      if (status == 429 && retryDate != null) {
        LOG.info("API request rate limited, trying again after {} seconds",
            (retryDate.getTime() - new Date().getTime()) / 1000);
      }
      return new RetryableException(message, response.request().httpMethod(), retryDate);
    }
    //Handling Anaplan API response for 400 BAD REQUEST
    if (status == 400 && response.body() != null) {
      AnaplanAPIException exceptionMessage = getAnaplanAPIException(response);
      if (exceptionMessage != null) return exceptionMessage;
    } else if (status == 401 && response.body() != null) {
      if (authenticator == null) {
        AnaplanAPIException exceptionMessage = getAnaplanAPIException(response);
        if (exceptionMessage != null) return exceptionMessage;
      }
      authenticator.setAuthToken(authenticator.authenticate());
      message = "HTTP " + response.status() + " " + response.reason() + ": " + response.request().url();
      return new RetryableException(message, response.request().httpMethod(), retryDate);
    }
    return new AnaplanAPIException(response.reason(), status);
  }


  private static AnaplanAPIException getAnaplanAPIException(Response response) {
    String message;
    try {
      message = Util.toString(response.body().asReader());
      if (message != null) {
        return new AnaplanAPIException(message);
      }
    } catch (IOException e) {
      LOG.error("Error Deserializing response body from failed feign request response.", e);
    }
    return null;
  }
}
