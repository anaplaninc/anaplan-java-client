package com.anaplan.client.integrationTests.helpers.dto;

/**
 * dto for dataprovider yaml files.
 */
public class Script {

  private String scriptName;
  private Report report;
  private ExportFile exportFile;
  private FailInfo failInfo;

  public String getScriptName() {
    return this.scriptName;
  }

  public void setScriptName(String scriptName) {
    this.scriptName = scriptName;
  }

  public Report getReport() {
    return this.report;
  }

  public void setReport(Report report) {
    this.report = report;
  }

  public ExportFile getExportFile() {
    return exportFile;
  }

  public void setExportFile(ExportFile exportFile) {
    this.exportFile = exportFile;
  }

  public FailInfo getFailInfo() {
    return failInfo;
  }

  public void setFailInfo(FailInfo failInfo) {
    this.failInfo = failInfo;
  }

  @Override
  public String toString() {
    return "Script{" +
        "scriptName='" + scriptName + '\'' +
        ", report=" + report +
        ", exportFile=" + exportFile +
        ", failInfo=" + failInfo +
        '}';
  }

}
