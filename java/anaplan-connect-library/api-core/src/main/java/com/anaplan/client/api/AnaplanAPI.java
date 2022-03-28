package com.anaplan.client.api;

import com.anaplan.client.dto.ActionData;
import com.anaplan.client.dto.ExportData;
import com.anaplan.client.dto.ImportData;
import com.anaplan.client.dto.ListItemParametersData;
import com.anaplan.client.dto.ListItemResultData;
import com.anaplan.client.dto.ProcessData;
import com.anaplan.client.dto.ServerFileData;
import com.anaplan.client.dto.responses.ActionsResponse;
import com.anaplan.client.dto.responses.ChunksResponse;
import com.anaplan.client.dto.responses.ExportMetadataResponse;
import com.anaplan.client.dto.responses.ExportsResponse;
import com.anaplan.client.dto.responses.ImportMetaResponse;
import com.anaplan.client.dto.responses.ImportsResponse;
import com.anaplan.client.dto.responses.ItemData;
import com.anaplan.client.dto.responses.ItemMetadataResponse;
import com.anaplan.client.dto.responses.ListItemsResponse;
import com.anaplan.client.dto.responses.ListMetadataResponse;
import com.anaplan.client.dto.responses.ListNamesResponse;
import com.anaplan.client.dto.responses.ModelResponse;
import com.anaplan.client.dto.responses.ModelsResponse;
import com.anaplan.client.dto.responses.ModulesResponse;
import com.anaplan.client.dto.responses.ProcessesResponse;
import com.anaplan.client.dto.responses.ServerFileResponse;
import com.anaplan.client.dto.responses.ServerFilesResponse;
import com.anaplan.client.dto.responses.TaskResponse;
import com.anaplan.client.dto.responses.TaskStatusResponse;
import com.anaplan.client.dto.responses.TasksResponse;
import com.anaplan.client.dto.responses.ViewDataResponse;
import com.anaplan.client.dto.responses.ViewDimensionMetadataResponse;
import com.anaplan.client.dto.responses.ViewMetadataResponse;
import com.anaplan.client.dto.responses.ViewsResponse;
import com.anaplan.client.dto.responses.WorkspaceResponse;
import com.anaplan.client.dto.responses.WorkspacesResponse;

/**
 * Created by Spondon Saha Date: 4/17/18 Time: 3:21 PM
 */
public interface AnaplanAPI {

  String URL_IMPORT_TASKS = "/workspaces/{workspaceId}/models/{modelId}/imports/{importId}/tasks";

  /* Workspaces */

  WorkspacesResponse getWorkspaces(int offset);

  WorkspacesResponse getWorkspaces(int offset, int limit);

  WorkspaceResponse getWorkspace(String workspaceId);

  /* Models */
  ModelsResponse getModels(int offset);

  ModelsResponse getModels(int offset, int limit);

  ModelResponse getModel(String modelId);

  ModulesResponse getModules(
      String workspaceId,
      String modelId,
      int offset);

  /* Files */

  ServerFilesResponse getServerFiles(
      String workspaceId,
      String modelId,
      int offset);

  ServerFileResponse createImportDataSource(
      String workspaceId,
      String modelId,
      String name,
      ServerFileData createFileData);

  ChunksResponse getChunks(
      String workspaceId,
      String modelId,
      String fileId);

  byte[] getChunkContent(
      String workspaceId,
      String modelId,
      String fileId,
      String chunkId);

  ServerFileResponse upsertFileDataSource(
      String workspaceId,
      String modelId,
      String fileId,
      ServerFileData createFileData);

  void uploadChunk(
      String workspaceId,
      String modelId,
      String fileId,
      String chunkId,
      byte[] fileData);

  void uploadChunkCompressed(
      String workspaceId,
      String modelId,
      String fileId,
      String chunkId,
      byte[] fileData);

  ServerFileResponse completeUpload(
      String workspaceId,
      String modelId,
      String fileId,
      ServerFileData serverFileData);

  /* Imports */

  ImportsResponse getImports(
      String workspaceId,
      String modelId,
      int offset);

  TasksResponse getImportTasks(
      String workspaceId,
      String modelId,
      String importId,
      int offset);

  TaskResponse createImportTask(
      String workspaceId,
      String modelId,
      String importId,
      ImportData importData);

  TaskStatusResponse getImportTaskStatus(
      String workspaceId,
      String modelId,
      String importId,
      String taskId);

  TaskStatusResponse cancelImportTask(
      String workspaceId,
      String modelId,
      String importId,
      String taskId);

  ChunksResponse getImportDumpFileChunks(
      String workspaceId,
      String modelId,
      String importId,
      String taskId);

