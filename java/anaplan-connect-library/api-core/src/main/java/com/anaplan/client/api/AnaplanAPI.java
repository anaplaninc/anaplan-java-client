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
  String URL_CHUNK_ID = "/workspaces/{workspaceId}/models/{modelId}/files/{fileId}/chunks/{chunkId}";

  /* Workspaces */

  /**
   * Get all workspaces with offset
   * @param offset the offset
   * @return {@link WorkspacesResponse}
   */
  WorkspacesResponse getWorkspaces(int offset);

  /**
   * Get all workspaces between the limits
   * @param offset the offset
   * @param limit the limit
   * @return {@link WorkspacesResponse}
   */
  WorkspacesResponse getWorkspaces(int offset, int limit);

  /**
   * Find a worskspace with the id
   * @param workspaceId the workspace identity
   * @return {@link WorkspacesResponse}
   */
  WorkspaceResponse getWorkspace(String workspaceId);

  /* Models */

  /**
   * Search all models in a workspace from an offset
   * @param offset the offset
   * @return {@link ModelsResponse}
   */
  ModelsResponse getModels(int offset);

  /**
   * Search for all models between limits
   * @param offset the offset
   * @param limit the limit
   * @return {@link ModelsResponse}
   */
  ModelsResponse getModels(int offset, int limit);

  /**
   * Search for model
   * @param modelId the model id
   * @return {@link ModelResponse}
   */
  ModelResponse getModel(String modelId);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param offset the offset
   * @return {@link ModelResponse}
   */
  ModulesResponse getModules(
      String workspaceId,
      String modelId,
      int offset);

  /* Files */

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param offset the offset
   * @return {@link ServerFilesResponse}
   */
  ServerFilesResponse getServerFiles(
      String workspaceId,
      String modelId,
      int offset);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param name import name
   * @param createFileData {@link ServerFileData}
   * @return {@link ServerFilesResponse}
   */
  ServerFileResponse createImportDataSource(
      String workspaceId,
      String modelId,
      String name,
      ServerFileData createFileData);

  /**
   * Get file chunks id
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param fileId the file id
   * @return {@link ChunksResponse}
   */
  ChunksResponse getChunks(
      String workspaceId,
      String modelId,
      String fileId);

  /**
   * Get file chunks content
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param fileId the file id
   * @param chunkId the chunks id
   * @return {@link byte[]}
   */
  byte[] getChunkContent(
      String workspaceId,
      String modelId,
      String fileId,
      String chunkId);


  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param fileId the file id
   * @param createFileData {@link ServerFileData}
   * @return {@link ServerFileResponse}
   */
  ServerFileResponse upsertFileDataSource(
      String workspaceId,
      String modelId,
      String fileId,
      ServerFileData createFileData);

  /**
   * Upload chunk in file
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param fileId the file id
   * @param chunkId the chunk id
   * @param fileData {@link byte[]}
   */
  void uploadChunk(
      String workspaceId,
      String modelId,
      String fileId,
      String chunkId,
      byte[] fileData);

  /**
   * Upload chunks in file
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param fileId the file id
   * @param chunkId the chunk id
   * @param fileData {@link byte[]}
   */
  void uploadChunkCompressed(
      String workspaceId,
      String modelId,
      String fileId,
      String chunkId,
      byte[] fileData);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param fileId the file id
   * @param serverFileData {@link ServerFileData}
   * @return {@link ServerFileResponse}
   */
  ServerFileResponse completeUpload(
      String workspaceId,
      String modelId,
      String fileId,
      ServerFileData serverFileData);

  /* Imports */

  /**
   * Search for imports
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param offset the offset
   * @return {@link ImportsResponse}
   */
  ImportsResponse getImports(
      String workspaceId,
      String modelId,
      int offset);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param importId the import id
   * @param offset the offset
   * @return {@link TasksResponse}
   */
  TasksResponse getImportTasks(
      String workspaceId,
      String modelId,
      String importId,
      int offset);

  /**
   * Create import task
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param importId the import id
   * @param importData {@link ImportData}
   * @return {@link TaskResponse}
   */
  TaskResponse createImportTask(
      String workspaceId,
      String modelId,
      String importId,
      ImportData importData);

  /**
   * Get import status
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param importId the import id
   * @param taskId the task id
   * @return {@link TaskStatusResponse}
   */
  TaskStatusResponse getImportTaskStatus(
      String workspaceId,
      String modelId,
      String importId,
      String taskId);

  /**
   * Cancel import
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param importId the import id
   * @param taskId the task id
   * @return {@link TaskStatusResponse}
   */
  TaskStatusResponse cancelImportTask(
      String workspaceId,
      String modelId,
      String importId,
      String taskId);

  /**
   *  Get import dump file chunks
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param importId the import id
   * @param taskId the task id
   * @return {@link ChunksResponse}
   */
  ChunksResponse getImportDumpFileChunks(
      String workspaceId,
      String modelId,
      String importId,
      String taskId);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param importId the import id
   * @param taskId the task id
   * @param chunkId the chunk id
   * @return {@link byte[]}
   */
  byte[] getImportDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String importId,
      String taskId,
      String chunkId);

  /**
   * Get import nested dump file chunks
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param importId the import id
   * @param taskId the task id
   * @param nestedId the nested import id
   * @return {@link ChunksResponse}
   */
  ChunksResponse getImportNestedDumpFileChunks(
      String workspaceId,
      String modelId,
      String importId,
      String taskId,
      String nestedId);

  /**
   * Get import nested dump file chuns content
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param importId the import id
   * @param taskId the task id
   * @param nestedId the nested import id
   * @param chunkId the chunk id
   * @return {@link byte[]}
   */
  byte[] getImportNestedDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String importId,
      String taskId,
      String nestedId,
      String chunkId);

  /* Exports */

  /**
   * Get exports items from model
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param offset the offset
   * @return {@link ExportsResponse}
   */
  ExportsResponse getExports(
      String workspaceId,
      String modelId,
      int offset);

  /**
   * Get export from model
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param exportId the export id
   * @return {@link ExportMetadataResponse}
   */
  ExportMetadataResponse getExport(
      String workspaceId,
      String modelId,
      String exportId);

  /**
   * Get export from model from limit
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param exportId the export id
   * @param offset the offset
   * @return {@link TasksResponse}
   */
  TasksResponse getExportTasks(
      String workspaceId,
      String modelId,
      String exportId,
      int offset);

  /**
   * Create export task
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param exportId the export id
   * @param exportData {@link ExportData}
   * @return {@link TaskResponse}
   */
  TaskResponse createExportTask(
      String workspaceId,
      String modelId,
      String exportId,
      ExportData exportData);

  /**
   * Get export task status
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param exportId the export id
   * @param taskId the task id
   * @return {@link TaskResponse}
   */
  TaskStatusResponse getExportTaskStatus(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId);

  /**
   * Cancel export
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param exportId the export id
   * @param taskId the task id
   * @return {@link TaskStatusResponse}
   */
  TaskStatusResponse cancelExportTask(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId);

  /**
   * Export Dump file
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param exportId the export id
   * @param taskId the task id
   * @return {@link ChunksResponse}
   */
  ChunksResponse getExportDumpFileChunks(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId);

  /**
   * Get export dump chunk content
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param exportId the export id
   * @param taskId the task id
   * @param chunkId the chunk id
   * @return {@link byte[]}
   */
  byte[] getExportDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId,
      String chunkId);

  /**
   * Get export nested file chunks
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param exportId the export id
   * @param taskId the task id
   * @param nestedId the nested task id
   * @return {@link ChunksResponse}
   */
  ChunksResponse getExportNestedDumpFileChunks(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId,
      String nestedId);

  /**
   * Get export dump file content
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param exportId the export id
   * @param taskId the task id
   * @param nestedId the nested task id
   * @param chunkId the chunk id
   * @return the content in bytes
   */
  byte[] getExportNestedDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String exportId,
      String taskId,
      String nestedId,
      String chunkId);

  /* Actions */

  /**
   * Get actions from model
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param offset the offset
   * @return {@link ActionsResponse}
   */
  ActionsResponse getActions(
      String workspaceId,
      String modelId,
      int offset);

  /**
   * Get action tasks
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param actionId the action id
   * @param offset the offset
   * @return {@link TasksResponse}
   */
  TasksResponse getActionTasks(
      String workspaceId,
      String modelId,
      String actionId,
      int offset);

  /**
   * Create action task
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param actionId the action id
   * @param actionData {@link ActionData}
   * @return {@link TaskResponse}
   */
  TaskResponse createActionTask(
      String workspaceId,
      String modelId,
      String actionId,
      ActionData actionData);

  /**
   * Get Action task status
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param actionId the action id
   * @param taskId the task id
   * @return {@link TaskStatusResponse}
   */
  TaskStatusResponse getActionTaskStatus(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId);

  /**
   * Cancel action task
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param actionId the action id
   * @param taskId the task id
   * @return {@link TaskStatusResponse}
   */
  TaskStatusResponse cancelActionTask(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param actionId the action id
   * @param taskId the task id
   * @return {@link ChunksResponse}
   */
  ChunksResponse getActionDumpFileChunks(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId);

  /**
   * Get Action file chunk content
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param actionId the action id
   * @param taskId the task id
   * @param chunkId the chunk id
   * @return file content in bytes
   */
  byte[] getActionDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId,
      String chunkId);

  /**
   * Get action dump file content
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param actionId the action id
   * @param taskId the task id
   * @param nestedId the nested task id
   * @return {@link ChunksResponse}
   */
  ChunksResponse getActionNestedDumpFileChunks(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId,
      String nestedId);

  /**
   * Get action nested file content
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param actionId the action id
   * @param taskId the task id
   * @param nestedId the nested task id
   * @param chunkId the chunk id
   * @return the content in bytes
   */
  byte[] getActionNestedDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String actionId,
      String taskId,
      String nestedId,
      String chunkId);

  /* Process */

  /**
   * Get procesess from model
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param offset the offset
   * @return {@link ProcessesResponse}
   */
  ProcessesResponse getProcesses(
      String workspaceId,
      String modelId,
      int offset);

  /**
   * Get process
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param processId the process id
   * @param offset the offset
   * @return {@link TasksResponse}
   */
  TasksResponse getProcessTasks(
      String workspaceId,
      String modelId,
      String processId,
      int offset);

  /**
   * Create task
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param processId the processId
   * @param processData {@link ProcessData}
   * @return {@link TaskResponse}
   */
  TaskResponse createProcessTask(
      String workspaceId,
      String modelId,
      String processId,
      ProcessData processData);

  /**
   * Return process status
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param processId the process id
   * @param taskId the task id
   * @return {@link TaskStatusResponse}
   */
  TaskStatusResponse getProcessTaskStatus(
      String workspaceId,
      String modelId,
      String processId,
      String taskId);

  /**
   * Cancel process task
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param processId the process id
   * @param taskId the task id
   * @return {@link TaskStatusResponse}
   */
  TaskStatusResponse cancelProcessTask(
      String workspaceId,
      String modelId,
      String processId,
      String taskId);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param processId the process id
   * @param taskId the task id
   * @return {@link ChunksResponse}
   */
  ChunksResponse getProcessDumpFileChunks(
      String workspaceId,
      String modelId,
      String processId,
      String taskId);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param processId the process id
   * @param taskId the task id
   * @param chunkId the chunk id
   * @return content in bytes
   */
  byte[] getProcessDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String processId,
      String taskId,
      String chunkId);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param processId the process id
   * @param taskId the task id
   * @param nestedId the proess nested id
   * @return {@link ChunksResponse}
   */
  ChunksResponse getProcessNestedDumpFileChunks(
      String workspaceId,
      String modelId,
      String processId,
      String taskId,
      String nestedId);

  /**
   *
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param processId the process id
   * @param taskId the task id
   * @param nestedId the proess nested id
   * @param chunkId the chunk id
   * @return the content in bytes
   */
  byte[] getProcessNestedDumpFileChunkContent(
      String workspaceId,
      String modelId,
      String processId,
      String taskId,
      String nestedId,
      String chunkId);

  /* Lists */

  /**
   * Return list names in model
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param offset the offset
   * @return {@link ListNamesResponse}
   */
  ListNamesResponse getListNames(
      String workspaceId,
      String modelId,
      int offset);

  /**
   * Get list metadata
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param listId the list id
   * @return {@link ListMetadataResponse}
   */
  ListMetadataResponse getListMetadata(
      String workspaceId,
      String modelId,
      String listId);

  /**
   * Return items list
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param listId the list id
   * @param includeAll includeAll parameter
   * @return {@link ListItemsResponse}
   */
  ListItemsResponse getListItems(
      String workspaceId,
      String modelId,
      String listId,
      boolean includeAll);

  /**
   * Return item list in csv format
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param listId the list id
   * @param includeAll includeAll parameter
   * @return the items in csv format
   */
  String getListItemsCsv(
      String workspaceId,
      String modelId,
      String listId,
      boolean includeAll);

  /* Views */

  /**
   * Get views from model
   * @param modelId the model id
   * @param moduleId the module id
   * @param offset the offset
   * @return {@link ViewsResponse}
   */
  ViewsResponse getViews(
      String modelId,
      String moduleId,
      int offset);

  /**
   * Return view in csv format
   * @param modelId the model id
   * @param viewId the view id
   * @param pages the pages
   * @return data result
   */
  String getViewDataCsv(
      String modelId,
      String viewId,
      String pages);

  /**
   * Get view data in json format
   * @param modelId the model id
   * @param viewId the view id
   * @param pages the pages
   * @return data in json format
   */
  String getViewDataJson(
      String modelId,
      String viewId,
      String pages);

  /**
   * Get view metadata
   * @param modelId the model id
   * @param viewId the view id
   * @return {@link ViewMetadataResponse}
   */
  ViewMetadataResponse getViewMetadata(
      String modelId,
      String viewId);

  /**
   * Return view dimension metadata
   * @param modelId the model id
   * @param viewId the view id
   * @param dimensionId the dimension id
   * @return {@link ViewDimensionMetadataResponse}
   */
  ViewDimensionMetadataResponse getViewDimensionMetadata(
      String modelId,
      String viewId,
      String dimensionId);

  /**
   * Return item identifier
   * @param modelId the model id
   * @param dimensionId the dimensionId
   * @param items the items data
   * @return {@link ItemMetadataResponse}
   */
  ItemMetadataResponse getItemId(
      String modelId,
      String dimensionId,
      ItemData items);

  /**
   * Get view metadata
   * @param modelId the model id
   * @param viewId the view id
   * @param pages the pages
   * @return {@link ViewDataResponse}
   */
  ViewDataResponse getViewData(
      String modelId,
      String viewId,
      String pages);

  /**
   * Ad items to list
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param listId the list id
   * @param itemData {@link ListItemResultData}
   * @return {@link ListItemParametersData}
   */
  ListItemResultData addItemsToList(
      String workspaceId,
      String modelId,
      String listId,
      ListItemParametersData itemData);

  /**
   * Update items list
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @param listId the list id
   * @param itemData {@link ListItemParametersData}
   * @return {@link ListItemResultData}
   */
  ListItemResultData updateItemsList(
      String workspaceId,
      String modelId,
      String listId,
      ListItemParametersData itemData);

  /**
   * Remove items list
   * @param workspaceId the workspace identifier
   * @param modelId the mode identifier
   * @param listId the list identifier
   * @param itemData {@link ListItemParametersData}
   * @return {@link ListItemResultData}
   */
  ListItemResultData deleteItemsList(
      String workspaceId,
      String modelId,
      String listId,
      ListItemParametersData itemData);

  /**
   * Return import metadata
   * @param workspaceId the workspace identifier
   * @param modelId the model identifier
   * @param importId the import identifier
   * @return {@link ImportMetaResponse}
   */
  ImportMetaResponse getImportMeta(
      String workspaceId,
      String modelId,
      String importId);
}
