// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import com.anaplan.client.dto.responses.ModelsResponse;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doReturn;


public class WorkspaceTest extends BaseTest {

    private static final String listOfModelsFixture = "responses/list_of_models_response.json";

    @Before
    public void setUp() throws Exception {
        recordActionsFetchMockWorkspace();
    }

    public void checkWorkspace(Workspace workspace) throws IOException {
        doReturn(createFeignResponse(listOfModelsFixture, ModelsResponse.class))
                .when(getMockAnaplanApi())
                .getModels(getTestUserId(), 0);
        List<Model> models = Lists.newArrayList(workspace.getModels());
        assertEquals(4, models.size());
        String[] modelIds = new String[4];
        for (int i = 0; i < 4; ++i) {
            Model model = models.get(i);
            assertEquals("Model " + i, model.getName());
            modelIds[i] = model.getId();
        }
    }

    @Test
    public void testWorkspace() throws Exception {
        Workspace testWorkspace = getTestWorkspace();
        recordActionsFetchMockModel();
        checkWorkspace(testWorkspace);
    }
}
