package com.anaplan.client.listwriter;

import static com.anaplan.client.listwriter.ListItemFileWriter.valueOrEmpty;

import com.anaplan.client.dto.ListName;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility for exporting list names to csv
 */
public class ListNamesCsvWriter {

  private ListNamesCsvWriter(){}

  public static final int PARALLEL_THRESHOLD = 10000;
  protected static final String[] FIXED_COLUMN_NAMES = new String[]{"id", "name"};

  /**
   * For a list of list names retrieve a stream of csv export lines
   *
   * @param listNames Source names
   * @return Stream of csv lines
   */
  public static Stream<String> getLines(List<ListName> listNames) {
    if (listNames == null || !listNames.iterator().hasNext()) {
      return Stream.empty();
    }
    return Stream.concat(getColumnNames(), getColumnValues(listNames));
  }

  /**
   * Returns the list of column names to be exported as csv.
   *
   * @return Column names
   */
  public static Stream<String> getColumnNames() {
    return Stream.of(ListItemFileWriter.concat(FIXED_COLUMN_NAMES));
  }

  /**
   * From a List<ListName> retrieve the property values and concat them as a csv line string
   *
   * @param items The list of ListItems
   * @return Result as a stream of strings
   */
  public static Stream<String> getColumnValues(List<ListName> items) {
    return (items.size() > PARALLEL_THRESHOLD ? items.parallelStream() : items.stream())
        .map(ListNamesCsvWriter::getColumnValuesAsString);
  }

  /**
   * From a ListName retrieve the values to be exported as csv and concat them as a line string
   *
   * @param listName The list name
   * @return Result as a string
   */
  public static String getColumnValuesAsString(ListName listName) {
    String[] columnValues = new String[]{
        valueOrEmpty(listName.getId()),
        valueOrEmpty(listName.getName())
    };
    return ListItemFileWriter.concat(columnValues);
  }


}
