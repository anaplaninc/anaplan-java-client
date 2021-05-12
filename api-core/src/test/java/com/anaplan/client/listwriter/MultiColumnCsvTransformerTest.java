package com.anaplan.client.listwriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anaplan.client.dto.ViewMetadata;
import com.anaplan.client.dto.ViewMetadataRow;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class MultiColumnCsvTransformerTest {
  @Test
  public void toMultiColumn() {
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

  private static ViewMetadata getViewMetadata() {
    ViewMetadata viewMetadata = new ViewMetadata();
    viewMetadata.setColumns(Arrays.asList(new ViewMetadataRow("101000000114", "view-data-test-columns","abc")));
    viewMetadata.setRows(Arrays.asList(new ViewMetadataRow("20000000012", "Line Items","abc")));
    viewMetadata.setPages(Arrays.asList(new ViewMetadataRow("101000000113", "view-data-test-dimension","abcs")));
    return viewMetadata;
  }

}
