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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.anaplan.client.Credentials;

/**
 * Contains functionality likely to be useful to any implementation of the
 * TransportProvider interface
 */
public abstract class TransportProviderBase implements TransportProvider {

    private static ProxySelector defaultProxySelector;

    private URI serviceLocation;

    private Credentials serviceCredentials;

    private URI proxyLocation;

    private Credentials proxyCredentials;

    private ProxySelector proxySelector = defaultProxySelector;

    private String userAgent;

    private int debugLevel;

    static {
        try {
            Class proxySearchClass = Class.forName("com.btr.proxy.search.ProxySearch");
            Method getDefaultProxySearch = proxySearchClass.getMethod("getDefaultProxySearch", new Class<?>[0]);
            Object proxySearchInstance = getDefaultProxySearch.invoke(null);
            Method getProxySelector = proxySearchClass.getMethod("getProxySelector", new Class<?>[0]);
            defaultProxySelector = (ProxySelector) getProxySelector.invoke(proxySearchInstance);
        } catch (Throwable thrown) {
            // Most likely the proxy-vole.jar file is not present in the lib folder.
            // Use Java default proxy selection instead.
            defaultProxySelector = ProxySelector.getDefault();
        }
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
        this.serviceLocation = serviceLocation;
    }

    protected URI getServiceLocation() {
        return serviceLocation;
    }

    /** {@inheritDoc} */
    @Override
    public void setServiceCredentials(Credentials serviceCredentials)
            throws AnaplanAPITransportException {
        if (null == serviceCredentials.getUserName()
                || serviceCredentials.getUserName().isEmpty()) {
            throw new AnaplanAPITransportException("User name not specified");
        }
        if (null == serviceCredentials.getPassphrase()
                || serviceCredentials.getPassphrase().isEmpty()) {
            throw new AnaplanAPITransportException("Passphrase not specified");
        }
        this.serviceCredentials = serviceCredentials;
    }

    protected Credentials getServiceCredentials() {
        return serviceCredentials;
    }

    /** {@inheritDoc} */
    @Override
    public void setProxyLocation(URI proxyLocation)
            throws AnaplanAPITransportException {
        this.proxyLocation = proxyLocation;
        Proxy.Type proxyType = Proxy.Type.DIRECT;
        SocketAddress socketAddress = null;
        final Proxy proxy;
        if (proxyLocation != null) {
            if ("http".equalsIgnoreCase(proxyLocation.getScheme())) {
                proxyType = Proxy.Type.HTTP;
            } else if ("socks".equalsIgnoreCase(proxyLocation.getScheme())) {
                proxyType = Proxy.Type.SOCKS;
            } else {
                throw new IllegalArgumentException("Unsupported scheme for proxy location URI (must start with http: or socks:): " + proxyLocation.getScheme());
            }
            socketAddress = new InetSocketAddress(proxyLocation.getHost(), proxyLocation.getPort());
            proxy = new Proxy(proxyType, socketAddress);
        } else {
            proxy = Proxy.NO_PROXY;
        }
        proxySelector = new ProxySelector() {
            public List<Proxy> select(URI uri) {
                return Collections.singletonList(proxy);
            }
            public void connectFailed(URI uri, SocketAddress socketAddress, IOException ioException) {
            }
        };
    }

    protected URI getProxyLocation() {
        return proxyLocation;
    }

    /** {@inheritDoc} */
    @Override
    public void setProxyCredentials(Credentials proxyCredentials)
            throws AnaplanAPITransportException {
        if (null == proxyCredentials.getUserName()
                || proxyCredentials.getUserName().isEmpty()) {
            throw new AnaplanAPITransportException("User name not specified");
        }
        if (null == proxyCredentials.getPassphrase()
                || proxyCredentials.getPassphrase().isEmpty()) {
            throw new AnaplanAPITransportException("Passphrase not specified");
        }
        this.proxyCredentials = proxyCredentials;
    }

    protected Credentials getProxyCredentials() {
        return proxyCredentials;
    }

    protected ProxySelector getProxySelector() {
        return proxySelector;
    }

