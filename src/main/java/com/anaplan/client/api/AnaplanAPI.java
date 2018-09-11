package com.anaplan.client.api;

import com.anaplan.client.dto.ActionData;
import com.anaplan.client.dto.ExportData;
import com.anaplan.client.dto.ImportData;
import com.anaplan.client.dto.ProcessData;
import com.anaplan.client.dto.ServerFileData;
import com.anaplan.client.dto.responses.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * Created by Spondon Saha
 * Date: 4/17/18
 * Time: 3:21 PM
 */
@Headers({"Content-Type: application/json"})
public interface AnaplanAPI {

    /* Users */

    @RequestLine("GET /users/me")
    UserResponse getUser();

    /* Workspaces */

    @RequestLine("GET /users/{userId}/workspaces?offset={offset}")
    WorkspacesResponse getWorkspaces(
            @Param("userId") String userId,
            @Param("offset") int offset);

    @RequestLine("GET /users/{userId}/workspaces/{workspaceId}")
    WorkspaceResponse getWorkspace(
            @Param("userId") String userId,
            @Param("workspaceId") String workspaceId);

    /* Models */

    @RequestLine("GET /users/{userId}/models?offset={offset}")
    ModelsResponse getModels(
            @Param("userId") String userId,
            @Param("offset") int offset);

    @RequestLine("GET /users/{userId}/models/{modelId}")
    ModelResponse getModel(
            @Param("userId") String userId,
            @Param("modelId") String modelId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/modules?offset={offset}")
    ModulesResponse getModules(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("offset") int offset);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/modules/{moduleId}/views?offset={offset}")
    ViewsResponse getViews(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("moduleId") String moduleId,
            @Param("offset") int offset);

    /* Files */

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/files?offset={offset}")
    ServerFilesResponse getServerFiles(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("offset") int offset);

    @RequestLine("POST /workspaces/{workspaceId}/models/{modelId}/files/{name}")
    ServerFileResponse createImportDataSource(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("name") String name,
            ServerFileData createFileData);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/files/{fileId}/chunks")
    ChunksResponse getChunks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("fileId") String fileId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/files/{fileId}/chunks/{chunkId}")
    byte[] getChunkContent(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("fileId") String fileId,
            @Param("chunkId") String chunkId);

    @RequestLine("POST /workspaces/{workspaceId}/models/{modelId}/files/{fileId}")
    ServerFileResponse upsertFileDataSource(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("fileId") String fileId,
            ServerFileData createFileData);

    @RequestLine("PUT /workspaces/{workspaceId}/models/{modelId}/files/{fileId}/chunks/{chunkId}")
    @Headers("Content-Type: application/octet-stream")
    void uploadChunk(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("fileId") String fileId,
            @Param("chunkId") String chunkId,
            byte[] fileData);

    @RequestLine("PUT /workspaces/{workspaceId}/models/{modelId}/files/{fileId}/chunks/{chunkId}")
    @Headers("Content-Type: application/x-gzip")
    void uploadChunkCompressed(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("fileId") String fileId,
            @Param("chunkId") String chunkId,
            byte[] fileData);

    @RequestLine("POST /workspaces/{workspaceId}/models/{modelId}/files/{fileId}/complete")
    ServerFileResponse completeUpload(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("fileId") String fileId,
            ServerFileData serverFileData);

    /* Imports */

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/imports?offset={offset}")
    ImportsResponse getImports(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("offset") int offset);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/imports/{importId}/tasks?offset={offset}")
    TasksResponse getImportTasks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("importId") String importId,
            @Param("offset") int offset);

    @RequestLine("POST /workspaces/{workspaceId}/models/{modelId}/imports/{importId}/tasks")
    TaskResponse createImportTask(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("importId") String importId,
            ImportData importData);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/imports/{importId}/tasks/{taskId}")
    TaskStatusResponse getImportTaskStatus(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("importId") String importId,
            @Param("taskId") String taskId);

