package com.anaplan.client.transport;

import com.anaplan.client.auth.Credentials;

import java.net.URI;

/**
 * Created by Spondon Saha
 * Date: 4/17/18
 * Time: 3:39 PM
 */
public class ConnectionProperties {
    private URI apiServicesUri;
    private URI authServiceUri;
    private Credentials apiCredentials;
    private URI proxyLocation;
    private Credentials proxyCredentials;
    private int retryTimeout;
    private int maxRetryCount;
    private int httpTimeout;

    public URI getApiServicesUri() {
        return apiServicesUri;
    }

    public void setApiServicesUri(URI apiServicesUri) {
        this.apiServicesUri = apiServicesUri;
    }

    public URI getAuthServiceUri() {
        return authServiceUri;
    }

    public void setAuthServiceUri(URI authServiceUri) {
        this.authServiceUri = authServiceUri;
    }

    public Credentials getApiCredentials() {
        return apiCredentials;
    }

    public void setApiCredentials(Credentials apiCredentials) {
        this.apiCredentials = apiCredentials;
    }

    public URI getProxyLocation() {
        return proxyLocation;
    }

    public void setProxyLocation(URI proxyLocation) {
        this.proxyLocation = proxyLocation;
    }

    public Credentials getProxyCredentials() {
        return proxyCredentials;
    }

    public void setProxyCredentials(Credentials proxyCredentials) {
        this.proxyCredentials = proxyCredentials;
    }

    public int getRetryTimeout() {
        return retryTimeout;
    }

    public void setRetryTimeout(int retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public int getHttpTimeout() {
        return httpTimeout;
    }

    public void setHttpTimeout(int httpTimeout) {
        this.httpTimeout = httpTimeout;
    }
}
