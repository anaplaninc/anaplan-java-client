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

import com.anaplan.client.dto.ModelData;
import com.anaplan.client.dto.WorkspaceData;
import com.anaplan.client.dto.responses.ModelResponse;
import com.anaplan.client.dto.responses.ModelsResponse;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.ModelNotFoundException;
import com.anaplan.client.ex.ModelsNotFoundException;
import com.anaplan.client.transport.Paginator;
import feign.FeignException;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * An Anaplan workspace.
 */
public class Workspace extends AnaplanApiClientObject {

    private WorkspaceData data;

    // Cached Model instances
    private Map<ModelData, Reference<Model>> modelCache = new WeakHashMap<>();

    // Package access: should only be instantiated by Service class.
    Workspace(Service service, WorkspaceData data) {
        super(service);
        this.data = data;
    }

    /**
     * Get the identifier of the workspace.
     *
     * @return The identifier (id) for the workspace
     */
    public String getId() {
        return data.getId();
    }

    /**
     * Get the name of the workspace.
     *
     * @return The name of this workspace
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Retrieve the list of available models.
     *
     * @return The list of models in the workspace to which the authenticated
     * user has access
     */
    public Iterable<Model> getModels() throws AnaplanAPIException {
        Workspace self = this;
        try {
            return new Paginator<Model>() {

                @Override
                public Model[] getPage(int offset) {
                    ModelsResponse response = getApi().getModels(getUserId(), offset);
                    setPageInfo(response.getMeta().getPaging());
                    if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                        return response.getItem()
                                .stream()
                                .map(modelData -> {
                                    Reference<Model> modelReference = modelCache.get(modelData);
                                    Model model = modelReference == null ? null : modelReference.get();
                                    if (model == null) {
                                        model = new Model(self, modelData);
                                        modelCache.put(modelData, new WeakReference<>(model));
                                    }
                                    return model;
                                })
                                .toArray(Model[]::new);
                    } else {
                        return new Model[]{};
                    }
                }
            };
        } catch (FeignException e) {
            throw new ModelsNotFoundException(getId(), e);
        }
    }

    /**
     * Retrieve a specific model by identifier.
     *
     * @param modelId The GUID or name of the model
     * @return The model, or null if no such model exists in the workspace
     */
    public Model getModel(String modelId) {
        try {
            ModelResponse modelResponse = getApi().getModel(getUserId(), modelId);
            if (modelResponse != null && modelResponse.getItem() != null) {
                return new Model(this, modelResponse.getItem());
            }
        } catch (FeignException e) {
            throw new ModelNotFoundException(modelId, e);
        }
        return null;
    }
}
