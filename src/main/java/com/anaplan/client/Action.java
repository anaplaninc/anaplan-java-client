//   Copyright 2011 Anaplan Inc.
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

import com.anaplan.client.dto.ActionData;
import com.anaplan.client.dto.NamedObjectData;
import com.anaplan.client.dto.TaskParametersData;
import com.anaplan.client.dto.responses.ChunksResponse;
import com.anaplan.client.dto.responses.TaskResponse;
import com.anaplan.client.dto.responses.TaskStatusResponse;
import com.anaplan.client.dto.responses.TasksResponse;

/**
 * An action object within an Anaplan model.
 *
 * @since 1.1
 */
public class Action extends TaskFactory {

    Action(Model model, NamedObjectData data) {
        super(model, data);
    }

    @Override
    TaskResponse createActionTask(TaskParametersData taskParametersData) {
        return getApi().createActionTask(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                getData().merge(taskParametersData, ActionData.class));
    }

    @Override
    TasksResponse getTasks(int offset) {
        return getApi().getActionTasks(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                offset);
    }

    @Override
    TaskStatusResponse cancelTask(String taskId) {
        return getApi().cancelActionTask(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId);
    }

    @Override
    TaskStatusResponse getTaskStatus(String taskId) {
        return getApi().getActionTaskStatus(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId);
    }

    @Override
    ChunksResponse getDumpFileChunks(String taskId) {
        return getApi().getActionDumpFileChunks(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId);
    }

    @Override
    byte[] getDumpFileChunkContent(String taskId, String chunkId) {
        return getApi().getActionDumpFileChunkContent(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId,
                chunkId);
    }

    @Override
    ChunksResponse getNestedDumpFileChunks(String taskId, String nestedObjectId) {
        return getApi().getActionNestedDumpFileChunks(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId,
                nestedObjectId);
    }

    @Override
    byte[] getNestedDumpFileChunkContent(String taskId, String nestedObjectId, String chunkId) {
        return getApi().getActionNestedDumpFileChunkContent(
                getWorkspace().getId(),
                getModel().getId(),
                getId(),
                taskId,
                nestedObjectId,
                chunkId);
    }
}
