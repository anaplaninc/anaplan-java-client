package com.anaplan.client.exceptions;

public class ListMetadataNotFoundException extends RuntimeException {

  public ListMetadataNotFoundException(String workspaceId, Throwable t) {
    super("List metadata not found for Workspace-ID=" + workspaceId, t);
  }
}
