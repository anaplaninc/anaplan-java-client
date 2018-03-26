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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.anaplan.client.serialization.TypeWrapper;

/**
 * An Anaplan workspace.
 */
public class Workspace extends AnaplanApiClientObject {

    // This data is passed over the wire between client and server
    static final class Data {
        String guid;
        String name;

        public boolean equals(Object other) {
            if (!(other != null && other instanceof Data)) {
                return false;
            }
            Data data = (Data) other;
            return guid.equals(data.guid) && name.equals(data.name);
        }

        public int hashCode() {
            return guid.hashCode() * 31 + name.hashCode();
        }
    }

    static final TypeWrapper<List<Data>> DATA_LIST_TYPE = new TypeWrapper<List<Data>>() {
    };

    private Service service;
    private Data data;

    // Cached Model instances
    private Map<Model.Data, Reference<Model>> modelCache = new WeakHashMap<Model.Data, Reference<Model>>();

    // Package access: should only be instantiated by Service class.
    Workspace(Service service, Data data) {
        super(service);
        this.service = service;
        this.data = data;
    }

    Service getService() {
        return service;
    }

    @Override
    String getPath() {
        return getService().getPath() + "/workspaces/" + getId();
    }

    /**
     * Get the identifier of the workspace.
     * 
     * @return The identifier (guid) for the workspace
     */
    public String getId() {
        return data.guid;
    }

    /**
     * Get the name of the workspace.
     * 
     * @return The name of this workspace
     */
    public String getName() {
        return data.name;
    }

    /**
     * Retrieve the list of available models.
     * 
     * @return The list of models in the workspace to which the authenticated
     *         user has access
     */
    public List<Model> getModels() throws AnaplanAPIException {
        List<Model.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/models",
                        getSerializationHandler().getContentType()),
                Model.DATA_LIST_TYPE);
        List<Model> result = new ArrayList<Model>(response.size());
        for (Model.Data modelData : response) {
            Reference<Model> modelReference = modelCache.get(modelData);
            Model model = modelReference == null ? null : modelReference.get();
            if (model == null) {
                model = new Model(this, modelData);
                modelCache.put(modelData, new WeakReference<Model>(model));
            }
            result.add(model);
        }
        return result;
    }

    /**
     * Retrieve a specific model by identifier.
     * 
     * @param modelId
     *            The GUID or name of the model
     * @return The model, or null if no such model exists in the workspace
     */
    public Model getModel(String modelId) throws AnaplanAPIException {
        List<Model> models = getModels();
        for (Model model : models) {
            if (modelId.equals(model.getId())) {
                return model;
            }
        }
        for (Model model : models) {
            if (modelId.equalsIgnoreCase(model.getName())) {
                return model;
            }
        }
        return null;
    }
}
