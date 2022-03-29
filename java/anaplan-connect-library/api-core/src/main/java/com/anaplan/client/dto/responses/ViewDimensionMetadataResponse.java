package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ViewDimensionMetadata;
import com.anaplan.client.dto.ViewMetadataRow;
import java.util.List;

public class ViewDimensionMetadataResponse extends BaseResponse {

  private List<ViewMetadataRow> items;

  public ViewDimensionMetadata getViewDimensionMetadata() {
    return new ViewDimensionMetadata(items);
  }

  public List<ViewMetadataRow> getItems() {
    return items;
  }

  public void setItems(List<ViewMetadataRow> items) {
    this.items = items;
  }
}
