package com.anaplan.client.exceptions;

public class ViewMetadataNotFoundException extends RuntimeException {

  public ViewMetadataNotFoundException(String viewId, Throwable t) {
    super("View metadata not found: " + viewId, t);
  }

}
