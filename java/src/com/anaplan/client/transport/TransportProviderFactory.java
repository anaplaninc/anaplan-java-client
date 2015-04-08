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

import java.util.HashMap;
import java.util.Map;


/**
  * Factory of TransportProvider implementations
  */

public class TransportProviderFactory {
    private static TransportProviderFactory instance;
    private Map<String, Class<? extends TransportProvider>> providers = new HashMap<String, Class<? extends TransportProvider>>();
    private Class<? extends TransportProvider> defaultProvider;
    private int debugLevel;
    /**
      * Get the singleton factory instance.
      * @return The (static) instance
      * @throws AnaplanAPITransportException if no providers are available
      */
    public static synchronized TransportProviderFactory getInstance() throws AnaplanAPITransportException {
        if (instance == null) {
            instance = new TransportProviderFactory();
        }
        return instance;
    }
    private TransportProviderFactory() throws AnaplanAPITransportException {
        try {
            Class<? extends TransportProvider> apacheHTTPProvider = Class.forName("com.anaplan.client.transport.ApacheHttpProvider").asSubclass(TransportProvider.class);
            providers.put("ApacheHTTP", apacheHTTPProvider);
            defaultProvider = apacheHTTPProvider;
        } catch (Throwable thrown) {
            if (debugLevel > 0)
                System.err.println(thrown.toString());
        }
    
        // Add others (ie those included in anaplan-connect-x-x-x.jar) here

        if (providers.isEmpty()) {
            throw new AnaplanAPITransportException("No transport providers available; check the classpath contains the necessary libraries");
        }
    }
    /**
      * Create a new provider by name
      * @return a newly-created provider; null if no such provider can be found
      */
    public TransportProvider createProvider(String name) throws AnaplanAPITransportException {
        try {
            Class<? extends TransportProvider> providerClass
                    = providers.get(name);
            if (providerClass == null) return null;
            TransportProvider instance = providerClass.newInstance();
            if (debugLevel > 0)
                instance.setDebugLevel(debugLevel);
            return instance;
        } catch (Exception exception) {
            throw new AnaplanAPITransportException("Failed to create transport provider(" + name + ")", exception);
        }
    }
    /**
      * Create a new default provider
      * @return a newly-created provider instance
      */
    public TransportProvider createDefaultProvider() throws AnaplanAPITransportException {
        try {
            TransportProvider instance = defaultProvider.newInstance();
            if (debugLevel > 0)
                instance.setDebugLevel(debugLevel);
            return instance;
        } catch (Exception exception) {
            throw new AnaplanAPITransportException("Failed to create default transport provider", exception);
        }
    }
}
