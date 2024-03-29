package com.anaplan.client.dto;

import java.io.Serializable;

/**
 * Created by Spondon Saha User: spondonsaha Date: 6/21/17 Time: 3:28 PM
 */
public class ImportData extends NamedObjectData implements Serializable {

  private String importType;
  private String importDataSourceId;

  public String getImportType() {
    return importType;
  }

  public String getImportDataSourceId() {
    return importDataSourceId;
  }

  public void setImportType(String importType) {
    this.importType = importType;
  }

  public void setImportDataSourceId(String importDataSourceId) {
    this.importDataSourceId = importDataSourceId;
  }
}
