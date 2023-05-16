package com.anaplan.client.jdbc;

import static com.anaplan.client.Constants.BATCH_SIZE;

import com.anaplan.client.ListImpl;
import com.anaplan.client.ListImpl.MetaContent;
import com.anaplan.client.dto.ListFailure;
import com.anaplan.client.dto.ListItemParametersData;
import com.anaplan.client.dto.ListItemResultData;
import java.io.IOException;
import java.io.LineNumberReader;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.LoggerFactory;

/**
 * Jdbc related utilities, e.g. connection string checks
 */
public class JDBCUtils {

  private JDBCUtils(){}
  private static final Pattern QUERY_INTERCEPTOR = Pattern
      .compile("QUERYINTERCEPTOR=", Pattern.CASE_INSENSITIVE);
  private static final Pattern AUTO_DESERIALIZE = Pattern
      .compile("AUTODESERIALIZE=TRUE", Pattern.CASE_INSENSITIVE);
  //Anaplan export special characters inside ""
  public static final Pattern QUOTE_REGEX_PATTERN = Pattern.compile("[^\"]*\"");

  private static final int MAX_ALLOWED_CONNECTION_STRING_LENGTH = 1500;
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JDBCUtils.class);

  //Set to false positive
  public static void validateURL(final JDBCConfig config) {
    final String url =
        config.getJdbcConnectionUrl() == null ? null : config.getJdbcConnectionUrl().toUpperCase();
    if (url == null) {
      throw new InvalidParameterException(
          "JDBC connection string cannot be empty !");
    }
    if (url.length() > MAX_ALLOWED_CONNECTION_STRING_LENGTH) {
      throw new InvalidParameterException(
          "JDBC connection string cannot be more than " + MAX_ALLOWED_CONNECTION_STRING_LENGTH
              + " characters in length!");
    }
    //Injection vulnerability
    if (QUERY_INTERCEPTOR.matcher(url).find() || AUTO_DESERIALIZE.matcher(url).find()) {
      throw new InvalidParameterException(
          "JDBC connection string is not valid !");
    }
    if (config.getJdbcPassword() == null || config.getJdbcPassword().length == 0) {
      LOG.warn("JDBC password is empty !");
    }

  }
  static int occurrence(final String line,final Pattern pattern) {
    final Matcher matcher = pattern.matcher(line);
    int count = 0;
    while (matcher.find()) {
      count++;
    }
    return count;
  }

  static List<String> addDelimiter(final LineNumberReader lnr,
      final CSVFormat anaplanCSVFormat, String line) throws IOException {
    if (line == null) {
      line = "";
    }
    boolean oddQuote = false;
    String li = "";
    while (li != null && !oddQuote) {
      li = lnr.readLine();
      if (li != null) {
        line = line.concat("\n").concat(li);
        oddQuote = (occurrence(li, QUOTE_REGEX_PATTERN) % 2 == 1);
      }
    }
    return getListFromParse(anaplanCSVFormat, line);
  }

  /**
   *
   * @param anaplanCSVFormat csv format
   * @param line to be parsed
   * @return parsed list
   * @throws IOException exception
   */
  public static List<String> getListFromParse(final CSVFormat anaplanCSVFormat, String line)
      throws IOException {
    final List<String> dataList = new ArrayList<>();
    final CSVParser csvParser = CSVParser.parse(line, anaplanCSVFormat);
    final List<CSVRecord> records = csvParser.getRecords();
    if (!records.isEmpty()) {
      CSVRecord csvRecord = records.get(0);
      for (String cellValue : csvRecord) {
        dataList.add(cellValue);
      }
    }
    return dataList;
  }

  /**
   * Add and Delete items from JDBC
   * @param jdbcConfig JDBC config
   * @param listImpl List Impl
   * @param headerMap Header Map
   * @param action Action to perform
   * @return result listitem result
   * @throws SQLException exception
   */
  public static ListItemResultData doActionsItemsFromJDBC(final JDBCConfig jdbcConfig, final ListImpl listImpl, final Map<String, String> headerMap,
      final ListImpl.ListAction action, final boolean doMapping)
      throws SQLException, IOException {
    return doActionsItemsFromJDBC(jdbcConfig, listImpl, headerMap, action, BATCH_SIZE, doMapping);
  }
  /**
   * Add and Delete items from JDBC
   * @param jdbcConfig JDBC Config
   * @param listImpl List Impl
   * @param headerMap Header Map
   * @param action Action to perform
   * @param batchSize BatchSize
   * @return result listitem result
   * @throws SQLException sqlException
   */
  public static ListItemResultData doActionsItemsFromJDBC(final JDBCConfig jdbcConfig, final ListImpl listImpl, final Map<String, String> headerMap,
      final ListImpl.ListAction action, final long batchSize, final boolean doMapping)
      throws SQLException, IOException {
    ListItemResultData result = new ListItemResultData();
    result.setFailures(new ArrayList<>(0));
    String query = jdbcConfig.getJdbcQuery();
    int index = 0;
    JDBCCellReader cellReader = null;
    final MetaContent metaContent = listImpl.getContent();
    boolean doBreak = false;
    try {
      while (true) {
        String queryToAdd = query.concat(
            " limit ".concat(String.valueOf(batchSize)).concat(" offset ").concat(
                String.valueOf(index * batchSize)));
        jdbcConfig.setJdbcQuery(queryToAdd);

        cellReader = new JDBCCellReader(jdbcConfig)
            .connectAndExecute();
        String[] row;
        final String[] header = cellReader.getHeaderRow();
        if (doMapping && headerMap != null) {
          listImpl.verifyHeaderMapping(header, headerMap, metaContent.getPropNames(),
              metaContent.getSubsets());
        }
        final List<String[]> items = new ArrayList<>();
        while (null != (row = cellReader.readDataRow()) && row.length > 0) {
          items.add(row);
        }
        if (items.isEmpty()) {
          doBreak = true;
        } else {
          final ListItemData listItemData = getResultAction(action, listImpl, header, headerMap,
              items);
          final ListItemResultData resultAction = listItemData.getResultAction();

          getFailures(listItemData, result);
          result.setIgnored(result.getIgnored() + resultAction.getIgnored());
          result.setDeleted(result.getDeleted() + resultAction.getDeleted());
          result.setAdded(result.getAdded() + resultAction.getAdded());
          result.setUpdated(result.getUpdated() + resultAction.getUpdated());
          if (items.size() < batchSize) {
            doBreak = true;
          }
          index++;
        }
        if (doBreak) {
          break;
        }
      }
    } finally {
      if (cellReader != null) {
        cellReader.close();
      }
    }
    return result;
  }

  static void getFailures(final ListItemData listItemData, final ListItemResultData result ) {
    if (listItemData.getResultAction().getFailures() != null && listItemData.getListItemParametersData() != null) {
      for (final ListFailure listFailure : listItemData.getResultAction().getFailures()) {
        listFailure.setListItem(listItemData.getListItemParametersData().getItems().get(listFailure.getRequestIndex()));
        result.getFailures().add(listFailure);
      }
    }
  }

  static ListItemData getResultAction(final ListImpl.ListAction action, final ListImpl listImpl,
      final String[] header, final Map<String, String> headerMap, final List<String[]> items)
      throws IOException {
    ListItemResultData resultAction;
    ListItemParametersData listItemParametersData;
    switch (action){
      case ADD:
        listItemParametersData = listImpl.getListItemFromJDBC(header, headerMap, items);
        resultAction = listImpl.addItemsToList(listItemParametersData);
        break;
      case UPDATE:
        listItemParametersData = listImpl.getListItemFromJDBC(header, headerMap, items);
        resultAction = listImpl.updateItemsList(listItemParametersData);
        break;
      case DELETE:
        listItemParametersData = listImpl.getDeleteItems(items, header, headerMap);
        resultAction = listImpl.deleteItemsList(listItemParametersData);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + action);
    }
    return new ListItemData(listItemParametersData, resultAction);
  }

  static class ListItemData {
    ListItemParametersData listItemParametersData;
    ListItemResultData resultAction;

    public ListItemData (ListItemParametersData listItemParametersData, ListItemResultData resultAction) {
      this.listItemParametersData = listItemParametersData;
      this.resultAction = resultAction;
    }
    public ListItemParametersData getListItemParametersData() {
      return listItemParametersData;
    }

    public ListItemResultData getResultAction() {
      return resultAction;
    }
  }
}
