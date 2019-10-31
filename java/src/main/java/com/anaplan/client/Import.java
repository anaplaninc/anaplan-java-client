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

import com.anaplan.client.dto.ImportData;
import com.anaplan.client.dto.TaskParametersData;
import com.anaplan.client.dto.responses.ChunksResponse;
import com.anaplan.client.dto.responses.TaskResponse;
import com.anaplan.client.dto.responses.TaskStatusResponse;
import com.anaplan.client.dto.responses.TasksResponse;
import com.anaplan.client.ex.AnaplanAPIException;

/**
 * An import object within an Anaplan model.
 */
public class Import extends TaskFactory {

    /**
     * The set of types of import definition.
     * This enumerates possible import types available at the release of this
     * version of Anaplan Connect. It may be the case that other types have
     * added to Anaplan in the intervening period. Clients should be aware of,
     * and handle, this potential condition gracefully.
     */
    public enum ImportType {
        /**
         * An import into a list.
         */
        HIERARCHY_DATA("List data"),
        /**
         * An import into a module (data) view.
         */
        MODULE_DATA("Module data"),
        /**
         * An import into a module blueprint view.
         */
        LINE_ITEM_DEFINITION("Module blueprint"),
        /**
         * An import into the Users view.
         */
        USERS("Users"),
        /**
         * An import into the Versions view.
         */
        VERSIONS("Versions");
        private String description;

        ImportType(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    Import(Model model, ImportData data) {
        super(model, data);
    }

    @Override
    TaskResponse createActionTask(TaskParametersData taskParametersData) {
        return getApi().createImportTask(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                getData().merge(taskParametersData, ImportData.class));
    }

    @Override
    TasksResponse getTasks(int offset) {
        return getApi().getImportTasks(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                offset);
    }

    @Override
    TaskStatusResponse cancelTask(String taskId) {
        return getApi().cancelImportTask(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId);
    }

    @Override
    TaskStatusResponse getTaskStatus(String taskId) {
        return getApi().getImportTaskStatus(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId);
    }

    @Override
    ChunksResponse getDumpFileChunks(String taskId) {
        return getApi().getImportDumpFileChunks(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId);
    }

    @Override
    byte[] getDumpFileChunkContent(String taskId, String chunkId) {
        return getApi().getImportDumpFileChunkContent(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId,
                chunkId);
    }

    @Override
    ChunksResponse getNestedDumpFileChunks(String taskId, String nestedObjectId) {
        return getApi().getImportNestedDumpFileChunks(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId,
                nestedObjectId);
    }

    @Override
    byte[] getNestedDumpFileChunkContent(String taskId, String nestedObjectId, String chunkId) {
        return getApi().getImportNestedDumpFileChunkContent(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId,
                nestedObjectId,
                chunkId);
    }

    /**
     * Get the ID of the data source associated with the import, if it is an uploaded file.
     *
     * @return the ID for the data source if it is a file; otherwise the empty
     * string ("") is returned.
     * @since 1.2
     */
    public String getSourceFileId() throws AnaplanAPIException {
        return ((ImportData) getData()).getImportDataSourceId();
    }

    /**
     * Get the type of import.
     *
     * @return The import type; null if the type is not recognized by this
     * version of Anaplan Connect.
     * @since 1.3
     */
    public ImportType getImportType() {
        try {
            return ImportType.valueOf(((ImportData) getData()).getImportType());
        } catch (Exception e) {
            return null;
        }
    }
}
