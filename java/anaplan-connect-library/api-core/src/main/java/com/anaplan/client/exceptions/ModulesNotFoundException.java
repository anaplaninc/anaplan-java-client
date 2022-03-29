package com.anaplan.client.exceptions;

public class ModulesNotFoundException extends RuntimeException {

  public ModulesNotFoundException(String modelId, Throwable t) {
    super("Modules not found for Model-ID=" + modelId, t);
  }
}
