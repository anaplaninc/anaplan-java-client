//   Copyright 2012 Anaplan Inc.
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
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
  * Base class for providers that use the HTTP protocol.
  * @since 1.3
  */

public abstract class HttpProvider extends TransportProviderBase {

    /** The default port for HTTP */
    protected final int DEFAULT_HTTP_PORT = 80;
    /** The default port for HTTPS */
    protected final int DEFAULT_HTTPS_PORT = 443;
    /** HTTP Content was expected but none received */
    protected static final String MSG_NO_CONTENT = "noContent";
    /** Some kind of underlying communication failure */
    protected static final String MSG_COMMS_FAILURE = "communicationFailure";
    /** Prefix for HTTP status codes with special handling */
    protected static final String MSG_CODE_PREFIX = "code.";
    /** Message for HTTP status codes without special handling */
    protected static final String MSG_CODE_OTHER = "code.other";

    private ResourceBundle messages;

    protected HttpProvider() {
        messages = ResourceBundle.getBundle(
                HttpProvider.class.getName(),
                Locale.getDefault());
    }

    /**
     * Extract the port from a URI. If absent a default port number is provided.
     * @param URI the URI
     * @return the port number; if absent a default port will be chosen if the
     * scheme part specifies HTTP or HTTPS.
     * @since 1.3.4
     */
    protected int getPort(URI uri) {
        String scheme = uri.getScheme();
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = DEFAULT_HTTP_PORT;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = DEFAULT_HTTPS_PORT;
            }
        }
        return port;
    }

    /**
     * Get a localized message for the given key.
     * @param key one of the MSG_ constants in this class.
     * @param parameters optional message parameters 
     * @return the message, localized based on the default locale if possible.
     */
    protected String getMessage(String key, Object... parameters) {
        try {
            String message = messages.getString(key);
            if (parameters == null || parameters.length == 0) {
                return message;
            } else {
                MessageFormat messageFormat = new MessageFormat(message);
                return messageFormat.format(parameters).toString();
            }
        } catch (MissingResourceException mre) {
            StringBuilder message = new StringBuilder(key);
            for (int i = 0; i < message.length(); ++i) {
                char c = message.charAt(i);
                if (Character.isUpperCase(c)) {
                    message.insert(i++, ' ');
                    message.setCharAt(i, Character.toLowerCase(c));
                }
            }
            return message.toString();
        }
    }

    /**
     * Get a localized message for the HTTP status code
     * @param statusCode the HTTP status code of the response.
     * @param reasonPhrase the HTTP reason phrase from the response.
     * @return the message, localized based on the default locale if possible.
     */
    protected String getStatusMessage(int statusCode, String reasonPhrase) {
        String key = MSG_CODE_PREFIX + statusCode;
        if (messages.containsKey(key)) {
            return messages.getString(key);
        }
        return String.format(messages.getString(MSG_CODE_OTHER), statusCode, reasonPhrase);
    }
}
