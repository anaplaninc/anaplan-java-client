package com.anaplan.client.dto;

import java.io.Serializable;
import java.util.SortedMap;

public class ListMetadataProperty implements Serializable {

  private String name;
  private String format;
  private String formula;
  private String referencedBy;
  private SortedMap<String, String> formatMetadata;
  private String id;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getFormula() {
    return formula;
  }

  public void setFormula(String formula) {
    this.formula = formula;
  }

  public String getReferencedBy() {
    return referencedBy;
  }

  public void setReferencedBy(String referencedBy) {
    this.referencedBy = referencedBy;
  }

  public SortedMap<String, String> getFormatMetadata() {
    return formatMetadata;
  }

  public void setFormatMetadata(SortedMap<String, String> formatMetadata) {
    this.formatMetadata = formatMetadata;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}