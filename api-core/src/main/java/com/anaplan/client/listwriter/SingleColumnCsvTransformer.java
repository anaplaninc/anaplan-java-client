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

public class SingleColumnCsvTransformer {

  /**
   * Transforms a grid CSV to single column result
   *
   * @param gridSources Source CSV
   * @return The transformed CSV
   */
  public static String toSingleColumn(ViewMetadata viewMetadata, String... gridSources) {
    StringBuilder result = new StringBuilder();
    List<ViewMetadataValues> allMetadataValues = Arrays.stream(gridSources)
        .map(ViewMetadataValues::new)
        .collect(Collectors.toList());
    String header = null;
    if(viewMetadata.getPages()!=null){
      header = Stream.of(
          viewMetadata.getPages().stream().map(ViewMetadataRow::getName),
          viewMetadata.getRows().stream().map(ViewMetadataRow::getName),
          viewMetadata.getColumns().stream().map(ViewMetadataRow::getName),
          Stream.of("Value\n")
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
          viewMetadata.getColumns().stream().map(ViewMetadataRow::getName),
          Stream.of("Value\n")
      ).flatMap(Function.identity())
          .collect(Collectors.joining(","));
      result.append(header);
      for (ViewMetadataValues metadataValues : allMetadataValues) {
        for (String rows: metadataValues.getLineValues()) {
          result.append(join(rows) + "\n");
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
      if (lines[0].startsWith(",")) {
        columnValues = Utils.splitValues(lines[0]);
        int lineSize = Utils.splitValues(lines[1]).size();
        for (int i = 1; i < lines.length; i++) {
          lineValues(lines[i],lineSize);
        }
      } else {
        pageValues = lines[0];
        columnValues = Utils.splitValues(lines[1]);
        int lineSize = Utils.splitValues(lines[2]).size();
        for (int i = 2; i < lines.length; i++) {
          lineValues(lines[i],lineSize);
          }
        }
      }

    /**
     * Converting CSV lines to Anaplan Single column format
     * @param line Source line
     */
    private void lineValues(String line, int lineSize) {
      int noOfRowDimensions = lineSize - columnValues.size();
      List<String> currenLine = Utils.splitValues(line);
      List <String> rowdata = new ArrayList(currenLine.subList(0,noOfRowDimensions));
      List <String> columndata = new ArrayList(currenLine.subList(noOfRowDimensions,lineSize));
      for (int j =0; j<columnValues.size();j++){
        int columnNameIndex = rowdata.size();
        int columnDataIndex = rowdata.size()+1;
        rowdata.add(columnNameIndex,columnValues.get(j));
        rowdata.add(columnDataIndex,columndata.get(j));
        lineValues.add(join(rowdata));
        rowdata.remove(columnDataIndex);
        rowdata.remove(columnNameIndex);
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
