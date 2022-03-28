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
    result.append(getHeader(viewMetadata));
      for (ViewMetadataValues metadataValues : allMetadataValues) {
        for (String rows: metadataValues.getLineValues()) {
          result.append(join(metadataValues.getPageValues() != null ? metadataValues.getPageValues() : "", rows)).append("\n");
        }
      }
    return result.toString();
  }


  /**
   * Get header data
   * @param viewMetadata
   * @return
   */
  private static String getHeader(ViewMetadata viewMetadata) {
    return Stream.of(
            viewMetadata.getPages() != null ? viewMetadata.getPages().stream().map(ViewMetadataRow::getName) : null,
            viewMetadata.getRows().stream().map(ViewMetadataRow::getName),
            viewMetadata.getColumns().stream().map(ViewMetadataRow::getName),
            Stream.of("Value\n")
    ).flatMap(Function.identity())
            .collect(Collectors.joining(","));
  }

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
        startInd++;
      }
      columnValues = Utils.getColumnValues(lines,startInd);
      int lineSize = Utils.splitValues(lines[columnValues.size()+1]).size();
      for (int i = startInd+columnValues.size(); i < lines.length; i++) {
        lineValues(lines[i],lineSize);
      }
    }

    /**
     * Converting CSV lines to Anaplan Single column format
     * @param line Source line
     */
    private void lineValues(String line, int lineSize) {
      int columnValSize = Utils.splitValues(columnValues.get(0)).size();
      int noOfRowDimensions = lineSize -  columnValSize;
      List<String> currenLine = Utils.splitValues(line);
      List<String> rowdata = new ArrayList<>(currenLine.subList(0,noOfRowDimensions));
      List<String> columndata = new ArrayList<>(currenLine.subList(noOfRowDimensions,lineSize));
      for (int j =0; j<columnValSize;j++){
        int columnNameIndex = rowdata.size();
        for(int k=0;k<this.columnValues.size();k++){
          rowdata.add(columnNameIndex+k,Utils.splitValues(columnValues.get(k)).get(j));
        }
        rowdata.add(rowdata.size(),columndata.get(j));
        lineValues.add(join(rowdata));
        for(int l=0;l<this.columnValues.size();l++){
          rowdata.remove(columnNameIndex+l);
        }
        rowdata.remove(rowdata.size()-1);
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
