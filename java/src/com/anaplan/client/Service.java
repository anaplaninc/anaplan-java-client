//   Copyright 2011, 2012 Anaplan Inc.
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.anaplan.client.serialization.SerializationHandler;
import com.anaplan.client.serialization.SerializationHandlerFactory;
import com.anaplan.client.transport.TransportProvider;
import com.anaplan.client.transport.TransportProviderFactory;

/**
 * An authenticated connection to the Anaplan API service.
 */
public class Service extends AnaplanApiClientObject implements Closeable {

    /**
     * The Major version of the API.
     */

    public static final int MAJOR_VERSION = Version.MAJOR;

    /**
     * The Minor version of the API.
     */

    public static final int MINOR_VERSION = Version.MINOR;

    /**
     * The Revision version of the API.
     */

    public static final int REVISION_VERSION = Version.REVISION;

    /**
     * The Release version of the API.
     */

    public static final String RELEASE_VERSION = Version.RELEASE;

    // Base path for resource locators
    private static final String PATH = "/" + MAJOR_VERSION + "/"
            + MINOR_VERSION;

    /**
     * The base URI of Anaplan's Production API service.
     */

    public static final URI PRODUCTION_API_ROOT;

    /**
     * The base URI of Anaplan's UAT Sandbox API service.
     */

    public static final URI UAT_API_ROOT;

    static {
        try {
            PRODUCTION_API_ROOT = new URI("https://api.anaplan.com/");
            UAT_API_ROOT = new URI("https://api.uat.anaplan.com/");
        } catch (URISyntaxException uriSyntaxException) {
            throw new ExceptionInInitializerError(uriSyntaxException);
        }
    }

    // The base URI for this Service instance
    private URI serviceLocation;

    // The service credentials
    private Credentials serviceCredentials;

    // The URI for proxy override
    private URI proxyLocation;

    // Has a proxy been specified (true) or are we using system default proxy (false)
    private boolean proxyLocationProvided;

    // The proxy credentials
    private Credentials proxyCredentials;

    // Serialization handler
    private SerializationHandler serializationHandler;

    // Transport provider
    private TransportProvider transportProvider;

    // Has the transport provider been initialized with location/credentials?
    private boolean transportProviderInitialized;

    // Cached Workspace instances
    private Map<Workspace.Data, Reference<Workspace>> workspaceCache = new WeakHashMap<Workspace.Data, Reference<Workspace>>();

    // Debug level
    private int debugLevel = 0;

    @Override
    String getPath() {
        return PATH;
    }

    /**
     * Create a new service client with the specified endpoint.
     * 
     * @param serviceLocation
     *            The base URI of the service to which the client will connect
     */

    public Service(URI serviceLocation) {
        super(null);
        if (serviceLocation == null)
            throw new IllegalArgumentException(
                    "Service location must be specified");
        this.serviceLocation = serviceLocation;
    }

    /**
     * Create a new service client for Anaplan's Production API Service.
     */

    public Service() {
        this(PRODUCTION_API_ROOT);
    }

    /**
     * Set the location of a proxy.
     * 
     * @param proxyLocation
     *            The URI of the proxy via which the service will be accessed
     */

    public void setProxyLocation(URI proxyLocation) throws AnaplanAPIException {
        this.proxyLocation = proxyLocation;
        proxyLocationProvided = true;
        if (transportProvider != null) {
            transportProvider.setProxyLocation(proxyLocation);
        }
    }

    /**
     * Set the user credentials for service authentication.
     * 
     * @param serviceCredentials
     *            The credentials to authenticate with
     */

    public void setServiceCredentials(Credentials serviceCredentials)
            throws AnaplanAPIException {
        this.serviceCredentials = serviceCredentials;
        if (transportProvider != null) {
            transportProvider.setServiceCredentials(serviceCredentials);
        }
    }

    /**
     * Set the user credentials for proxy authentication.
     * 
     * @param proxyCredentials
     *            The credentials to authenticate with
     * @since 1.3.1
     */
    public void setProxyCredentials(Credentials proxyCredentials)
            throws AnaplanAPIException {
        this.proxyCredentials = proxyCredentials;
        if (transportProvider != null) {
            transportProvider.setProxyCredentials(proxyCredentials);
        }
    }

    /**
     * Override the serialization handler to be used. If this method is not
     * called during the initialization phase, then a default handler will be
     * used.
     */
    public void setSerializationHandler(
            SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
    }

    SerializationHandler getSerializationHandler() throws AnaplanAPIException {
        if (serializationHandler == null) {
            SerializationHandlerFactory serializationHandlerFactory = SerializationHandlerFactory
                    .getInstance();
            serializationHandler = serializationHandlerFactory
                    .getDefaultHandler();
        }
        return serializationHandler;
    }

