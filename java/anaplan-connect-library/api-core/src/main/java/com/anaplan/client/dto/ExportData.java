package com.anaplan.client.dto;

import java.io.Serializable;

public class ExportData extends NamedObjectData implements Serializable {

  private String exportType;

  public ExportData(){}

  public ExportData(String exportType) {
    this.exportType = exportType;
  }

  public String getExportType() {
    return exportType;
  }

  public void setExportType(String exportType) {
    this.exportType = exportType;
  }
}