  byte[] getImportDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String importId,
      String taskId,
      String chunkId);

  ChunksResponse getImportNestedDumpFileChunks(
      String workspaceId,
      String modelId,
      String importId,
      String taskId,
      String nestedId);

  byte[] getImportNestedDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String importId,
      String taskId,
      String nestedId,
      String chunkId);

  /* Exports */

  ExportsResponse getExports(
      String workspaceId,
      String modelId,
      int offset);

  ExportMetadataResponse getExport(
      String workspaceId,
      String modelId,
      String exportId);

  TasksResponse getExportTasks(
      String workspaceId,
      String modelId,
      String exportId,
      int offset);

  TaskResponse createExportTask(
      String workspaceId,
      String modelId,
      String exportId,
      ExportData exportData);

  TaskStatusResponse getExportTaskStatus(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId);

  TaskStatusResponse cancelExportTask(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId);

  ChunksResponse getExportDumpFileChunks(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId);

  byte[] getExportDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId,
      String chunkId);

  ChunksResponse getExportNestedDumpFileChunks(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId,
      String nestedId);

  byte[] getExportNestedDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId,
      String nestedId,
      String chunkId);

  /* Actions */

  ActionsResponse getActions(
      String workspaceId,
      String modelId,
      int offset);

  TasksResponse getActionTasks(
      String workspaceId,
      String modelId,
      String actionId,
      int offset);

  TaskResponse createActionTask(
      String workspaceId,
      String modelId,
      String actionId,
      ActionData actionData);

  TaskStatusResponse getActionTaskStatus(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId);

  TaskStatusResponse cancelActionTask(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId);

  ChunksResponse getActionDumpFileChunks(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId);

  byte[] getActionDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId,
      String chunkId);

  ChunksResponse getActionNestedDumpFileChunks(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId,
      String nestedId);

  byte[] getActionNestedDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId,
      String nestedId,
      String chunkId);

  /* Process */

  ProcessesResponse getProcesses(
      String workspaceId,
      String modelId,
      int offset);

  TasksResponse getProcessTasks(
      String workspaceId,
      String modelId,
      String processId,
      int offset);

  TaskResponse createProcessTask(
      String workspaceId,
      String modelId,
      String processId,
      ProcessData processData);

  TaskStatusResponse getProcessTaskStatus(
      String workspaceId,
      String modelId,
      String processId,
      String taskId);

  TaskStatusResponse cancelProcessTask(
      String workspaceId,
      String modelId,
      String processId,
      String taskId);

  ChunksResponse getProcessDumpFileChunks(
      String workspaceId,
      String modelId,
      String processId,
      String taskId);

  byte[] getProcessDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String processId,
      String taskId,
      String chunkId);

  ChunksResponse getProcessNestedDumpFileChunks(
      String workspaceId,
      String modelId,
      String processId,
      String taskId,
      String nestedId);

  byte[] getProcessNestedDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String processId,
      String taskId,
      String nestedId,
      String chunkId);

  /* Lists */

  ListNamesResponse getListNames(
      String workspaceId,
      String modelId,
      int offset);

  ListMetadataResponse getListMetadata(
      String workspaceId,
      String modelId,
      String listId);

  ListItemsResponse getListItems(
      String workspaceId,
      String modelId,
      String listId,
      boolean includeAll);

  String getListItemsCsv(
      String workspaceId,
      String modelId,
      String listId,
      boolean includeAll);

  /* Views */

  ViewsResponse getViews(
      String modelId,
      String moduleId,
      int offset);

  String getViewDataCsv(
      String modelId,
      String viewId,
      String pages);

  String getViewDataJson(
      String modelId,
      String viewId,
      String pages);

  ViewMetadataResponse getViewMetadata(
      String modelId,
      String viewId);

  ViewDimensionMetadataResponse getViewDimensionMetadata(
      String modelId,
      String viewId,
      String dimensionId);

  ItemMetadataResponse getItemId(
      String modelId,
      String dimensionId,
      ItemData items);

  ViewDataResponse getViewData(
      String modelId,
      String viewId,
      String pages);

  ListItemResultData addItemsToList(
      String workspaceId,
      String modelId,
      String listId,
      ListItemParametersData itemData);

  ListItemResultData updateItemsList(
      String workspaceId,
      String modelId,
      String listId,
      ListItemParametersData itemData);

  ListItemResultData deleteItemsList(
      String workspaceId,
      String modelId,
      String listId,
      ListItemParametersData itemData);

  ImportMetaResponse getImportMeta(
      String workspaceId,
      String modelId,
      String importId);
}
