package com.anaplan.client.listwriter;

import static com.anaplan.client.listwriter.ListItemFileWriter.streamOfEmptyValues;
import static com.anaplan.client.listwriter.ListItemFileWriter.valueOrEmpty;

import com.anaplan.client.dto.ListMetadata;
import com.anaplan.client.dto.ListMetadataProperty;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility for exporting list metadata to csv
 */
public class ListMetadataCsvWriter {

  public static String[] FIXED_COLUMN_NAMES_LIST_METADATA = new String[]{
      "id", "name", "hasSelectiveAccess", "productionData", "managedBy", "numberedList",
      "useTopLevelAsPageDefault", "itemCount", "workflowEnabled", "permittedItems",
      "usedInAppliesTo"};

  /**
   * Get csv lines
   *
   * @param listMetadata ListMetadata
   * @return Result as string
   */
  public static Stream<String> getLines(ListMetadata listMetadata) {
    if (listMetadata == null) {
      return Stream.empty();
    }
    return Stream.concat(
        getColumnNamesConcat(listMetadata),
        getColumnValuesConcat(listMetadata)
    );
  }

  /**
   * Get column names concatenated string
   *
   * @param listMetadata ListMetadata
   * @return Result as string
   */
  public static Stream<String> getColumnNamesConcat(ListMetadata listMetadata) {
    Stream<String> columnNamesListMetadata = getColumnNames(listMetadata);
    List<String> columnNames = columnNamesListMetadata.collect(Collectors.toList());
    String columnNamesCsv = ListItemFileWriter
        .concat(columnNames.toArray(new String[columnNames.size()]));
    return Stream.of(columnNamesCsv);
  }

  /**
   * Get column values concatenated string
   *
   * @param listMetadata ListMetadata
   * @return Result as string
   */
  public static Stream<String> getColumnValuesConcat(ListMetadata listMetadata) {
    List<String> lineValues = getColumnValues(listMetadata)
        .collect(Collectors.toList());
    String concat = ListItemFileWriter.concat(lineValues.toArray(new String[lineValues.size()]));
    return Stream.of(concat);
  }

  /**
   * Get column names for an element
   *
   * @param listMetadata The list metadata
   * @return Stream of names
   */
  public static Stream<String> getColumnNames(ListMetadata listMetadata) {
    if (listMetadata == null) {
      return Stream.empty();
    }
    return Stream.concat(
        Arrays.stream(FIXED_COLUMN_NAMES_LIST_METADATA),
        Stream.concat(
            ListMetadataPropertyCsvWriter.getColumnNames(listMetadata.getProperties()),
            ListMetadataSubsetCsvWriter.getColumnNamesListSubsets(listMetadata.getSubsets())
        )
    );
  }

  /**
   * Get column values for an element
   *
   * @param listMetadata The list metadata
   * @return Stream of values
   */
  public static Stream<String> getColumnValues(ListMetadata listMetadata) {
    if (listMetadata == null) {
      return Stream.concat(
          Stream.concat(
              streamOfEmptyValues(11),
              ListMetadataPropertyCsvWriter.getColumnValues((ListMetadataProperty) null)
          ),
          ListMetadataSubsetCsvWriter.getColumnValues(null)
      );
    }
    return Stream.concat(
        Stream.of(
            valueOrEmpty(listMetadata.getId()),
            valueOrEmpty(listMetadata.getName()),
            valueOrEmpty(listMetadata.getHasSelectiveAccess()),
            valueOrEmpty(listMetadata.getProductionData()),
            valueOrEmpty(listMetadata.getManagedBy()),
            valueOrEmpty(listMetadata.getNumberedList()),
            valueOrEmpty(listMetadata.getUseTopLevelAsPageDefault()),
            valueOrEmpty(listMetadata.getItemCount()),
            valueOrEmpty(listMetadata.getWorkflowEnabled()),
            valueOrEmpty(listMetadata.getPermittedItems()),
            valueOrEmpty(listMetadata.getUsedInAppliesTo())
        ),
        Stream.concat(
            ListMetadataPropertyCsvWriter.getColumnValues(listMetadata.getProperties()),
            ListMetadataSubsetCsvWriter.getColumnValues(listMetadata.getSubsets())
        )
    );
  }

}
