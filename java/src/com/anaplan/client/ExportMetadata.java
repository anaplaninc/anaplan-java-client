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
import com.anaplan.client.DataType;

/**
  * Represents information about the format and layout of an export.
  * @since 1.2
  */
public class ExportMetadata {
    // Data passed over wire
    static class Data {
        int columnCount;
        int rowCount;
        String exportFormat;
        String separator;
        String encoding;
        String delimiter;
        DataType[] dataTypes;
        String[] headerNames;
        String[] listNames;
    }

    static final TypeWrapper<Data> DATA_TYPE = new TypeWrapper<Data>() {
    };
    static final TypeWrapper<List<Data>> DATA_LIST_TYPE = new TypeWrapper<List<Data>>() {
    };

    private final Data data;

    ExportMetadata(Data data) {
        this.data = data;
    }

    /**
      * Get the column count.
      */
    public int getColumnCount() {
        return data.columnCount;
    }

    /**
      * Get the row count.
      */
    public int getRowCount() {
        return data.rowCount;
    }

    /**
      * Get the export format.
      */
    public String getExportFormat() {
        return data.exportFormat;
    }

    /**
      * Get the column separator character for text.
      * If the export format is CSV, this will be a comma character.
      */
    public String getSeparator() {
        return data.separator;
    }

    /**
      * Get the text encoding for text/CSV.
      */
    public String getEncoding() {
        return data.encoding;
    }

    /**
      * Get the text delimiter for text/CSV.
      * The delimiter is typically a single or double quote, inside which cell
      * values are enclosed, with a double-delimiter used to represent a literal
      * delimiter character inside the cell. If no delimiter is set, the empty
      * string is returned.
      */
    public String getDelimiter() {
        return data.delimiter;
    }

    /**
     * Get the data types of each column in order.
     */
    public DataType[] getDataTypes() {
        return data.dataTypes;
    }

    /**
     * Get the header names of each column in order.
     */
    public String[] getHeaderNames() {
        return data.headerNames;
    }

    /**
     * Get the names of lists applicable to each column in order.
     */
    public String[] getListNames() {
        return data.listNames;
    }
}
