package com.anaplan.client.listwriter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anaplan.client.dto.ViewMetadata;
import com.anaplan.client.dto.ViewMetadataRow;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class CsvTransformerTest {

  @Test
  void toSingleColumn() {
    String expected = "Line Items,view-data-test-dimension,view-data-test-columns,Value\n"
        + "l1,a,c1,1\n"
        + "l1,a,c2,2\n"
        + "l1,b,c1,5\n"
        + "l1,b,c2,6\n"
        + "l2,a,c1,3\n"
        + "l2,a,c2,4\n"
        + "l2,b,c1,7\n"
        + "l2,b,c2,8\n";
    String dexpecte = CsvTransformer.toSingleColumn(
        getViewMetadata(),
        "a\n"
            + ",c1,c2\n"
            + "l1,1,2\n"
            + "l2,3,4",
        "b\n"
            + ",c1,c2\n"
            + "l1,5,6\n"
            + "l2,7,8");
    assertThat(expected, is(dexpecte));
  }

  @Test
  void toMultiColumn() {
    assertEquals(
        "view-data-test-dimension,Line Items,c1,c2\n"
            + "a,l1,1,2\n"
            + "a,l2,3,4\n"
            + "b,l1,5,6\n"
            + "b,l2,7,8\n",
        CsvTransformer.toMultiColumn(
            getViewMetadata(),
            "a\n"
                + ",c1,c2\n"
                + "l1,1,2\n"
                + "l2,3,4",
            "b\n"
                + ",c1,c2\n"
                + "l1,5,6\n"
                + "l2,7,8"));
  }

  private static ViewMetadata getViewMetadata() {
    ViewMetadata viewMetadata = new ViewMetadata();
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000114", "view-data-test-columns")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items")));
    viewMetadata.setPages(Arrays.asList(new ViewMetadataRow("101000000113", "view-data-test-dimension")));
    return viewMetadata;
  }

}
