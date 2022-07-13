package com.anaplan.client.jdbc;

/**
 * NOTE: There is no decisive way to figure out if the provided query is a
 * Stored-Procedure/Function call, or a regular SELECT query.
 */

import com.anaplan.client.CellWriter;
import com.anaplan.client.Constants;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.AnaplanRetryableException;
import com.anaplan.client.ex.TooLongQueryError;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.retryer.AnaplanJdbcRetryer;
import com.anaplan.client.transport.retryer.FeignApiRetryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.InvalidParameterException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of CellWriter that connects to a JDBC data source.
 * A query is executed and the results are written to the database one record at a time.
 *
 * @since 1.4.2
 */

public class JDBCCellWriter implements CellWriter {
    private static final Logger LOG = LoggerFactory.getLogger(JDBCCellWriter.class);
    private static final int MAX_ALLOWED_SQL_CHARACTERS = 65535;
    private static final int MAX_ALLOWED_CONNECTION_STRING_LENGTH = 1500;
    private Connection connection;
    private ConnectionProperties properties;
    private boolean autoCommit;
    private Statement statement;
    private ResultSet resultSet;
    private JDBCConfig jdbcConfig;
    private String lastRow;
    private int batch_no;
    private int datarowstransferred = 0;
    private int batch_records = 0;
    private int batch_size = 1000;
    private int update = 0;
    private int not_update = 0;
    private PreparedStatement preparedStatement = null;
    private List<String[]> tempRowList = new ArrayList<>(); //to save rows so that we can reuse in case of retry.

