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
import java.io.InputStream;
import java.sql.SQLException;

/**
 * Abstract sink for tabulated cell data.
 *
 * @since 1.2
 */
public interface CellWriter {
    /**
     * Write the header row.
     * This should be called once, before any calls to writeDataRow.
     *
     * @param row An array of string header values, one per column
     */
    void writeHeaderRow(Object[] row) throws AnaplanAPIException, IOException;

    /**
     * Write a data row.
     * This should be called after writeHeaderRow, for each line of data.
     *
     * @param row An array of string cell values, one per column
     */
    void writeDataRow(Object[] row) throws AnaplanAPIException, IOException, SQLException;

    /**
     * Write a data row.
     * This should be called after writeHeaderRow, for each line of data.
     *
     * @param separator An array of string cell values, one per column
     */

    int writeDataRow(String exportId,int maxRetryCount,int retryTimeout, InputStream inputStream,int chunks,String chunkId, int[] mapcols, int columnCount, String separator) throws AnaplanAPIException, IOException, SQLException;

    /**
     * Complete the transfer. Any remaining data is transferred,
     * and the transfer is marked as complete at the recipient end.
     */
    void close() throws AnaplanAPIException, IOException;

    /**
     * Abort the transfer. Any resources associated with the reader instance
     * are released.
     */
    void abort() throws AnaplanAPIException, IOException;
}
