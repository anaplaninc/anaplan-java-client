package com.anaplan.client.dto;

import java.io.Serializable;

/**
 * Created by Spondon Saha User: spondonsaha Date: 6/21/17 Time: 7:19 PM
 */
public class Meta implements Serializable {

  private String schema;
  private Paging paging;

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public Paging getPaging() {
    return paging;
  }

  public void setPaging(Paging paging) {
    this.paging = paging;
  }
}
