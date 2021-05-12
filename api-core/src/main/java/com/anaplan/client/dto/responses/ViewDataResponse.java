package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ViewDataCoordinate;
import com.anaplan.client.dto.ViewDataPage;
import com.anaplan.client.dto.ViewDataRow;
import java.util.List;

public class ViewDataResponse {

  private List<ViewDataPage> pages;
  private List<ViewDataCoordinate> columnCoordinate;
  private List<ViewDataRow> rows;

  public List<ViewDataPage> getPages() {
    return pages;
  }

  public void setPages(List<ViewDataPage> pages) {
    this.pages = pages;
  }

  public List<ViewDataCoordinate> getColumnCoordinate() {
    return columnCoordinate;
  }

  public void setColumnCoordinate(List<ViewDataCoordinate> columnCoordinate) {
    this.columnCoordinate = columnCoordinate;
  }

  public List<ViewDataRow> getRows() {
    return rows;
  }

  public void setRows(List<ViewDataRow> rows) {
    this.rows = rows;
  }

}
