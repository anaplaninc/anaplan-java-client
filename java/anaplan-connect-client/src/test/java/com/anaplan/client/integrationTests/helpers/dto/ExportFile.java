package com.anaplan.client.integrationTests.helpers.dto;

import java.util.Objects;

public class ExportFile {

  private String exportedFile;
  private String orgFile;
  private Long fileSize;

  public String getExportedFile() {
    return exportedFile;
  }

  public void setExportedFile(String exportedFile) {
    this.exportedFile = exportedFile;
  }

  public String getOrgFile() {
    return orgFile;
  }

  public void setOrgFile(String orgFile) {
    this.orgFile = orgFile;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExportFile)) {
      return false;
    }
    ExportFile that = (ExportFile) o;
    return Objects.equals(exportedFile, that.exportedFile) &&
        Objects.equals(orgFile, that.orgFile) &&
        Objects.equals(fileSize, that.fileSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exportedFile, orgFile, fileSize);
  }
}
