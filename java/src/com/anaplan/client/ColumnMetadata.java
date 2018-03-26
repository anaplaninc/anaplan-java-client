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

import java.util.List;

import com.anaplan.client.serialization.TypeWrapper;

/**
  * Carries information about a column in an export.
  * @since 1.2
  */
public class ColumnMetadata {
    private DataType _columnDataType;
    private String _headerName;
    
    public ColumnMetadata(DataType columnDataType, String headerName) {
        _columnDataType = columnDataType;
        _headerName = headerName;
    }
    
    static TypeWrapper<ColumnMetadata> DATA_TYPE = new TypeWrapper<ColumnMetadata>(){};
    static TypeWrapper<List<ColumnMetadata>> DATA_LIST_TYPE = new TypeWrapper<List<ColumnMetadata>>(){};
    
    /**
      * Get the data type of the column.
      */
    public DataType getColumnDataType() {
        return _columnDataType;
    }
    
    /**
      * Get the header text for the column.
      */
    public String getHeaderName() {
        return _headerName;
    }
}
