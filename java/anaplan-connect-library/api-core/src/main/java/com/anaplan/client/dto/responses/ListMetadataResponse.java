package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ListMetadata;

public class ListMetadataResponse extends ObjectResponse<ListMetadata> {

  private ListMetadata metadata;

  @Override
  public ListMetadata getItem() {
    return metadata;
  }

  @Override
  public void setItem(ListMetadata metadata) {
    this.metadata = metadata;
  }

}