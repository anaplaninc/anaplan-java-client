package com.anaplan.client.listwriter;

import static com.anaplan.client.listwriter.ListItemFileWriter.streamOfEmptyValues;
import static com.anaplan.client.listwriter.ListItemFileWriter.valueOrEmpty;

import com.anaplan.client.dto.ListSubset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * CSV export utility for list subset
 */
public class ListMetadataSubsetCsvWriter {

  private ListMetadataSubsetCsvWriter(){}

  protected static final String[] FIXED_COLUMN_NAMES_LIST_SUBSET = {"id", "name"};

  /**
   * Get column names for a list of elements
   *
   * @param listSubsets The subsets list
   * @return Stream of names
   */
  public static Stream<String> getColumnNamesListSubsets(List<ListSubset> listSubsets) {
    if (listSubsets == null) {
      return Stream.empty();
    }
    return listSubsets.stream()
        .flatMap(p -> Arrays.stream(FIXED_COLUMN_NAMES_LIST_SUBSET));
  }

  /**
   * Get column values for a list of elements
   *
   * @param listSubsets The subsets list
   * @return Stream of values
   */
  public static Stream<String> getColumnValues(List<ListSubset> listSubsets) {
    if (listSubsets == null || listSubsets.isEmpty()) {
      return Stream.empty();
    }
    return listSubsets.stream()
        .flatMap(ListMetadataSubsetCsvWriter::getColumnValuesListSubset);
  }

  /**
   * Get column values for an element
   *
   * @param listSubset The subset
   * @return Stream of values
   */
  public static Stream<String> getColumnValuesListSubset(ListSubset listSubset) {
    if (listSubset == null) {
      return streamOfEmptyValues(2);
    }
    return Stream.of(
        valueOrEmpty(listSubset.getId()),
        valueOrEmpty(listSubset.getName())
    );
  }

}
