// Copyright 2012 Anaplan Limited
package com.anaplan.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anaplan.client.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class WorkspaceTest extends BaseTest {

  @BeforeEach
  public void setUp() throws Exception {
    recordActionsFetchMockWorkspaces();
  }

  @Test
  void testWorkspace() throws Exception {
    Workspace testWorkspace = getTestWorkspacebyId();
    assertEquals("testWorkspaceNameOrId", testWorkspace.getId());
    assertEquals("Workspace A", testWorkspace.getName());
    testWorkspace = getTestWorkspacebyName();
    assertEquals("testWorkspaceNameOrId", testWorkspace.getId());
    assertEquals("Workspace A", testWorkspace.getName());
  }
}
