package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ListItem;
import java.util.List;

/**
 * List items response
 */
public class ListItemsResponse extends ListResponse<ListItem> {

  private List<ListItem> listItems;

  @Override
  public List<ListItem> getItem() {
    return listItems;
  }

  @Override
  public void setItem(List<ListItem> item) {
    this.listItems = item;
  }
}
