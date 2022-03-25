package com.anaplan.client.exceptions;

public class ListItemsNotFoundException extends RuntimeException {

  public ListItemsNotFoundException(String workspaceId, Throwable t) {
    super("List items not not found for Workspace-ID=" + workspaceId, t);
  }
}
