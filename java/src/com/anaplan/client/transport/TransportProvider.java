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

import java.net.URI;

import com.anaplan.client.Credentials;

/**
  * Represents a means of communicating (normally via HTTP) with an Anaplan
  * API service implementation. The primary purpose of this interface is to
  * cleanly abstract over third-party library dependencies, allowing
  * alternatives to be developed if necessary.
  */
public interface TransportProvider {
    /**
      * Set the service endpoint location
      * @param serviceLocation the service endpoint location
      */
    void setServiceLocation(URI serviceLocation)
            throws AnaplanAPITransportException;
    /**
      * Set the service credentials
      * @param serviceCredentials the service credentials
      */
    void setServiceCredentials(Credentials serviceCredentials)
            throws AnaplanAPITransportException;
    /**
      * Route requests through a proxy server.
      * If set to null, then a direct connection will be used.
      * If this method is not called, then proxy auto-detection may be attempted.
      * @param proxyLocation the location of the proxy server
      */
    void setProxyLocation(URI proxyLocation)
            throws AnaplanAPITransportException;
    /**
      * Set the credentials for a proxy requiring authentication
      * @param proxyCredentials the proxy credentials
      */
    void setProxyCredentials(Credentials proxyCredentials)
            throws AnaplanAPITransportException;
    /**
      * Set the content of the User-Agent header to be passed to the server.
      * This is used to identify the version of client when troubleshooting
      * @param userAgent The User-Agent header value
      */
    void setUserAgent(String userAgent) throws AnaplanAPITransportException;
    /**
      * Set the extent to which debug messages are produced
      * @param debugLevel the debug level; 0 (the default) means no output
      */
    void setDebugLevel(int debugLevel) throws AnaplanAPITransportException;
    /**
      * Send a GET request to the server
      * @param path The path relative to the service endpoint location
      * @param acceptType The Accept header to indicate the format of content
      *         expected in the reponse body
      * @return The (raw) body content; null if no content is available
      * @throws AnaplanAPITransportException a communication or protocol error occured
      */
    byte[] get(String path, String acceptType)
            throws AnaplanAPITransportException;
    /**
      * Send a HEAD request to the server
      * @param path The path relative to the service endpoint location
      * @return true if the resource exists; false if the resource was not found
      * @throws AnaplanAPITransportException a communication or protocol error occured
      */
    boolean head(String path) throws AnaplanAPITransportException;
    /**
      * Send a POST request to the server
      * @param path The path relative to the service endpoint location
      * @param content The content to send in the request body
      * @param contentType The value for the Content-Type header to indicate
      *        the format of the content in the request body
      * @param acceptType The Accept header to indicate the format of content
      *        expected in the reponse body
      * @return The (raw) body content; null if no content is available
      * @throws AnaplanAPITransportException a communication or protocol error occured
      */
    byte[] post(String path, byte[] content, String contentType,
            String acceptType) throws AnaplanAPITransportException;
    /**
      * Send a PUT request to the server
      * @param path The path relative to the service endpoint location
      * @param content The content to send in the request body
      * @param contentType The value for the Content-Type header to indicate
      *        the format of the content in the request body
      * @return The (raw) body content; null if no content is available
      * @throws AnaplanAPITransportException a communication or protocol error occured
      */
    byte[] put(String path, byte[] content, String contentType)
            throws AnaplanAPITransportException;
    /**
      * Send a DELETE request to the server
      * @param path The path relative to the service endpoint location
      * @param acceptType The Accept header to indicate the format of content
      *        expected in the reponse body
      * @return The (raw) body content; null if no content is available
      * @throws AnaplanAPITransportException a communication or protocol error occured
      */
    byte[] delete(String path, String acceptType)
            throws AnaplanAPITransportException;
    /**
      * Release all resources acquired by this provider
      */
    void close();
}
