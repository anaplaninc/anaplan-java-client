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

import com.anaplan.client.dto.NamedObjectData;

/**
 * The superclass of all named objects within an Anaplan model.
 */
public abstract class NamedObject extends AnaplanApiClientObject {

    private Model model;
    private NamedObjectData data;

    protected NamedObject(Model model, NamedObjectData data) {
        super(model.getWorkspace().getService());
        this.model = model;
        this.data = data;
    }

    Model getModel() {
        return model;
    }

    Workspace getWorkspace() {
        return model.getWorkspace();
    }

    public NamedObjectData getData() {
        return data;
    }

    /**
     * Get the Anaplan-generated identifier for the object.
     * It is important to note that these identifiers are
     * system-generated within the Anaplan model. The id for an object
     * will never change for the lifetime of the model that contains it,
     * but new ids are assigned for objects when a model is copied.
     *
     * @return The identifier for this object within the model
     */
    public String getId() {
        return data.getId();
    }

    /**
     * Get the name of the object.
     * This is the name used to refer to the object in the Anaplan.
     * Since it is possible that an object may be renamed, when
     * referring to an object through the API it is often better to find
     * it by code rather than name, if it has one.
     *
     * @return The name for this object
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Get the code for the object.
     * The code for an object is generally used to provide a stable
     * identifier that does not change when a model is copied (as is the
     * case for the system-generated ids), but which nevertheless allows
     * objects to be renamed for display purposes.
     *
     * @return The code for this object
     */
    public String getCode() {
        return data.getCode();
    }
}
