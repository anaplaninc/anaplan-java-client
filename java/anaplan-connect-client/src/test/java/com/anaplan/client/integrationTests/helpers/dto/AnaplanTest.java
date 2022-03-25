package com.anaplan.client.integrationTests.helpers.dto;
/**
 * dto for dataprovider yaml files.
 */

import java.util.List;

public class AnaplanTest {

  private String testName;

  private List<Script> scripts;

  public String getTestName() {
    return this.testName;
  }

  public void setTestName(String TestName) {
    this.testName = TestName;
  }

  public List<Script> getScripts() {
    return this.scripts;
  }

  public void setScripts(List<Script> scripts) {
    this.scripts = scripts;
  }

  @Override
  public String toString() {
    return this.testName;
  }
}
