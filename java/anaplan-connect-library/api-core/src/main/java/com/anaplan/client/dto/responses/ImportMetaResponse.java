package com.anaplan.client.dto.responses;

public class ImportMetaResponse extends BaseResponse {

  private ImportMetadata importMetadata;

  public ImportMetadata getImportMetadata() {
    return importMetadata;
  }

  public void setImportMetadata(ImportMetadata importMetadata) {
    this.importMetadata = importMetadata;
  }
}