    /** {@inheritDoc} */
    @Override
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    protected String getUserAgent() {
        return userAgent;
    }

    /** {@inheritDoc} */
    @Override
    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    protected int getDebugLevel() {
        return debugLevel;
    }

    // Abstract TransportProvider methods
    public abstract byte[] get(String path, String acceptType)
            throws AnaplanAPITransportException;

    public abstract boolean head(String path)
            throws AnaplanAPITransportException;

    public abstract byte[] post(String path, byte[] content,
            String contentType, String acceptType)
            throws AnaplanAPITransportException;

    public abstract byte[] put(String path, byte[] content, String contentType)
            throws AnaplanAPITransportException;

    public abstract byte[] delete(String path, String acceptType)
            throws AnaplanAPITransportException;

    public abstract void close();

    /**
     * Build a sanitized path relative to the service location. Any invalid
     * characters are converted to % sequences, and sequences of more than one
     * slash are avoided when concatenating.
     * 
     * @param path
     *            The relative path, which should start with a '/'
	 * @return The full path of the URI
	 */
	protected String getRequestPath(String path) {
		StringBuilder result = new StringBuilder(serviceLocation.getPath());
		// Trim any trailing '/' characters from the service locator's path
        while (result.length() > 0 && result.charAt(result.length() - 1) == '/')
            result.setLength(result.length() - 1);
        String encodedPath = path;
        try {
            encodedPath = new URI(null, null, path, null).getRawPath();
        } catch (URISyntaxException uriSyntaxException) {
            System.err.println("Warning: failed to encode URI path \"" + path
					+ "\": " + uriSyntaxException);
        }
        if (!encodedPath.isEmpty() && encodedPath.charAt(0) != '/')
            result.append('/');
        return result.append(encodedPath).toString();
    }

    /**
     * Read the stream fully into a byte array. The stream is read until the
     * end-of-stream condition is reached.
     * 
     * @param inputStream
     *            the stream to read
     * @return the data read from the stream
     */
    protected byte[] readResponseBody(InputStream inputStream)
            throws AnaplanAPITransportException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final int bufferSize = 32768;
            byte[] buffer = new byte[bufferSize];
            int read = 0;
            while (read >= 0) {
                read = inputStream.read(buffer);
                if (read > 0) {
                    baos.write(buffer, 0, read);
                }
            }
            inputStream.close();
            return baos.toByteArray();
        } catch (IOException ioException) {
            throw new AnaplanAPITransportException(
                    "Failed to read response body from server", ioException);
        }
    }

    /**
     * Compress the content using GZip compression.
     * 
     * @param source
     *            the byte array containing the content to be compressed
     * @param offset
     *            the start offset of the data to be compressed
     * @param size
     *            the length of the data, in bytes, to be compressed
     * @return the compressed content
     */
    protected byte[] compress(byte[] source, int offset, int size)
            throws AnaplanAPITransportException {
        try {
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(sink);
            gzos.write(source, offset, size);
            gzos.close();
            return sink.toByteArray();
        } catch (IOException ioException) {
            throw new AnaplanAPITransportException(
                    "Failed to compress data for upload", ioException);
        }
    }

    /**
     * Decompress the GZip-compressed data
     * 
     * @param source
     *            the byte array containing the data to be decompressed
     * @param offset
     *            the start offset of the data to be decompressed
     * @param size
     *            the length of the compressed data, in bytes
     * @return the decompressed content
     * @throws AnaplanAPITransportException
     *             if the compressed data is invalid
     */
    protected byte[] decompress(byte[] source, int offset, int size)
            throws AnaplanAPITransportException {
        try {
            GZIPInputStream gzis = new GZIPInputStream(
                    new ByteArrayInputStream(source, offset, size));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final int bufferSize = 65536;
            byte[] buffer = new byte[bufferSize];
            int read = 0;
            while (read >= 0) {
                read = gzis.read(buffer);
                if (read > 0) {
                    baos.write(buffer, 0, read);
                }
            }
            gzis.close();
            return baos.toByteArray();
        } catch (IOException ioException) {
            throw new AnaplanAPITransportException(
                    "Failed to decompress downloaded data", ioException);
        }
    }
}
