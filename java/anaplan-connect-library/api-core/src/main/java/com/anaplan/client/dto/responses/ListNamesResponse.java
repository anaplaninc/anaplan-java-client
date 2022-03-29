package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ListName;
import java.util.List;

public class ListNamesResponse extends ListResponse<ListName> {

  private List<ListName> lists;

  @Override
  public List<ListName> getItem() {
    return lists;
  }

  @Override
  public void setItem(List<ListName> item) {
    this.lists = item;
  }
}