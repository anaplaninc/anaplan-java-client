package com.anaplan.client.transport.retryer;

import com.anaplan.client.ex.AnaplanAPIException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

public class AnaplanErrorDecoder implements ErrorDecoder {

    /**
     * Overwrite the Decode Method to handle custom error cases
     *
     * @param methodKey Method Key
     * @param response Response
     * @return Exception
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        if (status >= 500) {
            String message = "HTTP "+response.status()+" "+response.reason()+": " + response.request().url();
            return new RetryableException(
                    message,
                    response.request().httpMethod(),
                    null
            );
        }
        return new AnaplanAPIException(response.reason());
    }

}
