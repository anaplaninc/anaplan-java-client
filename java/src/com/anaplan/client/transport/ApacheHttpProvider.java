//   Copyright 2011 Anaplan Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.anaplan.client.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.UnknownHostException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.anaplan.client.Credentials;

/**
  * TransportProvider implementation using Apache HttpComponents client library.
  */

public class ApacheHttpProvider extends HttpProvider implements TransportProvider {
    public class ApacheCredentialsProvider implements CredentialsProvider {
        @Override
        public void clear() {
        }
        @Override
        public org.apache.http.auth.Credentials getCredentials(AuthScope authScope) {
            try {
                InetAddress authAddress = InetAddress.getByName(authScope.getHost());
                URI serviceLocation = getServiceLocation();
                InetAddress serviceAddress = InetAddress.getByName(serviceLocation.getHost());
                URI proxyLocation = getProxyLocation();
                if (serviceAddress.equals(authAddress)
                && (    authScope.getPort() == AuthScope.ANY_PORT
                    ||  authScope.getPort() == serviceLocation.getPort())) {
                    Credentials serviceCredentials = getServiceCredentials();
                    if (serviceCredentials == null) {
                        return null;
                    }
                    return new UsernamePasswordCredentials(
                                serviceCredentials.getUserName(),
                                serviceCredentials.getPassphrase());
                } else if (proxyLocation != null) {
                    InetAddress proxyAddress = InetAddress.getByName(proxyLocation.getHost());
                    if (proxyAddress.equals(authAddress)
                    && (    authScope.getPort() == AuthScope.ANY_PORT
                        ||  authScope.getPort() == proxyLocation.getPort())) {
                        Credentials proxyCredentials = getProxyCredentials();
                        if (proxyCredentials == null) {
                            return null;
                        } else if ("ntlm".equalsIgnoreCase(authScope.getScheme())) {
                            return new NTCredentials(proxyCredentials.getUserName(), proxyCredentials.getPassphrase(), proxyCredentials.getWorkstation(), proxyCredentials.getDomain());
                        } else {
                            return new UsernamePasswordCredentials(
                                        proxyCredentials.getUserName(),
                                        proxyCredentials.getPassphrase());
                        }
                    }
                }
            } catch (UnknownHostException unknownHostException) {
                if (getDebugLevel() >= 1) {
                    unknownHostException.printStackTrace();
                }
                throw new IllegalArgumentException(unknownHostException.getMessage());
            }
            return null;
        }
        @Override
        public void setCredentials(AuthScope authScope, org.apache.http.auth.Credentials credentials) {
        }
    }


    private DefaultHttpClient httpClient;

    private HttpHost httpHost;

    private HttpContext httpContext;

    private ProxySelectorRoutePlanner routePlanner;

