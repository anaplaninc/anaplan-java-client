package com.anaplan.client.listwriter;

import com.anaplan.client.Utils;
import com.anaplan.client.dto.ViewMetadata;
import com.anaplan.client.dto.ViewMetadataRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiColumnCsvTransformer {

  private MultiColumnCsvTransformer(){}

  /**
   * CSV join an array of strings
   * @param values Values to join
   * @return Result
   */
  public static String join(String... values) {
    return Arrays.stream(values).filter(value -> !value.isEmpty())
            .collect(Collectors.joining(","));
  }

  /**
   * CSV join a list of strings
   * @param values Values to join
   * @return Result
   */
  public static String join(List<String> values) {
    return values.stream().filter(value -> !value.isEmpty())
            .collect(Collectors.joining(","));
  }


  /**
   * Transforms a grid CSV to single column result
   *
   * @param gridSources Source CSV
   * @return The transformed CSV
   */
  public static String toMultiColumn(ViewMetadata viewMetadata, String... gridSources) {
    if (gridSources.length == 0) {
      throw new IllegalArgumentException("Missing grid sources");
    }
    List<ViewMetadataValues> allMetadataValues = Arrays.stream(gridSources)
        .map(ViewMetadataValues::new)
        .collect(Collectors.toList());
    StringBuilder result = new StringBuilder();
    result.append(getHeader(viewMetadata, allMetadataValues));
    result.append(System.lineSeparator());
      for (ViewMetadataValues metadataValues : allMetadataValues) {
        for (String rows: metadataValues.getLineValues()) {
          result.append(join(metadataValues.getPageValues() != null ? metadataValues.getPageValues() : "", rows)).append("\n");
        }
      }
    return result.toString();
  }

  /**
   * Get header data
   * @param viewMetadata  {@link ViewMetadata} the view metadata
   * @param allMetadataValues {@link List<ViewMetadataValues>} the metadata values
   * @return csv header
   */
  private static String getHeader(ViewMetadata viewMetadata, List<ViewMetadataValues> allMetadataValues) {
    List<String> columnHeaders = getColumnValues(allMetadataValues);
    return columnHeaders.stream().map(header -> Stream.of(
            viewMetadata.getPages() != null ? viewMetadata.getPages().stream().map(ViewMetadataRow::getName) : null,
            viewMetadata.getRows().stream().map(ViewMetadataRow::getName),
            Stream.of(join(header))).flatMap(Function.identity())
            .collect(Collectors.joining(",")))
            .collect(Collectors.joining(System.lineSeparator()));
  }

  /**
   * Get all distinct metadata column values
   *
   * @param allMetadataValues Metadata
   * @return List of values
   */
  private static List<String> getColumnValues(List<ViewMetadataValues> allMetadataValues) {
    return allMetadataValues.stream()
        .map(ViewMetadataValues::getColumnValues)
        .flatMap(List::stream)
        .distinct()
        .collect(Collectors.toList());
  }


  /**
   * Utility to convert from grid CSV to different csv formats like multi column or single column
   * Parses a grid CSV source to in memory values
   */
  public static class ViewMetadataValues {

    private String pageValues;

    private final List<String> columnValues;

    private final List<String> lineValues = new ArrayList<>();

    /**
     * Grid CSV source will be parsed to in memory values
     *
     * @param gridSource Grid source
     */
    public ViewMetadataValues(String gridSource) {
      String[] lines = gridSource.split("\n");
      int startInd=0;
      if(!lines[0].startsWith(",")) {
        pageValues = lines[0];
        ++startInd;
      }
      columnValues = Utils.getColumnValues(lines, startInd);
      lineValues.addAll(Arrays.asList(lines).subList(startInd + columnValues.size(), lines.length));
    }


    public String getPageValues() {
      return pageValues;
    }

    public List<String> getColumnValues() {
      return columnValues;
    }

    public List<String> getLineValues() {
      return lineValues;
    }
  }


}
