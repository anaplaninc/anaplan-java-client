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

import com.anaplan.client.ex.AnaplanAPIException;

import java.io.IOException;

/**
 * Abstract source of tabulated cell data.
 *
 * @since 1.2
 */
public interface CellReader {
    /**
     * Return the header row.
     * This may be called multiple times, but will return the same value.
     *
     * @return If there is no header information available, then null;
     * otherwise, an array of string header values, one per column.
     */
    String[] getHeaderRow() throws AnaplanAPIException, IOException;

    /**
     * Read and return the next row of data.
     *
     * @return If there is no more data to be read, then null;
     * otherwise, an array of string cell values, one per column.
     */
    String[] readDataRow() throws AnaplanAPIException, IOException;

    /**
     * Complete the transfer. Any resources associated with the reader instance
     * are released.
     */
    void close() throws AnaplanAPIException, IOException;
}