    // Use TransportProviderFactory to instantiate
    protected ApacheHttpProvider() {
        super();
        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(ClientPNames.HANDLE_AUTHENTICATION,
               Boolean.TRUE);
        httpClient.setCredentialsProvider(new ApacheCredentialsProvider());
        try {
            Class<?> engineClass = Class.forName("com.anaplan.client.transport.JCIFSEngine");
            final NTLMEngine ntlmEngine = (NTLMEngine) engineClass.newInstance();
            
            httpClient.getAuthSchemes().register("ntlm", new AuthSchemeFactory() {
                    public AuthScheme newInstance(final HttpParams params) {
                        return new NTLMScheme(ntlmEngine);
                    }
                });
        } catch (InstantiationException instantiationException) {
            // Normal - the jcifs jar file is not present in the lib folder.
        } catch (Throwable thrown) {
            // Abnormal
            thrown.printStackTrace();
        }
        routePlanner = new ProxySelectorRoutePlanner(
                        httpClient.getConnectionManager().getSchemeRegistry(),
                        getProxySelector()) {
            private boolean suppressed;
            @Override
            public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
                HttpRoute httpRoute = super.determineRoute(target, request, context);
                if (getDebugLevel() >= 1 && !suppressed) {
                    System.err.println(httpRoute.toString() + " ("
                            + getProxySelector().getClass().getSimpleName() + ")");
                    if (getDebugLevel() == 1) suppressed = true;
                }
                return httpRoute;
            }
        };
        httpClient.setRoutePlanner(routePlanner);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String className = getClass().getName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /** {@inheritDoc} */
    @Override
    public void setServiceLocation(URI serviceLocation)
            throws AnaplanAPITransportException {

        super.setServiceLocation(serviceLocation);

        httpHost = URIUtils.extractHost(serviceLocation);
        httpContext = new BasicHttpContext();
        if (getServiceCredentials() != null) {
            // Set up the authorization again
            setServiceCredentials(getServiceCredentials());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setServiceCredentials(Credentials serviceCredentials)
            throws AnaplanAPITransportException {

        super.setServiceCredentials(serviceCredentials);

        if (getServiceLocation() != null) {
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicScheme = new BasicScheme();
            authCache.put(httpHost, basicScheme);
            httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setProxyLocation(URI proxyLocation)
            throws AnaplanAPITransportException {
        super.setProxyLocation(proxyLocation);
        // Update the route planner
        routePlanner.setProxySelector(getProxySelector());
    }

    /** {@inheritDoc} */
    @Override
    public void setUserAgent(String userAgent) {
        super.setUserAgent(userAgent);
        HttpProtocolParams.setUserAgent(httpClient.getParams(), userAgent);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] get(String path, String acceptType)
            throws AnaplanAPITransportException {
        try {
            HttpGet httpGet = new HttpGet(getRequestPath(path));
            if (acceptType != null) {
                httpGet.setHeader(HttpHeaders.ACCEPT, acceptType);
            }
            if (getDebugLevel() >= 1) {
                System.err.println(httpGet.getRequestLine().toString());
            }

            HttpResponse httpResponse
                    = httpClient.execute(httpHost, httpGet, httpContext);
            checkResponse(httpResponse);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity != null) {
                return readResponseBody(httpEntity.getContent());
            } else {
                throw new AnaplanAPITransportException(
                        getMessage(MSG_NO_CONTENT), null);
            }
        } catch (IOException ioException) {
            throw new AnaplanAPITransportException(
                    getMessage(MSG_COMMS_FAILURE),
                    ioException);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean head(String path) throws AnaplanAPITransportException {
        try {
            HttpHead httpHead = new HttpHead(getRequestPath(path));
            if (getDebugLevel() >= 1) {
                System.err.println(httpHead.getRequestLine().toString());
            }

            HttpResponse httpResponse
                    = httpClient.execute(httpHost, httpHead, httpContext);
            HttpEntity httpEntity = httpResponse.getEntity();
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (200 <= statusCode && statusCode <= 299) {
                if (httpEntity != null) {
                    EntityUtils.consume(httpEntity);
                }
                return true;
            } else if (statusCode == 404) {
                if (httpEntity != null) {
                    EntityUtils.consume(httpEntity);
                }
                return false;
            }
            checkResponse(httpResponse);
            // Dead code - checkResponse should have thrown an exception
            return false;

        } catch (IOException ioException) {
            throw new AnaplanAPITransportException(
                    getMessage(MSG_COMMS_FAILURE),
                    ioException);
        }
    }

    /** {@inheritDoc} */
    @Override
    public byte[] post(String path, byte[] content, String contentType,
            String acceptType) throws AnaplanAPITransportException {
        try {
            HttpPost httpPost = new HttpPost(getRequestPath(path));
            if (contentType != null) {
                httpPost.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
            }
            if (acceptType != null) {
                httpPost.setHeader(HttpHeaders.ACCEPT, acceptType);
            }
            if (getDebugLevel() >= 1) {
                System.err.println(httpPost.getRequestLine().toString());
            }
            httpPost.setEntity(new ByteArrayEntity(content));

            HttpResponse httpResponse
                    = httpClient.execute(httpHost, httpPost, httpContext);
            checkResponse(httpResponse);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity != null) {
                return readResponseBody(httpEntity.getContent());
            }
            return null;
        } catch (IOException ioException) {
            throw new AnaplanAPITransportException(
                    getMessage(MSG_COMMS_FAILURE),
                    ioException);
        }
    }

    /** {@inheritDoc} */
    @Override
    public byte[] put(String path, byte[] content, String contentType)
            throws AnaplanAPITransportException {
        try {
            final String binaryContent = "application/octet-stream";
            HttpPut httpPut = new HttpPut(getRequestPath(path));
            if (contentType == null) contentType = binaryContent;
            if (contentType.equals(binaryContent)) {
                contentType = "application/x-gzip";
                content = compress(content, 0, content.length);
            }
            httpPut.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
            if (getDebugLevel() >= 1) {
                System.err.println(httpPut.getRequestLine().toString());
            }
            httpPut.setEntity(new ByteArrayEntity(content));

            HttpResponse httpResponse = httpClient.execute(
                    httpHost, httpPut, httpContext);
            checkResponse(httpResponse);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity != null) {
                return readResponseBody(httpEntity.getContent());
            }
            return null;
        } catch (IOException ioException) {
            throw new AnaplanAPITransportException(
                    getMessage(MSG_COMMS_FAILURE),
                    ioException);
        }
    }

    /** {@inheritDoc} */
    @Override
    public byte[] delete(String path, String acceptType) throws AnaplanAPITransportException {
        try {
            HttpDelete httpDelete = new HttpDelete(getRequestPath(path));
            if (acceptType != null) {
                httpDelete.setHeader(HttpHeaders.ACCEPT, acceptType);
            }
            if (getDebugLevel() >= 1) {
                System.err.println(httpDelete.getRequestLine().toString());
            }

            HttpResponse httpResponse = httpClient.execute(httpHost, httpDelete, httpContext);
            checkResponse(httpResponse);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity != null) {
                return readResponseBody(httpEntity.getContent());
            } else {
                throw new AnaplanAPITransportException(
                        getMessage(MSG_NO_CONTENT), null);
            }
        } catch (IOException ioException) {
            throw new AnaplanAPITransportException(
                    getMessage(MSG_COMMS_FAILURE),
                    ioException);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        httpClient.getConnectionManager().shutdown();
    }

    protected void checkResponse(HttpResponse httpResponse) throws AnaplanAPITransportException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (!(200 <= statusCode && statusCode <= 299)) {
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                try {
                    Header contentTypeHeader = httpEntity.getContentType();
                    String entityContent = EntityUtils.toString(httpEntity);
                    if (!entityContent.isEmpty() && contentTypeHeader != null
                    &&  contentTypeHeader.getElements().length > 0
                    &&  contentTypeHeader.getElements()[0].getName().equals("text/plain")) {
                        throw new AnaplanAPITransportException(entityContent);
                    }

                    if (getDebugLevel() >= 1) {
                        System.err.println(httpResponse.getStatusLine().toString());
                    }
                    if (getDebugLevel() >= 2) {
                        System.err.println(entityContent);
                    }
                } catch (IOException ioException) {
                    if (getDebugLevel() >= 1) {
                        ioException.printStackTrace();
                    }
                }
            }
            throw new AnaplanAPITransportException(getStatusMessage(statusCode, 
                    httpResponse.getStatusLine().getReasonPhrase()));
        }
    }
}
