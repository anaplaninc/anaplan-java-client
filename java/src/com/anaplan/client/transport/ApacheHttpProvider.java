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
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.anaplan.client.Credentials;
import com.anaplan.client.Credentials.Scheme;
import com.anaplan.client.transport.AnaplanAPITransportException;
import com.anaplan.client.transport.HttpProvider;
import com.anaplan.client.transport.TransportProvider;

/**
 * TransportProvider implementation using Apache HttpComponents client library.
 */

public class ApacheHttpProvider extends HttpProvider implements
        TransportProvider {

    private static final String RFC_2617_AUTHORISATION_SCHEME = "AnaplanCertificate";

    private static final String CHARACTER_ENCODING = "UTF-8";

    private static final int[] NONFATAL_STATUS_CODES = {
        HttpStatus.SC_REQUEST_TIMEOUT,
        HttpStatus.SC_CONFLICT,
        HttpStatus.SC_LOCKED,
        HttpStatus.SC_INTERNAL_SERVER_ERROR,
        HttpStatus.SC_BAD_GATEWAY,
        HttpStatus.SC_SERVICE_UNAVAILABLE,
        HttpStatus.SC_GATEWAY_TIMEOUT
    };
    static {
        Arrays.sort(NONFATAL_STATUS_CODES);
    }

    private static final Logger logger = Logger
            .getLogger("anaplan-connect.transport");

    public class ApacheCredentialsProvider implements CredentialsProvider {

        @Override
        public void clear() {
        }

        @Override
        public org.apache.http.auth.Credentials getCredentials(
                AuthScope authScope) {
            try {
                InetAddress authAddress = InetAddress.getByName(authScope
                        .getHost());
                InetAddress serviceAddress = InetAddress
                        .getByName(httpHost.getHostName());
                URI proxyLocation = getProxyLocation();
                if (serviceAddress.equals(authAddress)
                        && (authScope.getPort() == AuthScope.ANY_PORT || authScope
                                .getPort() == httpHost.getPort())) {
                    Credentials serviceCredentials = getServiceCredentials();
                    if (serviceCredentials == null) {
                        return null;
                    }
                    return new UsernamePasswordCredentials(
                            serviceCredentials.getUserName(),
                            serviceCredentials.getPassphrase());
                } else if (proxyLocation != null) {
                    InetAddress proxyAddress = InetAddress
                            .getByName(proxyLocation.getHost());
                    int proxyPort = getPort(proxyLocation);
                    if (proxyAddress.equals(authAddress)
                            && (authScope.getPort() == AuthScope.ANY_PORT || authScope
                                    .getPort() == proxyPort)) {
                        Credentials proxyCredentials = getProxyCredentials();
                        if (proxyCredentials == null) {
                            return null;
                        } else if ("ntlm".equalsIgnoreCase(authScope
                                .getScheme())) {
                            return new NTCredentials(
                                    proxyCredentials.getUserName(),
                                    proxyCredentials.getPassphrase(),
                                    proxyCredentials.getWorkstation(),
                                    proxyCredentials.getDomain());
                        } else {
                            return new UsernamePasswordCredentials(
                                    proxyCredentials.getUserName(),
                                    proxyCredentials.getPassphrase());
                        }
                    }
                }
            } catch (UnknownHostException unknownHostException) {
                if (getDebugLevel() >= 1) {
                    logger.log(Level.WARNING, "Failed to resolve hostname for authentication", unknownHostException);
                }
                throw new IllegalArgumentException(
                        unknownHostException.getMessage());
            }
            return null;
        }

        @Override
        public void setCredentials(AuthScope authScope,
                org.apache.http.auth.Credentials credentials) {
        }
    }

    private HttpClient httpClient;

    private HttpHost httpHost;

    private HttpContext httpContext;

    private ProxySelectorRoutePlanner routePlanner;

    /**
     * The value of the HTTP Authorization header for certificate-based
     * authentication. Will be <code>null</code> if an alternative
     * authentication scheme is used.
     */
    private String certificateAuthorizationHeader;

    private int idempotentRequestAttempts = 3;

    private int retryDelay = 3000;

    // Use TransportProviderFactory to instantiate
    protected ApacheHttpProvider() {
        super();
        idempotentRequestAttempts = getIntegerSystemProperty("anaplan.client.http.attempts", 3, 1, 10);
        retryDelay = getIntegerSystemProperty("anaplan.client.http.retryDelay", 3000, 0, 3600000);
    }
    
    private static int getIntegerSystemProperty(String name, int defaultValue, int min, int max) {
        try {
            String property = System.getProperty(name);
            if (null != property) {
                int parsed = Integer.parseInt(property);
                if (!(min <= parsed && parsed <= max)) {
                    throw new IllegalArgumentException("Value must be in range " + min + ".." + max);
                }
                return parsed;
            }
        } catch (Throwable thrown) {
            logger.log(Level.WARNING, "Ignoring invalid " + name + " value" + thrown);
        }
        return defaultValue;
    }

    protected HttpClient createHttpClient() {
        return new DefaultHttpClient();
    }

    /**
     * Performs once-only initialisation
     * 
     * @param serviceCredentials
     *            the credentials used
     * @since 1.3.2
     */
    private void initialise(Credentials serviceCredentials)
            throws AnaplanAPITransportException {
        httpClient = createHttpClient();
        if (serviceCredentials.getScheme() == Scheme.USER_CERTIFICATE) {
            try {
                X509Certificate certificate = serviceCredentials
                        .getCertificate();
                String certificateCommonName = extractCommonName(certificate);
                String rawAuthenticationParameter = certificateCommonName + ":"
                        + convertToPEMFormat(certificate);
                byte[] encodedAuthenticationParameter = Base64
                        .encodeBase64(rawAuthenticationParameter
                                .getBytes(CHARACTER_ENCODING));
                certificateAuthorizationHeader = RFC_2617_AUTHORISATION_SCHEME
                        + " "
                        + new String(encodedAuthenticationParameter,
                                CHARACTER_ENCODING);
            } catch (Throwable thrown) {
                throw new AnaplanAPITransportException(
                        "Could not initialise transport given provided certificate credentials",
                        thrown);
            }
        } else if (httpClient instanceof AbstractHttpClient) {
            AbstractHttpClient httpClientImpl = (AbstractHttpClient) httpClient;
            httpClientImpl.getParams().setParameter(
                    ClientPNames.HANDLE_AUTHENTICATION, Boolean.TRUE);
            httpClientImpl.setCredentialsProvider(new ApacheCredentialsProvider());
            try {
                final NTLMEngine ntlmEngine = new JCIFSEngine();

                httpClientImpl.getAuthSchemes().register("ntlm",
                        new AuthSchemeFactory() {
                            public AuthScheme newInstance(
                                    final HttpParams params) {
                                return new NTLMScheme(ntlmEngine);
                            }
                        });
            } catch (InvocationTargetException invocationTargetException) {
                // Normal - the jcifs jar file is not present in the lib folder.
                if (getDebugLevel() >= 2) {
                    logger.log(Level.INFO,
                            "Not using JCIFS NTLM proxy authentication",
                            invocationTargetException.getCause());
                } else if (getDebugLevel() >= 1) {
                    logger.log(Level.INFO,
                            "Not using JCIFS NTLM proxy authentication due to "
                            + invocationTargetException.getCause()
                            + " (add jcifs.jar to lib folder if required)");
                }
            } catch (Throwable thrown) {
                // Abnormal
                if (getDebugLevel() >= 1) {
                    logger.log(Level.WARNING, "Failed to set up JCIFS NTLM proxy authentication", thrown);
                }
            }
        }

        if (httpClient instanceof AbstractHttpClient) {
            routePlanner = new ProxySelectorRoutePlanner(httpClient
                    .getConnectionManager().getSchemeRegistry(), getProxySelector()) {
                private boolean suppressed;

                @Override
                public HttpRoute determineRoute(HttpHost target,
                        HttpRequest request, HttpContext context)
                        throws HttpException {
                    HttpRoute httpRoute = super.determineRoute(target, request,
                            context);
                    if (getDebugLevel() >= 1 && !suppressed) {
                        logger.info(httpRoute.toString() + " ("
                                + getProxySelector().getClass().getSimpleName()
                                + ")");
                        if (getDebugLevel() == 1)
                            suppressed = true;
                    }
                    return httpRoute;
                }
            };
            AbstractHttpClient httpClientImpl = (AbstractHttpClient) httpClient;
            httpClientImpl.setRoutePlanner(routePlanner);
        } else {
            routePlanner = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /** {@inheritDoc} */
    @Override
    public void setServiceLocation(URI serviceLocation)
            throws AnaplanAPITransportException {

        super.setServiceLocation(serviceLocation);

        String scheme = serviceLocation.getScheme();
        int port = getPort(serviceLocation);
        httpHost = new HttpHost(serviceLocation.getHost(), port, scheme);
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

        initialise(serviceCredentials);

        if (serviceCredentials.getScheme() != Scheme.USER_CERTIFICATE
                && getServiceLocation() != null) {
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
        if (routePlanner != null) {
            // Update the route planner
            routePlanner.setProxySelector(getProxySelector());
        }
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
        for (int attempt = 1;; ++attempt) try {
            HttpGet httpGet = new HttpGet(getRequestPath(path));
            if (acceptType != null) {
                httpGet.setHeader(HttpHeaders.ACCEPT, acceptType);
            }
            addHeaders(httpGet);

            if (getDebugLevel() >= 1) {
                logger.info(httpGet.getRequestLine().toString());
            }

            HttpResponse httpResponse = httpClient.execute(httpHost, httpGet,
                    httpContext);
            if (checkResponse(httpResponse, attempt)) continue;
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity != null) {
                return readResponseBody(httpEntity.getContent());
            } else {
                throw new AnaplanAPITransportException(
                        getMessage(MSG_NO_CONTENT), null);
            }
        } catch (IOException ioException) {
            handleIOException(ioException, attempt);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean head(String path) throws AnaplanAPITransportException {
        for (int attempt = 1;; ++attempt) try {
            HttpHead httpHead = new HttpHead(getRequestPath(path));
            if (getDebugLevel() >= 1) {
                logger.info(httpHead.getRequestLine().toString());
            }
            addHeaders(httpHead);

            HttpResponse httpResponse = httpClient.execute(httpHost, httpHead,
                    httpContext);
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
            if (checkResponse(httpResponse, attempt)) continue;
            // Dead code - checkResponse should have thrown an exception or returned true
            return false;

        } catch (IOException ioException) {
            handleIOException(ioException, attempt);
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
            addHeaders(httpPost);
            if (getDebugLevel() >= 1) {
                logger.info(httpPost.getRequestLine().toString());
            }
            httpPost.setEntity(new ByteArrayEntity(content));

            HttpResponse httpResponse = httpClient.execute(httpHost, httpPost,
                    httpContext);
            checkResponse(httpResponse);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity != null) {
                return readResponseBody(httpEntity.getContent());
            }
            return null;
        } catch (IOException ioException) {
            throw createTransportException(ioException);
        }
    }

    /** {@inheritDoc} */
    @Override
    public byte[] put(String path, byte[] content, String contentType)
            throws AnaplanAPITransportException {
        for (int attempt = 1;; ++attempt) try {
            final String binaryContent = "application/octet-stream";
            HttpPut httpPut = new HttpPut(getRequestPath(path));
            if (contentType == null)
                contentType = binaryContent;
            if (contentType.equals(binaryContent)) {
                contentType = "application/x-gzip";
                content = compress(content, 0, content.length);
            }
            httpPut.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
            addHeaders(httpPut);
            if (getDebugLevel() >= 1) {
                logger.info(httpPut.getRequestLine().toString());
            }
            httpPut.setEntity(new ByteArrayEntity(content));

            HttpResponse httpResponse = httpClient.execute(httpHost, httpPut,
                    httpContext);
            if (checkResponse(httpResponse, attempt)) continue;
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity != null) {
                return readResponseBody(httpEntity.getContent());
            }
            return null;
        } catch (IOException ioException) {
            handleIOException(ioException, attempt);
        }
    }

    /** {@inheritDoc} */
    @Override
    public byte[] delete(String path, String acceptType)
            throws AnaplanAPITransportException {
        for (int attempt = 1;; ++attempt) try {
            HttpDelete httpDelete = new HttpDelete(getRequestPath(path));
            if (acceptType != null) {
                httpDelete.setHeader(HttpHeaders.ACCEPT, acceptType);
            }
            addHeaders(httpDelete);
            if (getDebugLevel() >= 1) {
                logger.info(httpDelete.getRequestLine().toString());
            }

            HttpResponse httpResponse = httpClient.execute(httpHost,
                    httpDelete, httpContext);
            if (checkResponse(httpResponse, attempt)) continue;
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity != null) {
                return readResponseBody(httpEntity.getContent());
            } else {
                throw new AnaplanAPITransportException(
                        getMessage(MSG_NO_CONTENT), null);
            }
        } catch (IOException ioException) {
            handleIOException(ioException, attempt);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }

    protected void checkResponse(HttpResponse httpResponse)
            throws AnaplanAPITransportException {
        checkResponse(httpResponse, Integer.MAX_VALUE);
    }

    protected boolean checkResponse(HttpResponse httpResponse, int attempt)
            throws AnaplanAPITransportException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (getDebugLevel() >= 1) {
            logger.info(httpResponse.getStatusLine().toString());
        }
        if (!(200 <= statusCode && statusCode <= 299)) {
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                try {
                    Header contentTypeHeader = httpEntity.getContentType();
                    String entityContent = EntityUtils.toString(httpEntity);
                    if (getDebugLevel() >= 2) {
                        logger.info(String.valueOf(contentTypeHeader));
                        logger.info(entityContent);
                    }
                    if (!entityContent.isEmpty()
                            && contentTypeHeader != null
                            && contentTypeHeader.getValue().startsWith("text/plain")) {
                        throw new AnaplanAPITransportException(entityContent);
                    }

                } catch (IOException ioException) {
                    if (getDebugLevel() >= 1) {
                        logger.log(Level.WARNING,
                                "I/O exception whilst reading response",
                                ioException);
                    }
                }
            }
            if (0 <= Arrays.binarySearch(NONFATAL_STATUS_CODES, statusCode)
                    && attempt < idempotentRequestAttempts) {
                if (getDebugLevel() > 0) {
                    logger.log(Level.WARNING,
                            "Request failed (attempt " + attempt + " of "
                            + idempotentRequestAttempts + "); retrying after "
                            + attempt * retryDelay / 1000 + "s");
                }
                try {
                    Thread.sleep(attempt * retryDelay);
                } catch (InterruptedException interruptedException) {
                }
                return true;
            }
            throw new AnaplanAPITransportException(getStatusMessage(statusCode,
                    httpResponse.getStatusLine().getReasonPhrase()));
        }
        return false;
    }

    protected void handleIOException(IOException ioException, int attempt)
            throws AnaplanAPITransportException {
        if (attempt < idempotentRequestAttempts) {
            String message = "Request failed (attempt " + attempt + " of "
                        + idempotentRequestAttempts + "); retrying after "
                        + attempt * retryDelay / 1000 + "s";
            if (getDebugLevel() > 1) {
                logger.log(Level.WARNING, message, ioException);
            } else if (getDebugLevel() > 0) {
                logger.log(Level.WARNING, message);
            }
            try {
                Thread.sleep(attempt * retryDelay);
            } catch (InterruptedException interruptedException) {
            }
        } else {
            throw createTransportException(ioException);
        }
    }

    private AnaplanAPITransportException createTransportException(
            IOException ioException) {
        return new AnaplanAPITransportException(getMessage(MSG_COMMS_FAILURE),
                ioException);
    }

    /**
     * Extracts the common name from the certificate with the given alias
     * 
     * @param certificate
     *            the certificate
     * @return the value of the common name (CN) field
     */
    private String extractCommonName(X509Certificate certificate) {
        try {
            String dn = certificate.getSubjectX500Principal().getName();
            LdapName ldapDN = new LdapName(dn);
            for (Rdn rdn : ldapDN.getRdns()) {
                if ("CN".equals(rdn.getType())) {
                    return (String) rdn.getValue();
                }
            }
            throw new RuntimeException(
                    "Could not load common name (CN) field from certificate");
        } catch (InvalidNameException e) {
            throw new RuntimeException(
                    "Could not load common name (CN) field from certificate");
        }
    }

    /**
     * Adds any required HTTP header(s) to the provided request
     * 
     * @param request
     *            representation of HTTP request
     */
    private void addHeaders(HttpRequestBase request)
            throws AnaplanAPITransportException, IOException {
        if (getServiceCredentials().getScheme() == Scheme.USER_CERTIFICATE) {
            request.setHeader("Authorization", certificateAuthorizationHeader);
        }
    }

    /**
     * Convert to PEM format (a human-readable format using Base64 encoding)
     * 
     * @param cert
     *            the certificate
     */
    private String convertToPEMFormat(X509Certificate cert)
            throws AnaplanAPITransportException, IOException {
        try {
            return "-----BEGIN CERTIFICATE-----\n"
                    + new String(Base64.encodeBase64(cert.getEncoded()),
                            "US-ASCII") + "\n-----END CERTIFICATE-----";
        } catch (CertificateEncodingException cee) {
            throw new AnaplanAPITransportException(
                    "Failed to encode user certificate", cee);
        }
    }
}
