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

package com.anaplan.client;

import com.anaplan.client.serialization.SerializationHandler;
import com.anaplan.client.transport.TransportProvider;

/**
  * Base class for remote server object accessor classes.
  * No publicly accessible methods are introduced by this class; it contains
  * only members relevant to the implementation
  */
public abstract class AnaplanApiClientObject {

    // The Service object representing the connection to the server
    private final Service service;

    AnaplanApiClientObject(Service service) {
        this.service = service == null ? (Service) this : service;
    }
    // Delegate serialization to service
    SerializationHandler getSerializationHandler() throws AnaplanAPIException {
        return service.getSerializationHandler();
    }

    // Delegate transport to service
    TransportProvider getTransportProvider() throws AnaplanAPIException {
        return service.getTransportProvider();
    }

    // Path part of server URI to locate this object
    abstract String getPath();

}
