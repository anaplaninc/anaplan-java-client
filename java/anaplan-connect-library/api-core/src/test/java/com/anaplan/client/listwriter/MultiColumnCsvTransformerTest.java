package com.anaplan.client.listwriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anaplan.client.dto.ViewMetadata;
import com.anaplan.client.dto.ViewMetadataRow;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MultiColumnCsvTransformerTest {
  @Test
  void toMultiColumn() {
    assertEquals(
        "view-data-test-dimension,Line Items,c1,c2\n"
            + "a,l1,1,2\n"
            + "a,l2,3,4\n",
        MultiColumnCsvTransformer.toMultiColumn(
            getViewMetadata(),
            "a\n"
                + ",c1,c2\n"
                + "l1,1,2\n"
                + "l2,3,4"));
  }

  @Test
  void toMultipleColumnMultiplePagesMultipleDimension(){
    assertEquals("view-data-test-row1,view-data-test-dimension,Line Items,Time,col1,col1,col1,col2,col2,col2,col3,col3,col3\n" +
            "view-data-test-row1,view-data-test-dimension,Line Items,Time,c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\"\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,1,11,0,1,11,0,1,11,0\n" +
            "\"r,1\",\"a,b\",l1,Feb 17,0,0,0,0,0,0,0,0,0\n" +
            "\"r,1\",\"a,b\",l1,Mar 17,0,0,0,0,0,0,0,0,0\n" +
            "\"r,1\",\"a,b\",l1,Q1 FY17,1,11,0,1,11,0,1,11,0\n" +
            "\"r,1\",\"a,b\",l1,Apr 17,0,0,0,0,0,0,0,0,0\n",MultiColumnCsvTransformer.toMultiColumn(
            getViewMetadataMultiplePages(),
            "\"r,1\",\"a,b\"\n" +
                    ",,col1,col1,col1,col2,col2,col2,col3,col3,col3\n" +
                    ",,c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\"\n" +
                    "l1,Jan 17,1,11,0,1,11,0,1,11,0\n" +
                    "l1,Feb 17,0,0,0,0,0,0,0,0,0\n" +
                    "l1,Mar 17,0,0,0,0,0,0,0,0,0\n" +
                    "l1,Q1 FY17,1,11,0,1,11,0,1,11,0\n" +
                    "l1,Apr 17,0,0,0,0,0,0,0,0,0"));
  }

  @Test
  void toMultipleColumnMultiplePagesMultipleDimensionWithoutPages(){
    assertEquals("Line Items,Time,col1,col1,col1,col2,col2,col2,col3,col3,col3\n" +
            "Line Items,Time,c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\"\n" +
            "l1,Jan 17,1,11,0,1,11,0,1,11,0\n" +
            "l1,Feb 17,0,0,0,0,0,0,0,0,0\n" +
            "l1,Mar 17,0,0,0,0,0,0,0,0,0\n" +
            "l1,Q1 FY17,1,11,0,1,11,0,1,11,0\n" +
            "l1,Apr 17,0,0,0,0,0,0,0,0,0\n",MultiColumnCsvTransformer.toMultiColumn(
            getViewMetadataWithNoPage(),
                    ",,col1,col1,col1,col2,col2,col2,col3,col3,col3\n" +
                    ",,c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\"\n" +
                    "l1,Jan 17,1,11,0,1,11,0,1,11,0\n" +
                    "l1,Feb 17,0,0,0,0,0,0,0,0,0\n" +
                    "l1,Mar 17,0,0,0,0,0,0,0,0,0\n" +
                    "l1,Q1 FY17,1,11,0,1,11,0,1,11,0\n" +
                    "l1,Apr 17,0,0,0,0,0,0,0,0,0"));
  }

  private static ViewMetadata getViewMetadata() {
    ViewMetadata viewMetadata = new ViewMetadata();
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000114", "view-data-test-columns")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items")));
    viewMetadata.setPages(Arrays.asList(new ViewMetadataRow("101000000113", "view-data-test-dimension")));
    return viewMetadata;
  }

  private static ViewMetadata getViewMetadataMultiplePages() {
    ViewMetadata viewMetadata = new ViewMetadata();
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000116", "view-data-test-column-2"),new ViewMetadataRow("101000000114", "view-data-test-columns")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items"),new ViewMetadataRow("20000000003", "Time")));
    viewMetadata.setPages(Arrays.asList(new ViewMetadataRow("101000000117", "view-data-test-row1"),new ViewMetadataRow("101000000113", "view-data-test-dimension")));
    return viewMetadata;
  }

  private static ViewMetadata getViewMetadataWithNoPage() {
    ViewMetadata viewMetadata = new ViewMetadata();
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000116", "view-data-test-column-2"),new ViewMetadataRow("101000000114", "view-data-test-columns")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items"),new ViewMetadataRow("20000000003", "Time")));
    return viewMetadata;
  }

}
