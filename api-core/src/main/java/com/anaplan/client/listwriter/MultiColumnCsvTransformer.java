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

  /**
   * CSV join an array of strings
   * @param values Values to join
   * @return Result
   */
  public static String join(String... values) {
    return Arrays.stream(values)
        .collect(Collectors.joining(","));
  }

  /**
   * CSV join a list of strings
   * @param values Values to join
   * @return Result
   */
  public static String join(List<String> values) {
    return values.stream()
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
    String header = null;
    if(viewMetadata.getPages()!=null){
      header = Stream.of(
          viewMetadata.getPages().stream().map(ViewMetadataRow::getName),
          viewMetadata.getRows().stream().map(ViewMetadataRow::getName),
          Stream.of(join(getColumnValues(allMetadataValues)) + "\n")
      ).flatMap(Function.identity())
          .collect(Collectors.joining(","));
      result.append(header);
      for (ViewMetadataValues metadataValues : allMetadataValues) {
        for (String rows: metadataValues.getLineValues()) {
          result.append(join(metadataValues.getPageValues(),rows) + "\n");
        }
      }
    }else{
      header = Stream.of(
          viewMetadata.getRows().stream().map(ViewMetadataRow::getName),
          Stream.of(join(getColumnValues(allMetadataValues)) + "\n")
      ).flatMap(Function.identity())
          .collect(Collectors.joining(","));
      result.append(header);
      for (ViewMetadataValues metadataValues : allMetadataValues) {
        for (String row: metadataValues.getLineValues()) {
          result.append(join(row) + "\n");
        }
      }
    }
    return result.toString();
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

    private List<String> columnValues;

    private List<String> lineValues = new ArrayList<String>();

    /**
     * Grid CSV source will be parsed to in memory values
     *
     * @param gridSource Grid source
     */
    public ViewMetadataValues(String gridSource) {
      String[] lines = gridSource.split("\n");
      if(lines[0].startsWith(",")) {
        columnValues = Utils.splitValues(lines[0]);
        for (int i = 1; i < lines.length; i++) {
          lineValues.add(lines[i]);
        }
      }else {
        pageValues = lines[0];
        columnValues = Utils.splitValues(lines[1]);
        for (int i = 2; i < lines.length; i++) {
          lineValues.add(lines[i]);
        }
      }
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
