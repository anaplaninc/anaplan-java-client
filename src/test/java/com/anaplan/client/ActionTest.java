// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import com.anaplan.client.dto.ActionData;
import com.anaplan.client.dto.ExportData;
import com.anaplan.client.dto.ImportData;
import com.anaplan.client.dto.ProcessData;
import com.anaplan.client.dto.responses.ActionsResponse;
import com.anaplan.client.dto.responses.ExportsResponse;
import com.anaplan.client.dto.responses.ImportsResponse;
import com.anaplan.client.dto.responses.ProcessesResponse;
import com.anaplan.client.dto.responses.TaskResponse;
import com.anaplan.client.dto.responses.TaskStatusResponse;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;


public class ActionTest extends BaseTest {

    private Model mockModel;
    private TaskResult taskResult;
    private static final String createTaskResponseJson = "responses/create_task_response.json";
    private static final String taskSuccessResponseJson = "responses/task_success_response.json";
    private static final String processSuccessResponseJson = "responses/process_success_response.json";
    private static final String listOfActionsResponseJson = "responses/list_of_actions_response.json";
    private static final String listOfImportsResponseJson = "responses/list_of_imports_response.json";
    private static final String listOfExportsResponseJson = "responses/list_of_exports_response.json";
    private static final String listOfProcessesResponseJson = "responses/list_of_processes_response.json";

    @Before
    public void setUp() throws Exception {
        mockModel = fetchMockModel();
    }

    @Test
    public void testImportAction() throws Exception {
        // create mock API response for fetching list of imports.
        when(mockModel.getApi().getImports(mockModel.getWorkspace().getId(), mockModel.getId(), 0))
                .thenReturn(createFeignResponse(listOfImportsResponseJson, ImportsResponse.class));

        Import mockImport = mockModel.getImport("List 0 from File 0");

        // mock out API calls to create Task and monitor it
        ImportData importData = new ImportData() {{
            this.localeName = "en_UK";
        }};
        when(mockModel.getApi().createImportTask(anyString(), anyString(), anyString(), any(ImportData.class)))
                .thenReturn(createFeignResponse(createTaskResponseJson, TaskResponse.class));
        when(mockModel.getApi().getImportTaskStatus(
                mockModel.getWorkspace().getId(), mockModel.getId(), mockImport.getId(),
                "task-id"))
                .thenReturn(createFeignResponse(taskSuccessResponseJson, TaskStatusResponse.class));

        taskResult = runTask(mockImport, new TaskParameters() {{
            setData(importData);
        }});
        assertNotNull(taskResult);
        assertTrue(taskResult.isSuccessful());
        assertTrue(taskResult.isFailureDumpAvailable());
    }

    @Test
    public void testAction() throws Exception {
        // create mock API response for fetching list of Actions.
        when(mockModel.getApi().getActions(mockModel.getWorkspace().getId(),
                mockModel.getId(), 0))
                .thenReturn(createFeignResponse(listOfActionsResponseJson, ActionsResponse.class));
        Action mockAction = mockModel.getAction("Action 0");

        // mock out API calls to create task and monitor
        when(mockModel.getApi().createActionTask(anyString(), anyString(), anyString(),
                any(ActionData.class)))
                .thenReturn(createFeignResponse(createTaskResponseJson, TaskResponse.class));
        when(mockModel.getApi().getActionTaskStatus(
                mockModel.getWorkspace().getId(), mockModel.getId(), mockAction.getId(),
                "task-id"))
                .thenReturn(createFeignResponse(taskSuccessResponseJson, TaskStatusResponse.class));

        taskResult = runTask(mockAction, null);
        assertNotNull(taskResult);
        assertTrue(taskResult.isSuccessful());
    }

    @Test
    public void testExport() throws Exception {
        // create mock API response for fetching list of Exports
        when(mockModel.getApi().getExports(mockModel.getWorkspace().getId(),
                mockModel.getId(), 0)).thenReturn(
                createFeignResponse(listOfExportsResponseJson, ExportsResponse.class));
        Export mockExport = mockModel.getExport("Export 0");

        // mock out API calls to create Task and monitor it
        when(mockModel.getApi().createExportTask(anyString(), anyString(), anyString(),
                any(ExportData.class)))
                .thenReturn(createFeignResponse(createTaskResponseJson, TaskResponse.class));
        when(mockModel.getApi().getExportTaskStatus(
                mockModel.getWorkspace().getId(), mockModel.getId(), mockExport.getId(),
                "task-id"))
                .thenReturn(createFeignResponse(taskSuccessResponseJson, TaskStatusResponse.class));

        taskResult = runTask(mockExport, null);
        assertNotNull(taskResult);
        assertTrue(taskResult.isSuccessful());
    }

    @Test
    public void testProcess() throws Exception {
        // create mock API response for fetching list of processes
        when(mockModel.getApi().getProcesses(mockModel.getWorkspace().getId(),
                mockModel.getId(), 0)).thenReturn(
                createFeignResponse(listOfProcessesResponseJson, ProcessesResponse.class));
        Process mockProcess = mockModel.getProcess("Process 0");

        // mock out API calls to create task and monitor
        when(mockModel.getApi().createProcessTask(
                anyString(), anyString(), anyString(), any(ProcessData.class)))
                .thenReturn(createFeignResponse(createTaskResponseJson, TaskResponse.class));
        when(mockModel.getApi().getProcessTaskStatus(
                mockModel.getWorkspace().getId(), mockModel.getId(), mockProcess.getId(),
                "task-id"))
                .thenReturn(createFeignResponse(processSuccessResponseJson, TaskStatusResponse.class));
        taskResult = runTask(mockProcess, null);
        assertNotNull(taskResult);
        assertTrue(taskResult.isSuccessful());
        assertEquals(4, taskResult.getNestedResults().size());

        // verify nested results
        TaskResult nestedResult = taskResult.getNestedResults().get(0);
        assertTrue(nestedResult.isSuccessful());
        assertFalse(nestedResult.isFailureDumpAvailable());
    }

}
