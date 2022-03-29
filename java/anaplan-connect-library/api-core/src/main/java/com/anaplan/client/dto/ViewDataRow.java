package com.anaplan.client.dto;

import java.util.List;

public class ViewDataRow {

  private List<ViewDataCoordinate> rowCoordinates;
  private List<ViewDataCell> cells;

  public List<ViewDataCoordinate> getRowCoordinates() {
    return rowCoordinates;
  }

  public void setRowCoordinates(List<ViewDataCoordinate> rowCoordinates) {
    this.rowCoordinates = rowCoordinates;
  }

  public List<ViewDataCell> getCells() {
    return cells;
  }

  public void setCells(List<ViewDataCell> cells) {
    this.cells = cells;
  }
}
