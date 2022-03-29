package com.anaplan.client.dto;

import java.util.List;

public class ViewMetadata {

  private String viewName;
  private String viewId;
  private List<ViewMetadataRow> columns;
  private List<ViewMetadataRow> rows;
  private List<ViewMetadataRow> pages;

  public ViewMetadata() {
    //
  }

  public ViewMetadata(String viewName, String viewId, List<ViewMetadataRow> columns, List<ViewMetadataRow> rows, List<ViewMetadataRow> pages) {
    this.viewName = viewName;
    this.viewId = viewId;
    this.columns = columns;
    this.rows = rows;
    this.pages = pages;
  }

  public String getViewName() {
    return viewName;
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  public String getViewId() {
    return viewId;
  }

  public void setViewId(String viewId) {
    this.viewId = viewId;
  }

  public List<ViewMetadataRow> getColumns() {
    return columns;
  }

  public void setColumns(List<ViewMetadataRow> columns) {
    this.columns = columns;
  }

  public List<ViewMetadataRow> getRows() {
    return rows;
  }

  public void setRows(List<ViewMetadataRow> rows) {
    this.rows = rows;
  }

  public List<ViewMetadataRow> getPages() {
    return pages;
  }

  public void setPages(List<ViewMetadataRow> pages) {
    this.pages = pages;
  }

}
