package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ItemMetadataRow;
import java.util.List;

public class ItemMetadataResponse extends ListResponse<ItemMetadataRow> {

  private List<ItemMetadataRow> items;

  @Override
  public List<ItemMetadataRow> getItem() {
    return items;
  }

  @Override
  public void setItem(List<ItemMetadataRow> item) {
    this.items = item;
  }

}
