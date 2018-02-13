// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import java.util.List;

public class WorkspaceTest extends BaseTest {
    public void testWorkspace() throws AnaplanAPIException {
        Workspace testWorkspace = getTestWorkspace();
        checkWorkspace(testWorkspace);
        getService().setServiceCredentials(getLowerCaseCredentials());
        checkWorkspace(testWorkspace);
        getService().setServiceCredentials(getUpperCaseCredentials());
        checkWorkspace(testWorkspace);
        getService().setServiceCredentials(getIncorrectCredentials());
        try {
            checkWorkspace(testWorkspace);
            fail("Expected authentication failure");
        } catch (AnaplanAPIException apiException) {
        }
    }
    public void checkWorkspace(Workspace workspace) throws AnaplanAPIException {
        List<Model> models = workspace.getModels();
        assertEquals(3, models.size());
        String[] modelIds = new String[3];
        for (int i = 0; i < 3; ++i) {
            Model model = models.get(i);
            assertEquals("Model " + i, model.getName());
            modelIds[i] = model.getId();
        }
        Model model = workspace.getModel(modelIds[1]);
        assertEquals(modelIds[1], model.getId());
        assertEquals("Model 1", model.getName());
    }
}
