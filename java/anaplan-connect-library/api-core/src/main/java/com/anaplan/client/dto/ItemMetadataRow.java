package com.anaplan.client.dto;

import java.io.Serializable;

public class ItemMetadataRow implements Serializable {

  private String id;
  private String name;
  private String code;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
