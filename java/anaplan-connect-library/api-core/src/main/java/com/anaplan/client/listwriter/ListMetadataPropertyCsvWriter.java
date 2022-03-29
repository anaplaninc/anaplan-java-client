package com.anaplan.client.listwriter;

import static com.anaplan.client.listwriter.ListItemFileWriter.streamOfEmptyValues;
import static com.anaplan.client.listwriter.ListItemFileWriter.valueOrEmpty;

import com.anaplan.client.dto.ListMetadataProperty;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * CSV export utility for list metadata property
 */
public class ListMetadataPropertyCsvWriter {

  private ListMetadataPropertyCsvWriter(){}

  protected static final String[] FIXED_COLUMN_NAMES_LIST_METADATA_PROPERTY = new String[]{
      "name", "format", "formula", "referencedBy", "id"};

  /**
   * Get column names for a list of elements
   *
   * @param listMetadataProperties The elements list
   * @return Stream of names
   */
  public static Stream<String> getColumnNames(List<ListMetadataProperty> listMetadataProperties) {
    if (listMetadataProperties == null) {
      return Stream.empty();
    }
    return listMetadataProperties.stream()
        .flatMap(p -> Stream.concat(
            Arrays.stream(FIXED_COLUMN_NAMES_LIST_METADATA_PROPERTY),
            p.getFormatMetadata()
                .keySet().
                stream().
                map(ListItemFileWriter::valueOrEmpty)
        ));
  }

  /**
   * Get column values for a list of elements
   *
   * @param listMetadataProperties The elements list
   * @return Stream of values
   */
  public static Stream<String> getColumnValues(List<ListMetadataProperty> listMetadataProperties) {
    if (listMetadataProperties == null || listMetadataProperties.isEmpty()) {
      return Stream.empty();
    }
    return listMetadataProperties.stream()
        .flatMap(ListMetadataPropertyCsvWriter::getColumnValues);
  }

  /**
   * Get column values for an element
   *
   * @param listMetadataProperty The list metadata property
   * @return Stream of values
   */
  public static Stream<String> getColumnValues(ListMetadataProperty listMetadataProperty) {
    if (listMetadataProperty == null) {
      return streamOfEmptyValues(5);
    }
    return Stream.concat(
        Stream.of(
            valueOrEmpty(listMetadataProperty.getName()),
            valueOrEmpty(listMetadataProperty.getFormat()),
            valueOrEmpty(listMetadataProperty.getFormula()),
            valueOrEmpty(listMetadataProperty.getReferencedBy()),
            valueOrEmpty(listMetadataProperty.getId())
        ),
        listMetadataProperty.getFormatMetadata()
            .values().
            stream().
            map(ListItemFileWriter::valueOrEmpty)
    );
  }

}
