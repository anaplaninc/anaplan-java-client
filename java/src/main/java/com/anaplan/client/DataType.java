//   Copyright 2012 Anaplan Inc.
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

/**
 * The basic type of a cell in an Anaplan model.
 *
 * @since 1.2
 */
public enum DataType {

    NONE, NUMBER, ENTITY("LIST"), DATE, TEXT, BOOLEAN, FORMAT, SUMMARY, MULTISELECT_HIERARCHY, MULTISELECT_USER, MULTISELECT_ENTITY, MULTISELECT_MODULE, ACTION_DEFINITION, MIXED;

    private final String _label;

    private DataType() {
        this(null);
    }

    private DataType(String label) {
        _label = label;
    }

    /**
     * Return an identifier for the data type.
     * This returns the enumeration name, with the exception of ENTITY which returns "LIST".
     */
    public String getLabel() {
        return _label == null ? name() : _label;
    }

}
