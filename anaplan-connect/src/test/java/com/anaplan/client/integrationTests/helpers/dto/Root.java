package com.anaplan.client.integrationTests.helpers.dto;
/**
 * dto for dataprovider yaml files.
 */

import java.io.Serializable;
import java.util.List;

public class Root implements Serializable {


  private List<AnaplanTest> anaplanTests;

  public Root() {
  }

  public List<AnaplanTest> getTests() {
    return anaplanTests;
  }

  public void setTests(List<AnaplanTest> anaplanTests) {
    this.anaplanTests = anaplanTests;
  }

  @Override
  public String toString() {
    return "Root{" +
        "tests=" + anaplanTests +
        '}';
  }
}
