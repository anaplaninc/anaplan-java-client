package com.anaplan.client.jdbc;

/**
 * NOTE: There is no decisive way to figure out if the provided query is a Stored-Procedure/Function
 * call, or a regular SELECT query.
 */

import com.anaplan.client.CellWriter;
import com.anaplan.client.Constants;
import com.anaplan.client.Utils;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.exceptions.AnaplanRetryableException;
import com.anaplan.client.exceptions.TooLongQueryError;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of CellWriter that connects to a JDBC data source.
 * A query is executed and the results are written to the database one record at a time.
 *
 * @since 1.4.2
 */

public class JDBCCellWriter implements CellWriter {

  private static final Logger LOG = LoggerFactory.getLogger(JDBCCellWriter.class);
  private static final int MAX_ALLOWED_SQL_CHARACTERS = 65535;
  private Connection connection;
  private boolean autoCommit;
  private Statement statement;
  private ResultSet resultSet;
  private JDBCConfig jdbcConfig;
  private String lastRow;
  private int batchNo;
  private int datarowstransferred = 0;
  private int batchRecords = 0;
  private int batchSize = 1000;
  private int update = 0;
  private int notUpdate = 0;
  private PreparedStatement preparedStatement = null;

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

  /**
   * Nothing to do
   * @param row An array of string header values, one per column
   * @throws AnaplanAPIException
   */
  @Override
  public void writeHeaderRow(Object[] row) throws AnaplanAPIException {
    //Nothing to do
  }

  /**
   * Nothing to do
   * @param row An array of string cell values, one per column
   * @throws AnaplanAPIException
   */
  @Override
  public void writeDataRow(Object[] row) throws AnaplanAPIException {
    //Nothing to do
  }

  /**
   * Write Anaplan exported data to the configurable DB
   *
   * @param dataRow
   * @return
   * @throws AnaplanAPIException
   * @throws SQLException
   */

  @Override
  public int writeDataRow(final DataRow dataRow)
      throws AnaplanAPIException, SQLException {
    Utils.checkSeparator(dataRow.getSeparator());
    JDBCUtils.validateURL(jdbcConfig);
    final CSVFormat anaplanCSVFormat = CSVFormat.RFC4180.builder().setDelimiter(dataRow.getSeparator().charAt(0)).build();
    try (LineNumberReader lnr = new LineNumberReader(
        new InputStreamReader(dataRow.getInputStream()))) {
      String line;
      boolean rowBatchRemoved = false;
      List<String[]> rowBatch = new ArrayList<>();
      while (null != (line = lnr.readLine())) {
        String[] row;
        // ignore the header
        if (lnr.getLineNumber() == 1 && dataRow.getChunkId().equals("0")) {
          LOG.info("Export {} to database started successfully", dataRow.getExportId());
          continue;
        }
        // adding a fix to handle the case when the chunk ends with a complete record
        if (lnr.getLineNumber() == 1 && !(dataRow.getChunkId().equals("0"))) {
          String temp = lastRow.concat(line);
          if (temp.split(dataRow.getSeparator()).length == dataRow.getColumnCount()) {
            line = temp;
          } else {
            row = lastRow.split(dataRow.getSeparator());
            rowBatch.add(row);
            ++batchRecords;
          }
        }
        List<String> dataRowStr;
        //If the row have an odd number of quotes the row have multiple lines and we have to read line by line until quotes are even (closed)
        boolean oddQuote = JDBCUtils.occurrence(line, JDBCUtils.QUOTE_REGEX_PATTERN) % 2 == 1;
        dataRowStr = oddQuote ? JDBCUtils.addDelimiter(lnr, anaplanCSVFormat, line) : JDBCUtils.getListFromParse(anaplanCSVFormat, line);
        row = dataRowStr.toArray(new String[0]);
        rowBatch.add(row);
        lastRow = line;
        RowBatchComplete rowBatchComplete = checkEndingIncomplete(dataRow, rowBatch, row, rowBatchRemoved);
        rowBatchRemoved = rowBatchComplete.isRowRemoved();
        rowBatch = rowBatchComplete.getRowBatch();
      }
      isLastChunk(dataRow, rowBatch);
      //transfer the last batch when all of the lines from inputstream have been read
      ++batchNo;
      batchExecution(rowBatch, dataRow.getColumnCount(), dataRow.getMapcols(), dataRow.getMaxRetryCount(),
          dataRow.getRetryTimeout());
      batchRecords = 0;
      //batch update exceptions captured to determine the committed and failed records
    } catch (Exception e) {
      final String log = Arrays.toString(e.getStackTrace());
      LOG.debug("Error observed  : {}", log);
      throw new AnaplanAPIException(e.getMessage());
    } finally {
      Utils.closeStatement(preparedStatement);
    }
    return datarowstransferred;
  }
  private void isLastChunk(final DataRow dataRow, final List<String[]> rowBatch) {
    //Check to make sure it is not the last chunk
    //removing the last record so that it can be processed in the next chunk
    if (Integer.parseInt(dataRow.getChunkId()) != dataRow.getChunks() - 1 && !rowBatch.isEmpty()) {
      rowBatch.remove(rowBatch.size() - 1);
    }
  }

