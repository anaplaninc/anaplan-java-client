package com.anaplan.client.integrationTests.helpers.dto;

/**
 * dto for dataprovider yaml files.
 */
public class FailInfo {

  private int exitCode;
  private String dumpFile;

  public int getExitCode() {
    return exitCode;
  }

  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  public String getDumpFile() {
    return dumpFile;
  }

  public void setDumpFile(String dumpFile) {
    this.dumpFile = dumpFile;
  }

  @Override
  public String toString() {
    return "FailInfo{" +
        "exitCode=" + exitCode +
        ", dumpFile='" + dumpFile + '\'' +
        '}';
  }


}


