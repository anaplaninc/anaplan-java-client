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

package com.anaplan.client.jdbc;

import com.anaplan.client.CellReader;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.TooLongQueryError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * NOTE: There is no decisive way to figure out if the provided query is a
 * Stored-Procedure/Function call, or a regular SELECT query.
 */

/**
 * An implementation of CellReader that connects to a JDBC data source.
 * A query is executed and the results are read one record at a time.
 *
 * @since 1.2
 */
public class JDBCCellReader implements CellReader {
    private static final Logger LOG = LoggerFactory.getLogger(JDBCCellReader.class);
    private static final int MAX_ALLOWED_SQL_CHARACTERS = 65535;
    private static final int MAX_ALLOWED_CONNECTION_STRING_LENGTH = 1500;
    private Connection connection;
    private boolean autoCommit;
    private Statement statement;
    private ResultSet resultSet;
    private int columnCount;
    private String[] headerRow;
    private int[] columnTypes;
    private JDBCConfig jdbcConfig;

    /**
     * Create a new JDBC cell reader by connecting to a database and performing a query.
     * @param jdbcConfig
     */
    public JDBCCellReader(JDBCConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
        String rawJdbcQuery = jdbcConfig.getJdbcQuery();
        this.jdbcConfig.setJdbcQuery(sanitizeQuery(rawJdbcQuery));
    }

    /**
     * Fetches the connection using provided JDBC parameters, and create the
     * prepared-statements/call for the provided SQL query and query parameters.
     *
     * @return
     * @throws SQLException If something goes wrong while creating the query,
     *                      plugging in the parameters, or executing it.
     */
    public JDBCCellReader connectAndExecute() throws SQLException {
        if (jdbcConfig.getJdbcConnectionUrl().length() > MAX_ALLOWED_CONNECTION_STRING_LENGTH) {
            throw new InvalidParameterException("JDBC connection string cannot be more than " + MAX_ALLOWED_CONNECTION_STRING_LENGTH + " characters in length!");
        }
        try {
            connection = DriverManager.getConnection(jdbcConfig.getJdbcConnectionUrl(),
                    jdbcConfig.getJdbcUsername(),
                    jdbcConfig.getJdbcPassword());
            connection.setAutoCommit(false);
            autoCommit = false;
            LOG.info("Created JDBC connection to: {}", connection.getMetaData().getURL());
        } catch (SQLException e) {
            throw new AnaplanAPIException("Could not connect to database!", e);
        }

        if (jdbcConfig.isStoredProcedure()) {
            CallableStatement callableStatement = connection.prepareCall(jdbcConfig.getJdbcQuery(),
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (jdbcConfig.getJdbcParams() != null && jdbcConfig.getJdbcParams().length > 0 && !jdbcConfig.getJdbcParams()[0].equals("")) {
                for (int i = 0; i < jdbcConfig.getJdbcParams().length; i++) {
                    callableStatement.setString(i + 1, String.valueOf(jdbcConfig.getJdbcParams()[i]));
                }
            }
            LOG.debug("Running provided stored-procedure: '{}'", jdbcConfig.getJdbcQuery());
            setJdbcFetchSize(callableStatement);
            resultSet = callableStatement.executeQuery();
            statement = callableStatement;
        } else {
            PreparedStatement preparedStatement = connection.prepareStatement(jdbcConfig.getJdbcQuery(),
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (jdbcConfig.getJdbcParams() != null && jdbcConfig.getJdbcParams().length > 0 && !jdbcConfig.getJdbcParams()[0].equals("")) {
                for (int i = 0; i < jdbcConfig.getJdbcParams().length; i++) {
                    preparedStatement.setString(i + 1, String.valueOf(jdbcConfig.getJdbcParams()[i]));
                }
            }
            LOG.debug("Running provided query: '{}'", jdbcConfig.getJdbcQuery());
            setJdbcFetchSize(preparedStatement);
            resultSet = preparedStatement.executeQuery();
            statement = preparedStatement;
        }
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        columnCount = resultSetMetaData.getColumnCount();
        headerRow = new String[columnCount];
        columnTypes = new int[columnCount];
        for (int i = 0; i < columnCount; ++i) {
            headerRow[i] = resultSetMetaData.getColumnLabel(i + 1);
            columnTypes[i] = resultSetMetaData.getColumnType(i + 1);
        }
        return this;
    }

    private void setJdbcFetchSize(Statement statement) {
        if (jdbcConfig.getJdbcFetchSize() != null) {
            try {
                statement.setFetchSize(jdbcConfig.getJdbcFetchSize());
            } catch (SQLException sqle) {
                LOG.error("Warning: setFetchSize failed(" + sqle + ")");
            }
        }
    }

    /**
     * Checks if the provided SQL query is sanitary:
     * - not greater than MAX_ALLOWED_SQL_CHARACTERS =
     *
     * @param query
     * @return
     */
    private String sanitizeQuery(String query) {
        if (query.length() >= MAX_ALLOWED_SQL_CHARACTERS) {
            throw new TooLongQueryError(query.length());
        }
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getHeaderRow() {
        return headerRow;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException sqle) {
                LOG.error("Failed to close result set: " + sqle);
            }
            resultSet = null;
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException sqle) {
                LOG.error("Failed to close prepared statement: " + sqle);
            }
            statement = null;
        }
        boolean closed = false;
        try {
            closed = connection.isClosed();
        } catch (SQLException sqle) {
            LOG.error("Failed to determine if JDBC connection closed: " + sqle);
        }
        if (connection != null && !closed) {
            if (!autoCommit) {
                try {
                    connection.commit();
                } catch (SQLException sqle) {
                    LOG.error("Failed to commit JDBC connection: " + sqle);
                }
            }
            try {
                connection.close();
            } catch (SQLException sqle) {
                LOG.warn("Failed to close JDBC connection: " + sqle);
            }
        }
        connection = null;
    }
}
