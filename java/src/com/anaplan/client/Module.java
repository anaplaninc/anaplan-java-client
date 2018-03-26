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

import java.util.ArrayList;
import java.util.List;

import com.anaplan.client.serialization.TypeWrapper;

/**
 * A module within a model.
 * 
 * @since 1.1
 */
public class Module extends NamedObject {

    // Data passed over the wire
    static class Data extends NamedObject.Data {
    }

    static TypeWrapper<Data> DATA_TYPE = new TypeWrapper<Data>() {
    };
    static TypeWrapper<List<Data>> DATA_LIST_TYPE = new TypeWrapper<List<Data>>() {
    };

    Module(Model model, Data data) {
        super(model, data);
    }

    final String getPath() {
        return getModel().getPath() + "/modules/" + getId();
    }

    /**
     * Retrieve a list of available views.
     * 
     * @return A list of the available views within this module
     */
    public List<View> getViews() throws AnaplanAPIException {
        List<View.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/views",
                        getSerializationHandler().getContentType()),
                View.DATA_LIST_TYPE);
        List<View> result = new ArrayList<View>(response.size());
        for (View.Data viewData : response) {
            result.add(new View(this, viewData));
        }
        return result;
    }

    /**
     * Retrieve a specific view.
     * 
     * @param identifier
     *            The name, code or id for the view
     * @return The view object
     */
    public View getView(String identifier) throws AnaplanAPIException {
        for (View view : getViews()) {
            if (identifier.equals(view.getId())
                    || identifier.equalsIgnoreCase(view.getCode())
                    || identifier.equalsIgnoreCase(view.getName())) {
                return view;
            }
        }
        return null;
    }
}
