package com.anaplan.client.transport.interceptors;

import com.anaplan.client.Constants;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Injects the X-AConnect-Client header
 */
public class AConnectHeaderInjector implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        template.header(Constants.X_ACONNECT_HEADER_KEY, Constants.X_ACONNECT_HEADER_VALUE);
    }
}
