package com.anaplan.client.exceptions;

/**
 * Used for
 */
public class InvalidTaskResultDetail extends RuntimeException {

  public InvalidTaskResultDetail(Throwable t) {
    super("Encountered error while parsing task-result-detail from API: ", t);
  }
}
