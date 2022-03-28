package com.anaplan.client.dto;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "subsets")
public class ListSubset {

  private String id;
  private String name;

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
}