  private RowBatchComplete checkEndingIncomplete(final DataRow dataRow, List<String[]> rowBatch, final String[] row, boolean rowRemoved)
      throws SQLException {
    boolean rowBatchRemoved = rowRemoved;
    if (++batchRecords % batchSize == 0) {
      ++batchNo;
      //If the last row in the chunk is ending with an incomplete last column value, there will be
      //duplicate records. To avoid this, we are removing it from the rowbatch.
      if (Integer.parseInt(dataRow.getChunkId()) != dataRow.getNoOfChunks() - 1 && !rowBatch.isEmpty()) {
        rowBatch.remove(rowBatch.size() - 1);
        rowBatchRemoved = true;
      }
      // for this batch, code should have dummy values to bypass the check for chunkId and no of chunks
      // chunkId is being sent as 1 and no of chunks as 2 to bypass the check
      batchExecution(rowBatch, dataRow.getColumnCount(), dataRow.getMapcols(), dataRow.getMaxRetryCount(), dataRow.getRetryTimeout());
      rowBatch = new ArrayList<>();
      if (rowBatchRemoved) {
        rowBatch.add(row);
        rowBatchRemoved = false;
        batchRecords = 1;
      } else {
        batchRecords = 0;
      }
    }
    return new RowBatchComplete(rowBatch , rowBatchRemoved);
  }
  /**
   * execute the batch and get the update count of records
   *
   * @param maxRetryCount
   * @param retryTimeout
   */
  private void batchExecution(List<String[]> rowBatch, int columnCount, int[] mapcols,
                              int maxRetryCount, int retryTimeout) throws SQLException {
    int k = 0; //retry count
    boolean retry;
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

        final BatchResult batchResult = execute(rowBatch, rowBatchSize, maxRetryCount);
        retry = batchResult.isRetry();
        if (!batchResult.isRetry()) {
          k = 0;
          retry = false;
        }

        rowBatchSize = batchResult.getRowBatchSize();
        rowBatch = batchResult.getRowBatch();

      } catch (AnaplanRetryableException ae) {
        retry = true;
      }
    } while (k < maxRetryCount && retry);
    // not successful
    if (retry) {
      throw new AnaplanAPIException(
          "Could not connect to the database after " + maxRetryCount + " retries");
    }
  }

  private BatchResult execute(List<String[]> rowBatch, final int rowBatchSize, final int maxRetryCount)
      throws AnaplanRetryableException, SQLException {
    try {
      executeBatch(rowBatchSize, maxRetryCount);
      batchRecords = 0;
      datarowstransferred = datarowstransferred + update;
      update = 0;
      notUpdate = 0;
    } catch (BatchUpdateException buex) {
      return solveBatchException(buex, rowBatchSize, rowBatch);
    }
    return new BatchResult(rowBatch, rowBatchSize, false);
  }

  private BatchResult solveBatchException(BatchUpdateException buex, int rowBatchSize, List<String[]> rowBatch) throws SQLException {
    // Rollback commit, remove failed entries from rowBatch, and try again.
    connection.rollback();
    int[] updateCounts = buex.getUpdateCounts();
    ArrayList<Integer> erroredRows = new ArrayList<>();
    ArrayList<String[]> newRowBatch = new ArrayList<>();

    // Oracle returns status only for the rows that succeeded, MySQL for every row. We need to get the right
    // amount of errored rows in either case.
    int attemptedInserts = Math.max(updateCounts.length, rowBatchSize);
    for (int i = 0; i < attemptedInserts; i++) {
      if (i >= updateCounts.length || updateCounts[i] == Statement.EXECUTE_FAILED) {
        ++notUpdate;
        erroredRows.add(i);
      } else {
        newRowBatch.add(rowBatch.get(i));
      }
    }

    final BatchResult result = new BatchResult(newRowBatch, newRowBatch.size(), true);
    final String logInfo = erroredRows.stream().
        map(Object::toString).
        collect(Collectors.joining(","));
    LOG.info("The following rows errored when inserting into the database: {}", logInfo);
    LOG.info("Trying batch {} again without errored rows", batchNo);

    return result;
  }
  /**
   * prepare the batch when line ends with a separator
   *
   * @param mapcols
   * @param columnCount
   * @param row
   * @throws SQLException
   */
  private void psLineEndWithSeparator(int[] mapcols, int columnCount, String[] row)
      throws SQLException {
    //handling the last column if the value is null in anaplan
    if (jdbcConfig.getJdbcParams() != null && jdbcConfig.getJdbcParams().length > 0
        && !jdbcConfig.getJdbcParams()[0].equals("") && mapcols.length != 0) {
      setParamForStatement(mapcols, row);
    } else {
      setEmptyParamForStatement(columnCount, row);
    }
  }

  private void setParamForStatement(int[] mapcols, String[] row) throws SQLException {
    for (int i = 0; i < mapcols.length; i++) {
      if (i == mapcols.length - 1 && row.length < mapcols.length) {
        preparedStatement.setString(i + 1, "");
      } else {
        preparedStatement
            .setString(i + 1,
                !String.valueOf(row[mapcols[i]]).equals("") ? String.valueOf(row[mapcols[i]]) : "");
      }
    }
  }

  private void setEmptyParamForStatement(int columnCount, String[] row) throws SQLException {
    for (int i = 0; i < columnCount; i++) {
      if (i == columnCount - 1 && row.length < columnCount) {
        preparedStatement.setString(i + 1, "");
      } else {
        preparedStatement
            .setString(i + 1, !String.valueOf(row[i]).equals("") ? String.valueOf(row[i]) : "");
      }
    }
  }


  /**
   * execute the batch
   *
   * @param
   * @return
   */
  private int[] executeBatch(int batchRecords, int maxRetryCount)
      throws AnaplanRetryableException, BatchUpdateException {
    int k = 0;
    int[] count = new int[batchSize];
    boolean retry;
    do {
      k++;
      try {
        if (k == 1) {
          LOG.info("Writing batch {} ({} records)", batchNo, batchRecords);
        }
        count = preparedStatement.executeBatch();
        connection.commit();

        Arrays.stream(count).forEach(i -> {
          if (i == 1) {
            ++update;
          } else {
            ++notUpdate;
          }
        });
        LOG.info("Batch {} written ({} records committed, {} records errored out)", batchNo,
            update,
            notUpdate);
        return count;
      } catch (BatchUpdateException buex) {
        retry = logBatchUpdateException(buex);
      } catch (SQLException e) {
        throw new AnaplanRetryableException(e.getMessage());
      }
    } while (k < maxRetryCount && retry);
    return count;
  }
  
  public boolean logBatchUpdateException(final BatchUpdateException buex) throws BatchUpdateException {
    LOG.debug("Exception observed during batch update : {}", buex.getMessage());
    if (buex.getSQLState().equals("08003")) {
      LOG.debug("Network Issue : {}", buex.getMessage());
    } else {
      throw buex;
    }
    return true;
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
              jdbcConfig.getJdbcUsername(), new String(jdbcConfig.getJdbcPassword()));
          connection.setAutoCommit(false);
          LOG.info("Created JDBC connection to: {}", connection.getMetaData().getURL());
          retry = false;
        }
      } catch (SQLException e) {
        if (e.getSQLState().equals("08001")) {
          final String logError = e.getMessage();
          LOG.error("Could not connect to the database: {}", logError);
          throw new AnaplanAPIException(
              "Could not connect to the database. Please check the connection URL in your JDBC properties and " +
                  "make sure you have a JDBC driver installed."
          );
        }

        sleepNoNetwork(maxRetryCount, retryTimeout, k);
        retry = true;
      }
    } while (k < maxRetryCount && retry);
    // if not successful after configured no of max retries
    if (retry) {
      throw new AnaplanAPIException(
          "Could not connect to the database after " + maxRetryCount + " retries");
    }
  }

  /**
   * common sleep process for database retries
   *
   * @param maxRetryCount
   * @param k
   */
  private void sleepNoNetwork(int maxRetryCount, int retryTimeout, int k) {
    Long interval = (long) retryTimeout * 1000;
    AnaplanJdbcRetryer anaplanJdbcRetryer =
        new AnaplanJdbcRetryer(retryTimeout * 1000L, Constants.MAX_RETRY_TIMEOUT_SECS * 1000L,
            Constants.DEFAULT_BACKOFF_MULTIPLIER);
    if (k > 1) {
      interval = anaplanJdbcRetryer.nextMaxInterval(k - 1);
    }
    if (k < maxRetryCount) {
      LOG.info("Attempt {} : Could not connect to the database! Will retry in {} seconds ", k,
          interval / 1000);
      try {
        Thread.sleep(interval);
      } catch (InterruptedException e1) {
        // we still want to retry, even though sleep was interrupted
        LOG.debug("Sleep was interrupted. {0}", e1);
        Thread.currentThread().interrupt();
      }
    }
    if (k == maxRetryCount) {
      LOG.info("Attempt {} : Could not connect to the database! Max connection attempts reached..",
          k);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws AnaplanAPIException {
    Utils.closeJDBC(resultSet, statement);
    resultSet = null;
    statement = null;
    boolean closed = false;
    try {
      if (connection != null) {
        closed = connection.isClosed();
      }
    } catch (SQLException sqle) {
      LOG.error("Failed to determine if JDBC connection closed: {0}", sqle);
    }
    if (connection != null && !closed) {
      if (!autoCommit) {
        try {
          connection.commit();
        } catch (SQLException sqle) {
          LOG.error("Failed to commit JDBC connection: {0}", sqle);
        }
      }
      try {
        connection.close();
      } catch (SQLException sqle) {
        LOG.warn("Failed to close JDBC connection: {0}", sqle);
      }
    }
    connection = null;
  }


  /**
   * Nothing to do
   * @throws AnaplanAPIException
   */
  @Override
  public void abort() throws AnaplanAPIException {
    //Nothing to do
  }

  class BatchResult {
    private List<String[]> rowBatch;
    private int rowBatchSize;
    private boolean retry;

    BatchResult(List<String[]> rowBatch, int rowBatchSize, boolean retry){
      this.rowBatch = rowBatch;
      this.rowBatchSize = rowBatchSize;
      this.retry = retry;
    }

    public List<String[]> getRowBatch() {
      return rowBatch;
    }

    public int getRowBatchSize() {
      return rowBatchSize;
    }

    public boolean isRetry() {
      return retry;
    }
  }

  class RowBatchComplete {
    private List<String[]> rowBatch;
    private boolean rowRemoved;

    RowBatchComplete(List<String[]> rowBatch, boolean rowRemoved){
      this.rowBatch = rowBatch;
      this.rowRemoved = rowRemoved;
    }

    public List<String[]> getRowBatch() {
      return rowBatch;
    }

    public boolean isRowRemoved() {
      return rowRemoved;
    }
  }
}