    @RequestLine("DELETE /workspaces/{workspaceId}/models/{modelId}/imports/{importId}/tasks/{taskId}")
    TaskStatusResponse cancelImportTask(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("importId") String importId,
            @Param("taskId") String taskId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/imports/{importId}/tasks/{taskId}/dump/chunks")
    ChunksResponse getImportDumpFileChunks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("importId") String importId,
            @Param("taskId") String taskId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/imports/{importId}/tasks/{taskId}/dump/chunks/{chunkId}")
    byte[] getImportDumpFileChunkContent(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("importId") String importId,
            @Param("taskId") String taskId,
            @Param("chunkId") String chunkId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/imports/{importId}/tasks/{taskId}/dumps/{nestedId}/chunks")
    ChunksResponse getImportNestedDumpFileChunks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("importId") String importId,
            @Param("taskId") String taskId,
            @Param("nestedId") String nestedId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/imports/{importId}/tasks/{taskId}/dumps/{nestedId}/chunks/{chunkId}")
    byte[] getImportNestedDumpFileChunkContent(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("importId") String importId,
            @Param("taskId") String taskId,
            @Param("nestedId") String nestedId,
            @Param("chunkId") String chunkId);

    /* Exports */

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/exports?offset={offset}")
    ExportsResponse getExports(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("offset") int offset);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/exports/{exportId}")
    ExportMetadataResponse getExport(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("exportId") String exportId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/exports/{exportId}/tasks?offset={offset}")
    TasksResponse getExportTasks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("exportId") String exportId,
            @Param("offset") int offset);

    @RequestLine("POST /workspaces/{workspaceId}/models/{modelId}/exports/{exportId}/tasks")
    TaskResponse createExportTask(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("exportId") String exportId,
            ExportData exportData);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/exports/{exportId}/tasks/{taskId}")
    TaskStatusResponse getExportTaskStatus(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("exportId") String exportId,
            @Param("taskId") String taskId);

    @RequestLine("DELETE /workspaces/{workspaceId}/models/{modelId}/exports/{exportId}/tasks/{taskId}")
    TaskStatusResponse cancelExportTask(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("exportId") String exportId,
            @Param("taskId") String taskId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/exports/{exportId}/tasks/{taskId}/dump/chunks")
    ChunksResponse getExportDumpFileChunks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("exportId") String exportId,
            @Param("taskId") String taskId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/exports/{exportId}/tasks/{taskId}/dump/chunks/{chunkId}")
    byte[] getExportDumpFileChunkContent(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("exportId") String exportId,
            @Param("taskId") String taskId,
            @Param("chunkId") String chunkId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/exports/{exportId}/tasks/{taskId}/dumps/{nestedId}/chunks")
    ChunksResponse getExportNestedDumpFileChunks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("exportId") String exportId,
            @Param("taskId") String taskId,
            @Param("nestedId") String nestedId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/exports/{exportId}/tasks/{taskId}/dumps/{nestedId}/chunks/{chunkId}")
    byte[] getExportNestedDumpFileChunkContent(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("exportId") String exportId,
            @Param("taskId") String taskId,
            @Param("nestedId") String nestedId,
            @Param("chunkId") String chunkId);

    /* Actions */

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/actions?offset={offset}")
    ActionsResponse getActions(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("offset") int offset);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/actions/{actionId}/tasks?offset={offset}")
    TasksResponse getActionTasks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("actionId") String actionId,
            @Param("offset") int offset);

    @RequestLine("POST /workspaces/{workspaceId}/models/{modelId}/actions/{actionId}/tasks")
    TaskResponse createActionTask(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("actionId") String actionId,
            ActionData actionData);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/actions/{actionId}/tasks/{taskId}")
    TaskStatusResponse getActionTaskStatus(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("actionId") String actionId,
            @Param("taskId") String taskId);

    @RequestLine("DELETE /workspaces/{workspaceId}/models/{modelId}/actions/{actionId}/tasks/{taskId}")
    TaskStatusResponse cancelActionTask(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("actionId") String actionId,
            @Param("taskId") String taskId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/actions/{actionId}/tasks/{taskId}/dump/chunks")
    ChunksResponse getActionDumpFileChunks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("actionId") String actionId,
            @Param("taskId") String taskId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/actions/{actionId}/tasks/{taskId}/dump/chunks/{chunkId}")
    byte[] getActionDumpFileChunkContent(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("actionId") String actionId,
            @Param("taskId") String taskId,
            @Param("chunkId") String chunkId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/actions/{actionId}/tasks/{taskId}/dumps/{nestedId}/chunks")
    ChunksResponse getActionNestedDumpFileChunks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("actionId") String actionId,
            @Param("taskId") String taskId,
            @Param("nestedId") String nestedId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/actions/{actionId}/tasks/{taskId}/dumps/{nestedId}/chunks/{chunkId}")
    byte[] getActionNestedDumpFileChunkContent(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("actionId") String actionId,
            @Param("taskId") String taskId,
            @Param("nestedId") String nestedId,
            @Param("chunkId") String chunkId);

    /* Process */

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/processes?offset={offset}")
    ProcessesResponse getProcesses(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("offset") int offset);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/processes/{processId}/tasks?offset={offset}")
    TasksResponse getProcessTasks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("processId") String processId,
            @Param("offset") int offset);

    @RequestLine("POST /workspaces/{workspaceId}/models/{modelId}/processes/{processId}/tasks")
    TaskResponse createProcessTask(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("processId") String processId,
            ProcessData processData);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/processes/{processId}/tasks/{taskId}")
    TaskStatusResponse getProcessTaskStatus(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("processId") String processId,
            @Param("taskId") String taskId);

    @RequestLine("DELETE /workspaces/{workspaceId}/models/{modelId}/processes/{processId}/tasks/{taskId}")
    TaskStatusResponse cancelProcessTask(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("processId") String processId,
            @Param("taskId") String taskId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/processes/{processId}/tasks/{taskId}/dump/chunks")
    ChunksResponse getProcessDumpFileChunks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("processId") String processId,
            @Param("taskId") String taskId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/processes/{processId}/tasks/{taskId}/dump/chunks/{chunkId}")
    byte[] getProcessDumpFileChunkContent(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("processId") String processId,
            @Param("taskId") String taskId,
            @Param("chunkId") String chunkId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/processes/{processId}/tasks/{taskId}/dumps/{nestedId}/chunks")
    ChunksResponse getProcessNestedDumpFileChunks(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("processId") String processId,
            @Param("taskId") String taskId,
            @Param("nestedId") String nestedId);

    @RequestLine("GET /workspaces/{workspaceId}/models/{modelId}/processes/{processId}/tasks/{taskId}/dumps/{nestedId}/chunks/{chunkId}")
    byte[] getProcessNestedDumpFileChunkContent(
            @Param("workspaceId") String workspaceId,
            @Param("modelId") String modelId,
            @Param("processId") String processId,
            @Param("taskId") String taskId,
            @Param("nestedId") String nestedId,
            @Param("chunkId") String chunkId);
}