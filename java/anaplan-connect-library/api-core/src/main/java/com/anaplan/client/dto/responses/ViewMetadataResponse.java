package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ViewMetadata;
import com.anaplan.client.dto.ViewMetadataRow;
import java.util.List;

public class ViewMetadataResponse extends BaseResponse {

  private String viewName;
  private String viewId;
  private List<ViewMetadataRow> columns;
  private List<ViewMetadataRow> rows;
  private List<ViewMetadataRow> pages;

  public ViewMetadata getViewMetadata(){
    return new ViewMetadata(viewName, viewId, columns, rows, pages);
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
