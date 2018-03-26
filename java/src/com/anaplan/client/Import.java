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

import java.util.List;

import com.anaplan.client.serialization.TypeWrapper;

/**
 * An import object within an Anaplan model.
 */
public class Import extends TaskFactory {
    
    /**
      * The set of types of import definition.
      * This enumerates possible import types available at the release of this
      * version of Anaplan Connect. It may be the case that other types have
      * added to Anaplan in the intervening period. Clients should be aware of, 
      * and handle, this potential condition gracefully.
      */
    public static enum ImportType {
        /** An import into a list. */
        HIERARCHY_DATA("List data"),
        /** An import into a module (data) view. */
        MODULE_DATA("Module data"),
        /** An import into a module blueprint view. */
        LINE_ITEM_DEFINITION("Module blueprint"),
        /** An import into the Users view. */
        USERS("Users"),
        /** An import into the Versions view. */
        VERSIONS("Versions");
        private String description;
        ImportType(String description) {
            this.description = description;
        }
        @Override
        public String toString() {
            return description;
        }
    }

    // Data passed over the wire
    static class Data extends NamedObject.Data {
        String importType;
        String importDataSourceId;
    }

    static final TypeWrapper<Data> DATA_TYPE = new TypeWrapper<Data>() {};
    static final TypeWrapper<List<Data>> DATA_LIST_TYPE = new TypeWrapper<List<Data>>() {};

    private Data data;

    Import(Model model, Data data) {
        super(model, data);
        this.data = data;
    }

    @Override
    String getPath() {
        return getModel().getPath() + "/imports/" + getId();
    }

    /**
      * Get the ID of the data source associated with the import, if it is an uploaded file.
      * @return the ID for the data source if it is a file; otherwise the empty
      * string ("") is returned.
      * @since 1.2
      */
    public String getSourceFileId() throws AnaplanAPIException {
        return data.importDataSourceId;
    }
    /**
      * Get the type of import.
      * @return The import type; null if the type is not recognized by this
      * version of Anaplan Connect.
      * @since 1.3
      */
    public ImportType getImportType() {
        try {
            return ImportType.valueOf(data.importType);
        } catch (Exception e) {
            return null;
        }
    }
}
