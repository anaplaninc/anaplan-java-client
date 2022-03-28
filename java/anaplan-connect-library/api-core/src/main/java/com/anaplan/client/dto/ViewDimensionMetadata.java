package com.anaplan.client.dto;

import java.util.List;

public class ViewDimensionMetadata {

  private List<ViewMetadataRow> items;

  public ViewDimensionMetadata() {
    //
  }

  public ViewDimensionMetadata(List<ViewMetadataRow> items) {
    this.items = items;
  }

  public List<ViewMetadataRow> getItems() {
    return items;
  }

  public void setItems(List<ViewMetadataRow> items) {
    this.items = items;
  }
}
