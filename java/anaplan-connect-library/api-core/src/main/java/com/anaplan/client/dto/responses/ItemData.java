package com.anaplan.client.dto.responses;


import java.util.List;

public class ItemData {

  private List<String> names;
  private List<String> codes;

  public List<String> getNames() {
    return names;
  }

  public void setNames(List<String> names) {
    this.names = names;
  }

  public List<String> getCodes() {
    return codes;
  }

  public void setCodes(List<String> codes) {
    this.codes = codes;
  }
}
