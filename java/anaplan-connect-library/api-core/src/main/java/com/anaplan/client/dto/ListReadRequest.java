package com.anaplan.client.dto;

import com.anaplan.client.dto.responses.BaseLargeResponse;
import java.io.Serializable;

/**
 * List request response
 */
public class ListReadRequest extends BaseLargeResponse implements Serializable {

  private String listId;

  public String getListId() {
    return listId;
  }

  public void setListId(String viewId) {
    this.listId = viewId;
  }
}
