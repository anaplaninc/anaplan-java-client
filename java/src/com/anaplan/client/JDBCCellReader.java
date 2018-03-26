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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
  * An implementation of CellReader that connects to a JDBC data source.
  * A query is executed and the results are read one record at a time.
  * @since 1.2
  */
public class JDBCCellReader implements CellReader {
    private static final Logger logger = Logger.getLogger("anaplan-connect.jdbc");
    private Connection connection;
    private boolean autoCommit;
    private Statement statement;
    private ResultSet resultSet;
    private int columnCount;
    private String[] headerRow;
    private int[] columnTypes;
    private boolean debug;

    /**
     * Create a new JDBC cell reader by connecting to a database and performing a query.
     * @param jdbcUrl The JDBC URL of the database to connect to
     * @param jdbcProperties The properties to use for the connection
     * @param query The query to perform
     */
    public JDBCCellReader(String jdbcUrl, Properties jdbcProperties, String query) throws SQLException {
        this(jdbcUrl, jdbcProperties, query, null, false);
    }

    /**
     * Create a new JDBC cell reader by connecting to a database and performing a query.
     * @param jdbcUrl The JDBC URL of the database to connect to
     * @param jdbcProperties The properties to use for the connection
     * @param query The query to perform
     * @param fetchSize Hint to JDBC driver to fetch results 
     * @param debug Report to Java logging any non-fatal errors produced by JDBC driver
     */
    public JDBCCellReader(String jdbcUrl, Properties jdbcProperties, String query, Integer fetchSize, boolean debug) throws SQLException {
        connection = DriverManager.getConnection(jdbcUrl, jdbcProperties);
        try {
            connection.setAutoCommit(false);
            autoCommit = false;
        } catch (SQLException sqle) {
            autoCommit = true;
            if (debug) {
                logger.warning("Warning: setAutoCommit failed(" + sqle + ")");
            }
        }
        statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        if (fetchSize != null) {
            try {
                statement.setFetchSize(fetchSize);
            } catch (SQLException sqle) {
                if (debug) {
                    logger.warning("Warning: setFetchSize failed(" + sqle + ")");
                }
            }
        }
        resultSet = statement.executeQuery(query);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        columnCount = resultSetMetaData.getColumnCount();
        headerRow = new String[columnCount];
        columnTypes = new int[columnCount];
        for (int i = 0; i < columnCount; ++i) {
            headerRow[i] = resultSetMetaData.getColumnLabel(i + 1);
            columnTypes[i] = resultSetMetaData.getColumnType(i + 1);
        }
    }
    /** {@inheritDoc} */
    @Override
    public String[] getHeaderRow() {
        return headerRow;
    }
    /** {@inheritDoc} */
    @Override
    public String[] readDataRow() throws AnaplanAPIException {
        try {
            if (resultSet == null || !resultSet.next()) return null;
            String[] dataRow = new String[columnCount];
            for (int i = 0; i < columnCount; ++i) {
                Object val = resultSet.getObject(i + 1);
                dataRow[i] = (val == null ? "" : val.toString());
            }
            return dataRow;
        } catch (SQLException sqle) {
            throw new AnaplanAPIException("Failed to retrieve result data", sqle);
        }
    }
    /** {@inheritDoc} */
    @Override
    public void close() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException sqle) {
                if (debug) {
                    logger.warning("Warning: failed to close result set: " + sqle);
                }
            }
            resultSet = null;
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException sqle) {
                if (debug) {
                    logger.warning("Warning: failed to close prepared statement: " + sqle);
                }
            }
            statement = null;
        }
        boolean closed = false;
        try {
            closed = connection.isClosed();
        } catch (SQLException sqle) {
            logger.warning("Warning: failed to determine if JDBC connection closed: " + sqle);
        }
        if (connection != null && !closed) {
            if (!autoCommit) {
                try {
                    connection.commit();
                } catch (SQLException sqle) {
                    if (debug) {
                        logger.warning("Warning: failed to commit JDBC connection: " + sqle);
                    }
                }
            }
            try {
                connection.close();
            } catch (SQLException sqle) {
                if (debug) {
                    logger.warning("Warning: failed to close JDBC connection: " + sqle);
                }
            }
        }
        connection = null;
    }
}
