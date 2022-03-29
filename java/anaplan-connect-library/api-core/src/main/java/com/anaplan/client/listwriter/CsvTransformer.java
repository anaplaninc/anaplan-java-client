package com.anaplan.client.listwriter;

import com.anaplan.client.dto.ViewMetadata;
import com.anaplan.client.dto.ViewMetadataRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvTransformer {

  private CsvTransformer(){}

  /**
   * Transforms a grid CSV to single column result
   *
   * @param gridSources Source CSV
   * @return The transformed CSV
   */
  public static String toSingleColumn(ViewMetadata viewMetadata, String... gridSources) {
    StringBuilder result = new StringBuilder();
    String header = Stream.of(
        viewMetadata.getRows().stream().map(ViewMetadataRow::getName),
        viewMetadata.getPages().stream().map(ViewMetadataRow::getName),
        viewMetadata.getColumns().stream().map(ViewMetadataRow::getName),
        Stream.of("Value\n")
    ).flatMap(Function.identity())
        .collect(Collectors.joining(","));
    result.append(header);
    List<ViewMetadataValues> allMetadataValues = Arrays.stream(gridSources)
        .map(ViewMetadataValues::new)
        .collect(Collectors.toList());
    List<String> lineItems = allMetadataValues.stream()
        .map(ViewMetadataValues::getLineValues)
        .map(Map::keySet)
        .flatMap(Set::stream)
        .distinct()
        .collect(Collectors.toList());
    for (String lineItem : lineItems) {
      for (ViewMetadataValues metadataValues : allMetadataValues) {
        List<String> values = metadataValues.getLineValues().get(lineItem);
        if (values != null) {
          Iterator<String> valuesIterator = values.iterator();
          for (String page : metadataValues.getPageValues()) {
            for (String column : metadataValues.getColumnValues()) {
              result.append(join(lineItem, page, column, valuesIterator.next()) + "\n");
            }
          }
        }
      }
    }
    return result.toString();
  }

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
    String header = Stream.of(
        viewMetadata.getPages().stream().map(ViewMetadataRow::getName),
        viewMetadata.getRows().stream().map(ViewMetadataRow::getName),
        Stream.of(join(getColumnValues(allMetadataValues)) + "\n")
    ).flatMap(Function.identity())
        .collect(Collectors.joining(","));
    result.append(header);
    for (String page : getPageValues(allMetadataValues)) {
      for (ViewMetadataValues metadataValues : allMetadataValues) {
        if(metadataValues.getPageValues().contains(page)){
          for (Entry<String, List<String>> itemEntry : metadataValues.getLineValues().entrySet()) {
            String lineItem = itemEntry.getKey();
            result.append(join(page, lineItem, join(itemEntry.getValue())) + "\n");
          }
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
   * Get all distinct metadata page values
   *
   * @param allMetadataValues Metadata
   * @return List of values
   */
  private static List<String> getPageValues(List<ViewMetadataValues> allMetadataValues) {
    return allMetadataValues.stream()
        .map(ViewMetadataValues::getPageValues)
        .flatMap(List::stream)
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Utility to convert from grid CSV to different csv formats like multi column or single column
   * Parses a grid CSV source to in memory values
   */
  public static class ViewMetadataValues {

    private List<String> pageValues;

    private List<String> columnValues;

    private Map<String, List<String>> lineValues;

    /**
     * Grid CSV source will be parsed to in memory values
     *
     * @param gridSource Grid source
     */
    public ViewMetadataValues(String gridSource) {
      String[] lines = gridSource.split("\n");
      if (lines.length < 2) {
        throw new IllegalArgumentException("Invalid CSV grid source");
      }
      pageValues = splitValues(lines[0]);
      columnValues = splitValues(lines[1]);
      lineValues = new LinkedHashMap<>();
      for (int i = 2; i < lines.length; i++) {
        addLineValues(lines[i]);
      }
    }

    /**
     * Parse line values
     * @param line Line string
     */
    private void addLineValues(String line) {
      List<String> split = splitValues(line);
      if (!split.isEmpty()) {
        List<String> value = new ArrayList<>(split);
        value.remove(0);
        lineValues.put(split.get(0), value);
      }
    }

    /**
     * Split CSV line to non empty tokens
     * @param line Source line
     * @return A list of parsed values
     */
    private List<String> splitValues(String line) {
      return Arrays.stream(line.split(","))
          .filter(s1 -> s1 != null && !"".equals(s1))
          .collect(Collectors.toList());
    }

    public List<String> getPageValues() {
      return pageValues;
    }

    public List<String> getColumnValues() {
      return columnValues;
    }

    public Map<String, List<String>> getLineValues() {
      return lineValues;
    }
  }


}
