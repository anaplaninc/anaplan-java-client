package com.anaplan.client.dto;

import com.anaplan.client.dto.responses.BaseResponse;
import java.io.Serializable;

/**
 * View request response
 */
public class ViewRequestData extends BaseResponse implements Serializable {

  private ViewReadRequest viewReadRequest;

  public ViewReadRequest getViewReadRequest() {
    return viewReadRequest;
  }

  public void setViewReadRequest(ViewReadRequest viewReadRequest) {
    this.viewReadRequest = viewReadRequest;
  }
}
