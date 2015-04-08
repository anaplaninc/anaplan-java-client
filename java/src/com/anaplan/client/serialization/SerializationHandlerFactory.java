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

package com.anaplan.client.serialization;

import java.util.HashMap;
import java.util.Map;


/**
  * Registry of SerializationHandler implementations
  */

public class SerializationHandlerFactory {
    private static SerializationHandlerFactory instance;
    private Map<String, SerializationHandler> handlers = new HashMap<String, SerializationHandler>();
    private SerializationHandler defaultHandler;
    /**
      * Get the singleton factory instance.
      * @return The (static) instance
      * @throws AnaplanAPISerializationException if no handlers are available
      */
    public static synchronized SerializationHandlerFactory getInstance() throws AnaplanAPISerializationException {
        if (instance == null) {
            instance = new SerializationHandlerFactory();
        }
        return instance;
    }
    private SerializationHandlerFactory() throws AnaplanAPISerializationException {
        try {
            Class.forName("com.google.gson.Gson");
            SerializationHandler gsonHandler = (SerializationHandler) Class.forName("com.anaplan.client.serialization.GsonHandler").newInstance();
            handlers.put("GSON", gsonHandler);
            defaultHandler = gsonHandler;
        } catch (Throwable thrown) {
        }
    
        // Add others (ie those included in anaplan-connect-x-x-x.jar) here

        if (handlers.isEmpty()) {
            throw new AnaplanAPISerializationException("No serialization handlers available: try adding gson-1.7.1.jar to the classpath");
        }
    }
    /**
      * Get a handler by name
      */
    public SerializationHandler getHandler(String name) {
        return handlers.get(name);
    }
    /**
      * Get the default handler
      */
    public SerializationHandler getDefaultHandler() {
        return defaultHandler;
    }
}
