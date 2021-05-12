package com.anaplan.client.exceptions;

public class ViewDataNotFoundException extends RuntimeException {

  public ViewDataNotFoundException(String viewId, Throwable t) {
    super("View data not found: " + viewId, t);
  }

}