    /**
     * Override the transport provider to be used. If this method is not called
     * during the initialization phase, then a default provider will be used.
     */
    public void setTransportProvider(TransportProvider transportProvider) {
        this.transportProvider = transportProvider;
        transportProviderInitialized = false;
    }

    /**
     * Override the debug level. Values greater than zero will cause more
     * information to be produced on failure.
     * 
     * @param debugLevel
     *            the debug level
     * @since 1.3
     */
    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    synchronized TransportProvider getTransportProvider()
            throws AnaplanAPIException {
        if (transportProvider == null) {
            TransportProviderFactory transportProviderFactory = TransportProviderFactory
                    .getInstance();
            transportProvider = transportProviderFactory
                    .createDefaultProvider();
        }
        if (!transportProviderInitialized) {
            transportProvider.setServiceLocation(serviceLocation);
            transportProvider.setServiceCredentials(serviceCredentials);
            if (proxyLocationProvided) {
                transportProvider.setProxyLocation(proxyLocation);
            }
            if (proxyCredentials != null) {
                transportProvider.setProxyCredentials(proxyCredentials);
            }
            transportProvider.setDebugLevel(debugLevel);
            // Set transportProviderInitialized to true before calling
            // buildUserAgentIdentifier to avoid unbounded recursion.
            transportProviderInitialized = true;
            transportProvider.setUserAgent(buildUserAgentIdentifier());
        }
        return transportProvider;
    }

    /**
     * Generate a suitable value for a User-Agent header.
     */
    protected String buildUserAgentIdentifier() throws AnaplanAPIException {
        StringBuilder result = new StringBuilder(getClass().getName());
        result.append("/").append(MAJOR_VERSION).append(".")
                .append(MINOR_VERSION);
        result.append(".").append(REVISION_VERSION).append(RELEASE_VERSION);
        result.append(" (").append(getTransportProvider().toString());
        result.append("; ").append(getSerializationHandler().toString());
        String vmIdentifier = System.getProperty("java.vm.name") + " ("
                + System.getProperty("java.vendor") + ")/"
                + System.getProperty("java.vm.version") + " ("
                + System.getProperty("java.version") + ")";
        result.append("; ").append(vmIdentifier);
        String osIdentifier = System.getProperty("os.name") + " ("
                + System.getProperty("os.arch") + ")/"
                + System.getProperty("os.version");
        result.append("; ").append(osIdentifier).append(')');
        return result.toString();
    }

    /**
     * Check that the Service object is ready for use.
     * 
     * @throws IllegalStateException
     *             the service has been closed, or no credentials have been
     *             provided.
     */
    protected void checkInitialized() {
        if (serviceLocation == null) {
            throw new IllegalStateException(
                    "The Service instance is in a closed state."
                            + " No further access is permitted.");
        }
        if (serviceCredentials == null) {
            throw new IllegalStateException("No credentials were provided");
        }
    }

    /**
     * Retrieves the list of available workspaces.
     * 
     * @return The list of workspaces this user has access to
     * @throws AnaplanAPIException
     *             an error occurred.
     */

    public List<Workspace> getWorkspaces() throws AnaplanAPIException {
        checkInitialized();
        List<Workspace.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/workspaces/",
                        getSerializationHandler().getContentType()),
                Workspace.DATA_LIST_TYPE);
        List<Workspace> result = new ArrayList<Workspace>(response.size());
        for (Workspace.Data workspaceData : response) {
            Reference<Workspace> workspaceReference = workspaceCache
                    .get(workspaceData);
            Workspace workspace = workspaceReference == null ? null
                    : workspaceReference.get();
            if (workspace == null) {
                workspace = new Workspace(this, workspaceData);
                workspaceCache.put(workspaceData, new WeakReference<Workspace>(
                        workspace));
            }
            result.add(workspace);
        }
        return result;
    }

    /**
     * Retrieve a reference to a workspace from its workspaceId.
     * 
     * @param workspaceId
     *            The workspace ID or name of the workspace.
     * @return The workspace; null if no such workspace exists or the user is
     *         not permitted to access the workspace.
     * @throws AnaplanAPIException
     *             an error occurred.
     */

    public Workspace getWorkspace(String workspaceId)
            throws AnaplanAPIException {
        checkInitialized();
        List<Workspace> workspaces = getWorkspaces();
        for (Workspace workspace : workspaces) {
            if (workspaceId.equals(workspace.getId())) {
                return workspace;
            }
        }
        for (Workspace workspace : workspaces) {
            if (workspaceId.equalsIgnoreCase(workspace.getName())) {
                return workspace;
            }
        }
        return null;
    }

    /**
     * Release any system resources associated with this instance.
     */
    @Override
    public void close() {
        serviceLocation = null;
        if (transportProvider != null) {
            transportProvider.close();
            transportProvider = null;
        }
    }
}
