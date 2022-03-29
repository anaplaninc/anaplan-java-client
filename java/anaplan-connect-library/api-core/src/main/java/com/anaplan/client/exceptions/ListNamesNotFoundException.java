package com.anaplan.client.exceptions;

public class ListNamesNotFoundException extends RuntimeException {

  public ListNamesNotFoundException(String modelId, Throwable t) {
    super("List names not found for Model-ID=" + modelId, t);
  }
}
