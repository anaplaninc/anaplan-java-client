package com.anaplan.client.exceptions;


public class ViewDimensionMetadataNotFoundException extends RuntimeException {

  public ViewDimensionMetadataNotFoundException(String dimensionId, Throwable t) {
    super("View dimension metadata not found: " + dimensionId, t);
  }

}
