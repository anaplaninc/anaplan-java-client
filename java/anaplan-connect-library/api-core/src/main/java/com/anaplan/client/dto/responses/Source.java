package com.anaplan.client.dto.responses;

import java.io.Serializable;
import java.util.List;

public class Source implements Serializable {
  private String textEncoding;
  private String columnSeparator;
  private String textDelimiter;
  private int headerRow;
  private int firstDataRow;
  private String decimalSeparator;
  private List<String> headerNames;
  private int columnCount;

  public String getTextEncoding() {
    return textEncoding;
  }

  public void setTextEncoding(String textEncoding) {
    this.textEncoding = textEncoding;
  }

  public String getColumnSeparator() {
    return columnSeparator;
  }

  public void setColumnSeparator(String columnSeparator) {
    this.columnSeparator = columnSeparator;
  }

  public String getTextDelimiter() {
    return textDelimiter;
  }

  public void setTextDelimiter(String textDelimiter) {
    this.textDelimiter = textDelimiter;
  }

  public int getHeaderRow() {
    return headerRow;
  }

  public void setHeaderRow(int headerRow) {
    this.headerRow = headerRow;
  }

  public int getFirstDataRow() {
    return firstDataRow;
  }

  public void setFirstDataRow(int firstDataRow) {
    this.firstDataRow = firstDataRow;
  }

  public String getDecimalSeparator() {
    return decimalSeparator;
  }

  public void setDecimalSeparator(String decimalSeparator) {
    this.decimalSeparator = decimalSeparator;
  }

  public List<String> getHeaderNames() {
    return headerNames;
  }

  public void setHeaderNames(List<String> headerNames) {
    this.headerNames = headerNames;
  }

  public int getColumnCount() {
    return columnCount;
  }

  public void setColumnCount(int columnCount) {
    this.columnCount = columnCount;
  }
}
