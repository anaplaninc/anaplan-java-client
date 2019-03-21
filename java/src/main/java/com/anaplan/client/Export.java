//   Copyright 2011, 2012 Anaplan Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.anaplan.client;

import com.anaplan.client.dto.ExportData;
import com.anaplan.client.dto.ExportMetadata;
import com.anaplan.client.dto.TaskParametersData;
import com.anaplan.client.dto.responses.ChunksResponse;
import com.anaplan.client.dto.responses.TaskResponse;
import com.anaplan.client.dto.responses.TaskStatusResponse;
import com.anaplan.client.dto.responses.TasksResponse;
import com.anaplan.client.ex.AnaplanAPIException;

/**
 * An export object within an Anaplan model.
 */
public class Export extends TaskFactory {

    Export(Model model, ExportData data) {
        super(model, data);
    }

    /**
     * Get information about the columns in the export.
     *
     * @since 1.2
     */
    public ExportMetadata getExportMetadata() throws AnaplanAPIException {
        return getApi().getExport(getWorkspace().getId(), getModel().getId(), getId()).getItem();
    }

    @Override
    TaskResponse createActionTask(TaskParametersData taskParametersData) {
        return getApi().createExportTask(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                getData().merge(taskParametersData, ExportData.class));
    }

    @Override
    TasksResponse getTasks(int offset) {
        return getApi().getExportTasks(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                offset);
    }

    @Override
    TaskStatusResponse cancelTask(String taskId) {
        return getApi().cancelExportTask(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId);
    }

    @Override
    TaskStatusResponse getTaskStatus(String taskId) {
        return getApi().getExportTaskStatus(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId);
    }

    @Override
    ChunksResponse getDumpFileChunks(String taskId) {
        return getApi().getExportDumpFileChunks(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId);
    }

    @Override
    byte[] getDumpFileChunkContent(String taskId, String chunkId) {
        return getApi().getExportDumpFileChunkContent(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId,
                chunkId);
    }

    @Override
    ChunksResponse getNestedDumpFileChunks(String taskId, String nestedObjectId) {
        return getApi().getExportNestedDumpFileChunks(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId,
                nestedObjectId);
    }

    @Override
    byte[] getNestedDumpFileChunkContent(String taskId, String nestedObjectId, String chunkId) {
        return getApi().getExportNestedDumpFileChunkContent(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId,
                nestedObjectId,
                chunkId);
    }
}
