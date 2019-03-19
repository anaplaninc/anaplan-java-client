// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import com.anaplan.client.dto.responses.ModelsResponse;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.doReturn;


public class WorkspaceTest extends BaseTest {

    private static final String listOfModelsFixture = "responses/list_of_models_response.json";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testWorkspace() throws Exception {
        Workspace testWorkspace = getTestWorkspace();
        assertNotNull(testWorkspace.getId());
    }
}
