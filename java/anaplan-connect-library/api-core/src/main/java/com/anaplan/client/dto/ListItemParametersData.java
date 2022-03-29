package com.anaplan.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ListItemParametersData {

  @JsonProperty("items")
  public List<ListItem> items;

  public List<ListItem> getItems() {
    return items;
  }

  public void setItems(List<ListItem> itemDataList) {
    this.items = itemDataList;
  }
}
