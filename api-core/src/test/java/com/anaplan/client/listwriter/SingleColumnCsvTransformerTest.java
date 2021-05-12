package com.anaplan.client.listwriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anaplan.client.dto.ViewMetadata;
import com.anaplan.client.dto.ViewMetadataRow;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class SingleColumnCsvTransformerTest {



  @Test
  public void toSingleColumnOnePage() {
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
  public void toSingleColumnTwoPage() {
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
  public void toSingleColumnNoPage() {
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
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000114", "view-data-test-columns","abc")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items","abc")));
    viewMetadata.setPages(Arrays.asList(new ViewMetadataRow("101000000113", "view-data-test-dimension","abcs")));
    return viewMetadata;
  }

  private static ViewMetadata getViewMetadataNoPage() {
    ViewMetadata viewMetadata = new ViewMetadata();
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000114", "view-data-test-columns","abc")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items","abc")));
    return viewMetadata;
  }

}
