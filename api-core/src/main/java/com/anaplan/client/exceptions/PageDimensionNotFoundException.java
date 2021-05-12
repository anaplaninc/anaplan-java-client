package com.anaplan.client.exceptions;

public class PageDimensionNotFoundException extends RuntimeException {

  public PageDimensionNotFoundException(String pageDimension) {
    super(pageDimension);
  }
}
