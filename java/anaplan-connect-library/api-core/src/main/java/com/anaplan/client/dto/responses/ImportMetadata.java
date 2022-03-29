package com.anaplan.client.dto.responses;

import java.io.Serializable;

public class ImportMetadata implements Serializable {
  private String id;
  private String name;
  private String type;
  private Source source;
  private int chunkCount;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getChunkCount() {
    return chunkCount;
  }

  public void setChunkCount(int chunkCount) {
    this.chunkCount = chunkCount;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }
}
