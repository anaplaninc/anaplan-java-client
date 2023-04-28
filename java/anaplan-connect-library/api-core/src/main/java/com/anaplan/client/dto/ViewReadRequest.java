package com.anaplan.client.dto;

import com.anaplan.client.dto.responses.BaseLargeResponse;
import java.io.Serializable;

/**
 * View request response
 */
public class ViewReadRequest extends BaseLargeResponse implements Serializable {

  private String viewId;

  public String getViewId() {
    return viewId;
  }

  public void setViewId(String viewId) {
    this.viewId = viewId;
  }

}