    public JDBCCellWriter(JDBCConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
        String rawJdbcQuery = jdbcConfig.getJdbcQuery();
        this.jdbcConfig.setJdbcQuery(sanitizeQuery(rawJdbcQuery));
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

    @Override
    public void writeHeaderRow(Object[] row) throws AnaplanAPIException, IOException {
    }

    @Override
    public void writeDataRow(Object[] row) throws AnaplanAPIException, IOException, SQLException {
    }

    /**
     * Write Anaplan exported data to the configurable DB
     *
     * @param exportId
     * @param maxRetryCount
     * @param retryTimeout
     * @param inputStream
     * @param noOfChunks
     * @param chunkId
     * @param mapcols
     * @param columnCount
     * @param separator     An array of string cell values, one per column
     * @return
     * @throws AnaplanAPIException
     * @throws SQLException
     */

    @Override
    public int writeDataRow(String exportId, int maxRetryCount, int retryTimeout, InputStream inputStream, int noOfChunks, String chunkId, int[] mapcols, int columnCount, String separator)
            throws AnaplanAPIException, SQLException {
        if (jdbcConfig.getJdbcConnectionUrl().length() > MAX_ALLOWED_CONNECTION_STRING_LENGTH) {
            throw new InvalidParameterException("JDBC connection string cannot be more than " + MAX_ALLOWED_CONNECTION_STRING_LENGTH + " characters in length!");
        }
        try {
            LineNumberReader lnr = new LineNumberReader(
                    new InputStreamReader(inputStream));
            String line;
            List rowBatch = new ArrayList<>();
            while (null != (line = lnr.readLine())) {
                String[] row;
                //ignore the header
                if (lnr.getLineNumber() == 1 && chunkId.equals("0")) {
                    LOG.info("Export {} to database started successfully", exportId);
                } else {
                    //adding a fix to handle the case when the chunk ends with a complete record
                    if (lnr.getLineNumber() == 1 && !(chunkId.equals("0"))) {
                        String temp = lastRow.concat(line);
                        if (temp.split(separator).length == columnCount) {
                            line = temp;
                        } else {
                            row = lastRow.split(separator);
                            rowBatch.add(row);
                        }
                    }
                    row = line.split(separator);
                    rowBatch.add(row);
                    lastRow = line;
                    if (++batch_records % batch_size == 0) {
                        ++batch_no;
                        //for this batch, code should have dummy values to bypass the check for chunkId and no of chunks
                        // chunkId is being sent as 1 and no of chunks as 2 to bypass the check
                        batchExecution(rowBatch, columnCount, mapcols, "1", 2, maxRetryCount, retryTimeout);
                        rowBatch = new ArrayList<>(); //reset temoRowList
                        batch_records = 0;
                    }
                }
            }
            //Check to make sure it is not the last chunk
            //removing the last record so that it can be processed in the next chunk
            if(Integer.parseInt(chunkId)!=noOfChunks-1 && rowBatch.size()>0) {
                rowBatch.remove(rowBatch.size() - 1);
            }
            //transfer the last batch when all of the lines from inputstream have been read
            ++batch_no;
            batchExecution(rowBatch, columnCount, mapcols,chunkId,noOfChunks, maxRetryCount, retryTimeout);
            batch_records = 0;
            //batch update exceptions captured to determine the committed and failed records
        }catch (Exception e) {
            LOG.debug("Error observed  : {}", e.getStackTrace());
            throw new AnaplanAPIException(e.getMessage());
        } finally {
            if (preparedStatement != null) {
                if (!preparedStatement.isClosed())
                    preparedStatement.close();
            }
        }
        return datarowstransferred;
    }

    /**
     * execute the batch and get the update count of records
     *
     * @param maxRetryCount
     * @param retryTimeout
     */
    private void batchExecution(List<String[]> rowBatch, int columnCount, int[] mapcols, String chunkId, int noOfChunks,
                                 int maxRetryCount, int retryTimeout) throws SQLException {
        int k = 0; //retry count
        boolean retry = false;
        int rowBatchSize = rowBatch.size();
        do {
            k++;
            try {
                if (connection == null || connection.isClosed()) {
                    getJdbcConnection(maxRetryCount, retryTimeout);
                }
                if (preparedStatement == null || preparedStatement.isClosed()) {
                    preparedStatement = connection.prepareStatement(jdbcConfig.getJdbcQuery(),
                            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                }
                for (int i = 0; i < rowBatchSize; i++) {
                    psLineEndWithSeparator(mapcols, columnCount, rowBatch.get(i));
                    preparedStatement.addBatch();
                }
                //  ++batch_no;
                executeBatch(rowBatchSize, maxRetryCount, retryTimeout);
                batch_records = 0;
                k = 0;
                retry = false;
                datarowstransferred = datarowstransferred + update;
                update = 0;
                not_update = 0;
            } catch (AnaplanRetryableException ae) {
                retry = true;
            }
        } while (k < maxRetryCount && retry);
        // not successful
        if (retry) {
            throw new AnaplanAPIException("Could not connect to the database after " + maxRetryCount + " retries");
        }
    }


    /**
     * @param mapcols
     * @param columnCount
     * @param row
     * @throws SQLException
     */
    //during code refactoring, usage of this method was removed. this will be removed once we QA the database exports.
    private void psLineNotEndWithSeparator(int[] mapcols, int columnCount, String[] row) throws SQLException {
        if (jdbcConfig.getJdbcParams() != null && jdbcConfig.getJdbcParams().length > 0
                && !jdbcConfig.getJdbcParams()[0].equals("") && mapcols.length != 0) {
            for (int i = 0; i < mapcols.length; i++) {
                preparedStatement.setString(i + 1, String.valueOf(row[mapcols[i]]) != "" ? String.valueOf(row[mapcols[i]]) : "");
            }
        } else {
            for (int i = 0; i < columnCount; i++) {
                preparedStatement.setString(i + 1, String.valueOf(row[i]) != "" ? String.valueOf(row[i]) : "");
            }
        }
    }

    /**
     * prepare the batch when line ends with a separator
     *
     * @param mapcols
     * @param columnCount
     * @param row
     * @throws SQLException
     */
    private void psLineEndWithSeparator(int[] mapcols, int columnCount, String[] row) throws SQLException {
        //handling the last column if the value is null in anaplan
        if (jdbcConfig.getJdbcParams() != null && jdbcConfig.getJdbcParams().length > 0
                && !jdbcConfig.getJdbcParams()[0].equals("") && mapcols.length != 0) {
            for (int i = 0; i < mapcols.length; i++) {
                if (i == mapcols.length - 1 && row.length<mapcols.length) {
                    preparedStatement.setString(i + 1, "");
                } else {
                    preparedStatement.setString(i + 1, String.valueOf(row[mapcols[i]]) != "" ? String.valueOf(row[mapcols[i]]) : "");
                }
            }
        } else {
            for (int i = 0; i < columnCount; i++) {
                if (i == columnCount - 1 && row.length<columnCount) {
                    preparedStatement.setString(i + 1, "");
                } else {
                    preparedStatement.setString(i + 1, String.valueOf(row[i]) != "" ? String.valueOf(row[i]) : "");
                }
            }
        }
    }

    /**
     * execute the batch
     *
     * @param
     * @return
     */
    private int[] executeBatch(int batch_records, int maxRetryCount, int retryTimeOut) throws AnaplanRetryableException {
        int k = 0;
        int[] count = new int[batch_size];
        boolean retry = false;
        int notAvailable = 0;
        do {
            k++;
            try {
                if (k == 1)
                    LOG.info("Writing batch {} ({} records)", batch_no, batch_records);
                count = preparedStatement.executeBatch();
                connection.commit();
                for (int x = 0; x < count.length; ++x) {
                    if (count[x] == 1) {
                        ++update;
                    } else {
                        ++not_update;
                    }
                }
                LOG.info("Batch {} written ({} records committed,{} records errored out)", batch_no, update, not_update);
                return count;
            } catch (BatchUpdateException buex) {
                LOG.debug("Exception observed during batch update : {}", buex.getStackTrace());
                int[] updateCounts = buex.getUpdateCounts();
                if (buex.getSQLState().equals("08003")) {
                    LOG.debug("Network Issue : {}",buex.getMessage());
                }
                update = 0;
                not_update = 0;
                if (buex.getSQLState().equals("23000")) {
                    LOG.debug("Primary Key Exception : {}", buex.getMessage());
                    for (int i = 0; i < updateCounts.length; i++) {
                        if (updateCounts[i] >= 0) {
                            update++;
                        } else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
                            not_update++;
                        }
                    }
                }
                retry = true;
                // batch_records = 0;
            } catch (SQLException e) {
                throw new AnaplanRetryableException(e.getMessage());
            }
        } while (k < maxRetryCount && retry);
        return count;
    }

    /**
     * Connects to DB with 5 retries
     */
    private void getJdbcConnection(int maxRetryCount, int retryTimeout) {
        int k = 0; //retry count
        boolean retry = false;
        do {
            k++;
            try {
                if (connection == null || connection.isClosed()) {
                    connection = DriverManager.getConnection(jdbcConfig.getJdbcConnectionUrl(),
                            jdbcConfig.getJdbcUsername(), jdbcConfig.getJdbcPassword());
                    connection.setAutoCommit(false);
                    LOG.info("Created JDBC connection to: {}", connection.getMetaData().getURL());
                    retry = false;
                }
            } catch (SQLException e) {
                sleepNoNetwork(maxRetryCount, retryTimeout, k);
                retry = true;
            }
        } while (k < maxRetryCount && retry);
        // if not successful after configured no of max retries
        if (retry) {
            throw new AnaplanAPIException("Could not connect to the database after " + maxRetryCount + " retries");
        }
    }

    /**
     * common sleep process for database retries
     *
     * @param maxRetryCount
     * @param k
     */
    private void sleepNoNetwork(int maxRetryCount, int retryTimeout, int k) {
        Long interval=(long)retryTimeout*1000;
        AnaplanJdbcRetryer anaplanJdbcRetryer = new AnaplanJdbcRetryer((long) (retryTimeout * 1000),
                (long) Constants.MAX_RETRY_TIMEOUT_SECS * 1000,
                FeignApiRetryer.DEFAULT_BACKOFF_MULTIPLIER);
        if (k>1) {
            interval = anaplanJdbcRetryer.nextMaxInterval(k-1);
        }
        if(k<maxRetryCount) {
            LOG.info("Attempt {} : Could not connect to the database! Will retry in {} seconds ", k, interval / 1000);
        try {
            Thread.sleep(interval);
        }catch (InterruptedException e1) {
            // we still want to retry, even though sleep was interrupted
            LOG.debug("Sleep was interrupted." + e1);
        }
        }
        if (k == maxRetryCount) {
            LOG.info("Attempt {} : Could not connect to the database! Max connection attempts reached..", k);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws AnaplanAPIException {
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
            if (connection != null)
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


    @Override
    public void abort() throws AnaplanAPIException, IOException {

    }
}
