// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import java.util.List;

public class ModelTest extends BaseTest {
    public void testModel() throws AnaplanAPIException {
        Model testModel = getTestModel();
        checkModel(testModel);
        getService().setServiceCredentials(getLowerCaseCredentials());
        checkModel(testModel);
        getService().setServiceCredentials(getUpperCaseCredentials());
        checkModel(testModel);
        getService().setServiceCredentials(getIncorrectCredentials());
        try {
            checkModel(testModel);
            fail("Expected authentication failure");
        } catch (AnaplanAPIException apiException) {
        }
        getService().setServiceCredentials(getCorrectCredentials());
        try {
            checkModel(getArchivedModel());
            fail("Expected archived model failure");
        } catch (AnaplanAPIException apiException) {
        }
        checkModel(getLockedModel());
    }
    public void checkModel(Model model) throws AnaplanAPIException {
        assertNotNull(model);
        // Modules
        List<Module> modules = model.getModules();
        assertEquals(2, modules.size());
        Module module0 = modules.get(0);
        assertEquals("Module 0", module0.getName());
        String module0Id = module0.getId();
        Module module1 = modules.get(1);
        assertEquals("Module 1", module1.getName());
        String module1Id = module1.getId();
        module0 = model.getModule("Module 0");
        assertEquals(module0Id, module0.getId());
        module1 = model.getModule(module1Id);
        assertEquals("Module 1", module1.getName());

        // Files
        List<ServerFile> serverFiles = model.getServerFiles();
        assertEquals(2, Math.min(2, serverFiles.size()));
        assertEquals(3, Math.max(3, serverFiles.size()));
        ServerFile serverFile0 = serverFiles.get(0);
        assertEquals("File 0", serverFile0.getName());
        String serverFile0Id = serverFile0.getId();
        ServerFile serverFile1 = serverFiles.get(1);
        assertEquals("File 1", serverFile1.getName());
        String serverFile1Id = serverFile1.getId();
        serverFile0 = model.getServerFile("File 0");
        assertEquals(serverFile0Id, serverFile0.getId());
        serverFile1 = model.getServerFile(serverFile1Id);
        assertEquals("File 1", serverFile1.getName());

        // Imports
        List<Import> imports = model.getImports();
        assertEquals(2, imports.size());
        Import import0 = imports.get(0);
        assertEquals("List 0 from File 0", import0.getName());
        String import0Id = import0.getId();
        Import import1 = imports.get(1);
        assertEquals("Module 0 from File 0", import1.getName());
        String import1Id = import1.getId();
        import0 = model.getImport("LIST 0 from FILE 0");
        assertEquals(import0Id, import0.getId());
        import1 = model.getImport(import1Id);
        assertEquals("Module 0 from File 0", import1.getName());
        
        // Exports
        List<Export> exports = model.getExports();
        assertEquals(2, exports.size());
        Export export0 = exports.get(0);
        assertEquals("Export 0", export0.getName());
        String export0Id = export0.getId();
        Export export1 = exports.get(1);
        assertEquals("Export 1", export1.getName());
        String export1Id = export1.getId();
        export0 = model.getExport("export 0");
        assertEquals(export0Id, export0.getId());
        export1 = model.getExport(export1Id);
        assertEquals("Export 1", export1.getName());
        
        // Actions
        List<Action> actions = model.getActions();
        assertEquals(2, actions.size());
        Action action0 = actions.get(0);
        assertEquals("Action 0", action0.getName());
        String action0Id = action0.getId();
        Action action1 = actions.get(1);
        assertEquals("Action 1", action1.getName());
        String action1Id = action1.getId();
        action0 = model.getAction("action 0");
        assertEquals(action0Id, action0.getId());
        action1 = model.getAction(action1Id);
        assertEquals("Action 1", action1.getName());

        // Processes
        List<Process> processes = model.getProcesses();
        assertEquals(2, processes.size());
        Process process0 = processes.get(0);
        assertEquals("Process 0", process0.getName());
        String process0Id = process0.getId();
        Process process1 = processes.get(1);
        assertEquals("Process 1", process1.getName());
        String process1Id = process1.getId();
        process0 = model.getProcess("process 0");
        assertEquals(process0Id, process0.getId());
        process1 = model.getProcess(process1Id);
        assertEquals("Process 1", process1.getName());
    }
}
