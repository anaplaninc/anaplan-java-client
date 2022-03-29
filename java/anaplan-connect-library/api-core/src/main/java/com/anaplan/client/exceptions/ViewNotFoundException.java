package com.anaplan.client.exceptions;

public class ViewNotFoundException extends RuntimeException {

  public ViewNotFoundException(String view) {
    super(view);
  }

}