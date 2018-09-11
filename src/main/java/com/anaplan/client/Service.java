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

import com.anaplan.client.auth.Authenticator;
import com.anaplan.client.auth.AuthenticatorFactory;
import com.anaplan.client.dto.WorkspaceData;
import com.anaplan.client.dto.responses.UserResponse;
import com.anaplan.client.dto.responses.WorkspaceResponse;
import com.anaplan.client.dto.responses.WorkspacesResponse;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.UserNotFoundException;
import com.anaplan.client.ex.WorkspaceNotFoundException;
import com.anaplan.client.transport.AnaplanApiProvider;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.Paginator;
import com.google.common.base.Preconditions;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * An authenticated connection to the Anaplan API service.
 */
public class Service implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Service.class);
    private static final URI PRODUCTION_API_ROOT;
    private static final URI PRODUCTION_AUTH_API_ROOT;
    private String userId;

    static {
        try {
            PRODUCTION_API_ROOT = new URI("https://api.anaplan.com/");
            PRODUCTION_AUTH_API_ROOT = new URI("https://auth.anaplan.com/");
        } catch (URISyntaxException uriSyntaxException) {
            throw new ExceptionInInitializerError(uriSyntaxException);
        }
    }

    private ConnectionProperties props;
    private AnaplanApiProvider apiProvider;
    private Authenticator authProvider;

    public AnaplanApiProvider getApiProvider() {
        return apiProvider;
    }

    public void setApiProvider(AnaplanApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

    public Authenticator getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(Authenticator authProvider) {
        this.authProvider = authProvider;
    }

    // Cached Workspace instances
    private Map<WorkspaceData, Reference<Workspace>> workspaceCache = new WeakHashMap<>();

    public Service(ConnectionProperties properties) {
        if (properties.getApiServicesUri() == null) {
            properties.setApiServicesUri(PRODUCTION_API_ROOT);
        }
        if (properties.getAuthServiceUri() == null) {
            properties.setAuthServiceUri(PRODUCTION_AUTH_API_ROOT);
        }
        LOG.info("Initializing Service...");
        this.props = properties;
        this.authProvider = AuthenticatorFactory.getAuthenticator(properties);
        this.apiProvider = new AnaplanApiProvider(properties, authProvider);
    }

    /**
     * Authenticates using provided credentials
     */
    public void authenticate() {
        Preconditions.checkNotNull(props.getApiCredentials(), "No service credentials present to authenticate with.");
        authProvider.getAuthToken();
    }

    public String getUserId() {
        if (userId == null) {
            try {
                UserResponse userResponse = apiProvider.getApiClient().getUser();
                userId = userResponse.getItem().getId();
            } catch (FeignException | AnaplanAPIException e) {
                throw new UserNotFoundException(e);
            }
        }
        return userId;
    }

    /**
     * Retrieves the list of available workspaces.
     *
     * @return The list of workspaces this user has access to
     * @throws AnaplanAPIException an error occurred.
     */

    public Iterable<Workspace> getWorkspaces() throws AnaplanAPIException {
        Service self = this;
        return new Paginator<Workspace>() {

            @Override
            public Workspace[] getPage(int offset) {
                WorkspacesResponse response = apiProvider.getApiClient().getWorkspaces(
                        getUserId(), offset);
                setPageInfo(response.getMeta().getPaging());
                if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                    return response.getItem()
                            .stream()
                            .map(workspaceData -> {
                                Reference<Workspace> workspaceReference = workspaceCache.get(workspaceData);
                                Workspace workspace = workspaceReference == null ? null : workspaceReference.get();
                                if (workspace == null) {
                                    workspace = new Workspace(self, workspaceData);
                                    workspaceCache.put(workspaceData, new WeakReference<>(workspace));
                                }
                                return workspace;
                            })
                            .toArray(Workspace[]::new);
                } else {
                    return new Workspace[]{};
                }
            }
        };
    }

    /**
     * Retrieve a reference to a workspace from its workspaceId.
     *
     * @param workspaceId The workspace ID or name of the workspace.
     * @return The workspace; null if no such workspace exists or the user is
     * not permitted to access the workspace.
     * @throws com.anaplan.client.ex.AnaplanAPIException an error occurred.
     */

    public Workspace getWorkspace(String workspaceId) {
        try {
            WorkspaceResponse response = apiProvider.getApiClient().getWorkspace(getUserId(), workspaceId);
            if (response != null && response.getItem() != null) {
                return new Workspace(this, response.getItem());
            } else {
                throw new WorkspaceNotFoundException(workspaceId);
            }
        } catch (FeignException e) {
            throw new WorkspaceNotFoundException(workspaceId, e);
        }
    }

    /**
     * Release any system resources associated with this instance.
     */
    @Override
    public void close() {
        if (apiProvider != null) {
            apiProvider = null;
            authProvider = null;
        }
    }


}
