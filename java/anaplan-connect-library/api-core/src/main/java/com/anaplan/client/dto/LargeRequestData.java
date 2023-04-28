package com.anaplan.client.dto;

import com.anaplan.client.dto.responses.BaseResponse;
import java.io.Serializable;

/**
 * List request response
 */
public class LargeRequestData extends BaseResponse implements Serializable {

  private ListReadRequest listReadRequest;

  public ListReadRequest getListReadRequest() {
    return listReadRequest;
  }

  public void setListReadRequest(ListReadRequest listReadRequest) {
    this.listReadRequest = listReadRequest;
  }
}
