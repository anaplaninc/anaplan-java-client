package com.anaplan.client.listwriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anaplan.client.dto.ViewMetadata;
import com.anaplan.client.dto.ViewMetadataRow;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SingleColumnCsvTransformerTest {



  @Test
  void toSingleColumnOnePage() throws IOException {
    assertEquals(
        "view-data-test-dimension,Line Items,view-data-test-columns,Value\n"
            + "a,l1,c1,1\n"
            + "a,l1,c2,2\n"
            + "a,l2,c1,3\n"
            + "a,l2,c2,4\n",
        SingleColumnCsvTransformer.toSingleColumn(
            getViewMetadataOnePage(),
            "a\n"
                + ",c1,c2\n"
                + "l1,1,2\n"
                + "l2,3,4"));
  }

  @Test
  void toSingleColumnMultiplePagesMultipleDimension() throws IOException {
    assertEquals("view-data-test-row1,view-data-test-dimension,Line Items,Time,view-data-test-column-2,view-data-test-columns,Value\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,col1,c1,1\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,col1,\"c,2,\",11\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,col1,\"co,lu: mn 3\",0\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,col2,c1,1\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,col2,\"c,2,\",11\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,col2,\"co,lu: mn 3\",0\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,col3,c1,1\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,col3,\"c,2,\",11\n" +
            "\"r,1\",\"a,b\",l1,Jan 17,col3,\"co,lu: mn 3\",0\n",SingleColumnCsvTransformer.toSingleColumn(
            getViewMetadataMultiplePages(),
            "\"r,1\",\"a,b\"\n" +
                    ",,col1,col1,col1,col2,col2,col2,col3,col3,col3\n" +
                    ",,c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\",c1,\"c,2,\",\"co,lu: mn 3\"\n" +
                    "l1,Jan 17,1,11,0,1,11,0,1,11,0\n"));
  }

  @Test
  void toSingleColumnTwoPage() throws IOException {
    assertEquals(
        "view-data-test-dimension,Line Items,view-data-test-columns,Value\n"
            + "r\\3,\"c,2,\",Jan 17,\"l, 1\",\"a,b\",0\n"
            + "r\\3,\"c,2,\",Jan 17,\"l, 1\",b \\,0\n"
            + "r\\3,\"c,2,\",Jan 17,l2,\"a,b\",0\n"
            + "r\\3,\"c,2,\",Jan 17,l2,b \\,0\n",
        SingleColumnCsvTransformer.toSingleColumn(
            getViewMetadataOnePage(),
            "r\\3,\"c,2,\"\n"
                + ",,\"a,b\",b \\\n"
                + "Jan 17,\"l, 1\",0,0\n"
                + "Jan 17,l2,0,0\n"));
  }

  @Test
  void toSingleColumnNoPage() throws IOException {
    assertEquals(
        "Line Items,view-data-test-columns,Value\n"
            + "l1,c1,1\n"
            + "l1,c2,2\n"
            + "l2,c1,3\n"
            + "l2,c2,4\n",
        SingleColumnCsvTransformer.toSingleColumn(
            getViewMetadataNoPage(), ",c1,c2\n"
                + "l1,1,2\n"
                + "l2,3,4"));
  }


  private static ViewMetadata getViewMetadataOnePage() {
    ViewMetadata viewMetadata = new ViewMetadata();
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000114", "view-data-test-columns")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items")));
    viewMetadata.setPages(Arrays.asList(new ViewMetadataRow("101000000113", "view-data-test-dimension")));
    return viewMetadata;
  }

  private static ViewMetadata getViewMetadataNoPage() {
    ViewMetadata viewMetadata = new ViewMetadata();
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000114", "view-data-test-columns")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items")));
    return viewMetadata;
  }

  private static ViewMetadata getViewMetadataMultiplePages() {
    ViewMetadata viewMetadata = new ViewMetadata();
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000116", "view-data-test-column-2"),new ViewMetadataRow("101000000114", "view-data-test-columns")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items"),new ViewMetadataRow("20000000003", "Time")));
    viewMetadata.setPages(Arrays.asList(new ViewMetadataRow("101000000117", "view-data-test-row1"),new ViewMetadataRow("101000000113", "view-data-test-dimension")));
    return viewMetadata;
  }

}
