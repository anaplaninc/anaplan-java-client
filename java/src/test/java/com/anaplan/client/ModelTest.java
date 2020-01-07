// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import com.anaplan.client.dto.responses.ActionsResponse;
import com.anaplan.client.dto.responses.ExportsResponse;
import com.anaplan.client.dto.responses.ImportsResponse;
import com.anaplan.client.dto.responses.ModulesResponse;
import com.anaplan.client.dto.responses.ProcessesResponse;
import com.anaplan.client.dto.responses.ServerFilesResponse;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


public class ModelTest extends BaseTest {

    private Model mockModel;

    @Before
    public void setUp() throws Exception {
        mockModel = fetchMockModel();
    }


    @Test
    public void testModelFetchModules() throws Exception {
        assertNotNull(mockModel);
        when(mockModel.getApi().getModules(
                mockModel.getWorkspace().getId(), mockModel.getId(), 0))
                .thenReturn(createFeignResponse("responses/list_of_modules_response.json", ModulesResponse.class));
        List<Module> modules = Lists.newArrayList(mockModel.getModules());
        assertEquals(2, modules.size());
        Module module0 = modules.get(0);
        assertEquals("Module 0", module0.getName());
        String module0Id = module0.getId();
        Module module1 = modules.get(1);
        assertEquals("Module 1", module1.getName());
        String module1Id = module1.getId();
        module0 = mockModel.getModule("Module 0");
        assertEquals(module0Id, module0.getId());
        module1 = mockModel.getModule(module1Id);
        assertEquals("Module 1", module1.getName());
    }

    @Test
    public void testModelFetchFiles() throws Exception {
        assertNotNull(mockModel);
        when(mockModel.getApi().getServerFiles(
                mockModel.getWorkspace().getId(), mockModel.getId(), 0))
                .thenReturn(createFeignResponse("responses/list_of_files_response.json", ServerFilesResponse.class));
        List<ServerFile> serverFiles = Lists.newArrayList(mockModel.getServerFiles());
        assertEquals(2, Math.min(2, serverFiles.size()));
        assertEquals(3, Math.max(3, serverFiles.size()));
        ServerFile serverFile0 = serverFiles.get(0);
        assertEquals("File 0", serverFile0.getName());
        String serverFile0Id = serverFile0.getId();
        ServerFile serverFile1 = serverFiles.get(1);
        assertEquals("File 1", serverFile1.getName());
        String serverFile1Id = serverFile1.getId();
        serverFile0 = mockModel.getServerFile("File 0");
        assertEquals(serverFile0Id, serverFile0.getId());
        serverFile1 = mockModel.getServerFile(serverFile1Id);
        assertEquals("File 1", serverFile1.getName());
    }

    @Test
    public void testModelFetchImports() throws Exception {
        assertNotNull(mockModel);
        when(mockModel.getApi().getImports(
                mockModel.getWorkspace().getId(), mockModel.getId(), 0))
                .thenReturn(createFeignResponse("responses/list_of_imports_response.json", ImportsResponse.class));
        List<Import> imports = Lists.newArrayList(mockModel.getImports());
        assertEquals(2, imports.size());
        Import import0 = imports.get(0);
        assertEquals("List 0 from File 0", import0.getName());
        String import0Id = import0.getId();
        Import import1 = imports.get(1);
        assertEquals("Module 0 from File 0", import1.getName());
        String import1Id = import1.getId();
        import0 = mockModel.getImport("LIST 0 from FILE 0");
        assertEquals(import0Id, import0.getId());
        import1 = mockModel.getImport(import1Id);
        assertEquals("Module 0 from File 0", import1.getName());
    }

    @Test
    public void testModelFetchExports() throws Exception {
        assertNotNull(mockModel);
        when(mockModel.getApi().getExports(
                mockModel.getWorkspace().getId(), mockModel.getId(), 0))
                .thenReturn(createFeignResponse("responses/list_of_exports_response.json", ExportsResponse.class));
        List<Export> exports = Lists.newArrayList(mockModel.getExports());
        assertEquals(2, exports.size());
        Export export0 = exports.get(0);
        assertEquals("Export 0", export0.getName());
        String export0Id = export0.getId();
        Export export1 = exports.get(1);
        assertEquals("Export 1", export1.getName());
        String export1Id = export1.getId();
        export0 = mockModel.getExport("export 0");
        assertEquals(export0Id, export0.getId());
        export1 = mockModel.getExport(export1Id);
        assertEquals("Export 1", export1.getName());
    }

    @Test
    public void testModelFetchActions() throws Exception {
        assertNotNull(mockModel);
        when(mockModel.getApi().getActions(
                mockModel.getWorkspace().getId(), mockModel.getId(), 0))
                .thenReturn(createFeignResponse("responses/list_of_actions_response.json", ActionsResponse.class));
        List<Action> actions = Lists.newArrayList(mockModel.getActions());
        assertEquals(2, actions.size());
        Action action0 = actions.get(0);
        assertEquals("Action 0", action0.getName());
        String action0Id = action0.getId();
        Action action1 = actions.get(1);
        assertEquals("Action 1", action1.getName());
        String action1Id = action1.getId();
        action0 = mockModel.getAction("action 0");
        assertEquals(action0Id, action0.getId());
        action1 = mockModel.getAction(action1Id);
        assertEquals("Action 1", action1.getName());
    }

    @Test
    public void testModelFetchProcesses() throws Exception {
        assertNotNull(mockModel);
        when(mockModel.getApi().getProcesses(
                mockModel.getWorkspace().getId(), mockModel.getId(), 0))
                .thenReturn(createFeignResponse("responses/list_of_processes_response.json", ProcessesResponse.class));
        List<Process> processes = Lists.newArrayList(mockModel.getProcesses());
        assertEquals(2, processes.size());
        Process process0 = processes.get(0);
        assertEquals("Process 0", process0.getName());
        String process0Id = process0.getId();
        Process process1 = processes.get(1);
        assertEquals("Process 1", process1.getName());
        String process1Id = process1.getId();
        process0 = mockModel.getProcess("process 0");
        assertEquals(process0Id, process0.getId());
        process1 = mockModel.getProcess(process1Id);
        assertEquals("Process 1", process1.getName());
    }
}
