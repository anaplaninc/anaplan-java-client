package com.anaplan.client.transport.interceptors;

import com.anaplan.client.auth.Authenticator;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Injects the auth-token in every request, whose life-cycle is managed by Authenticator
 */
public class AuthTokenInjector implements RequestInterceptor {

    private Authenticator authenticator;

    public AuthTokenInjector(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Injects the Auth-Token from the Auth APIs for every request made to the API services
     *
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        template.header("AUTHORIZATION", "AnaplanAuthToken " + authenticator.getAuthToken());
    }
}
