// Copyright 2012 Anaplan Limited
package com.anaplan.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anaplan.client.Service;
import com.anaplan.client.Workspace;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.exceptions.AnaplanAPITransportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class AuthenticationTest extends BaseTest {

  private Service mockService;

  @BeforeEach
  public void setUp() throws AnaplanAPITransportException {
    mockService = getMockService();
  }

  @Test
  void testGoodAuth() throws Exception {
    recordActionsFetchMockWorkspaces();
    checkWorkspace();
  }

  public void checkWorkspace() throws AnaplanAPIException {
    Workspace workspace = mockService.getWorkspace(getTestWorkspaceNameOrId());
    assertEquals(getTestWorkspaceNameOrId(), workspace.getId());
    assertEquals("Workspace A", workspace.getName());
  }
}
