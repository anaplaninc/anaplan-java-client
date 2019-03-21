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

import com.anaplan.client.dto.ModuleData;
import com.anaplan.client.dto.responses.ViewsResponse;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.transport.Paginator;

/**
 * A module within a model.
 *
 * @since 1.1
 */
public class Module extends NamedObject {

    Module(Model model, ModuleData data) {
        super(model, data);
    }

    /**
     * Retrieve a list of available views.
     *
     * @return A list of the available views within this module
     */
    public Iterable<View> getViews() throws AnaplanAPIException {
        Module self = this;
        return new Paginator<View>() {

            @Override
            public View[] getPage(int offset) {
                ViewsResponse response = getApi().getViews(getModel().getWorkspace().getId(),
                        getModel().getId(), getId(), offset);
                if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                    return response.getItem()
                            .stream()
                            .map(viewData -> new View(self, viewData))
                            .toArray(View[]::new);
                } else {
                    return new View[]{};
                }
            }
        };
    }

    /**
     * Retrieve a specific view.
     *
     * @param identifier The name, code or id for the view
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
