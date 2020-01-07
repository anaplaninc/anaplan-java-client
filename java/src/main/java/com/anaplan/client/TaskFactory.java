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

import com.anaplan.client.dto.NamedObjectData;
import com.anaplan.client.dto.TaskParametersData;
import com.anaplan.client.dto.responses.ChunksResponse;
import com.anaplan.client.dto.responses.TaskResponse;
import com.anaplan.client.dto.responses.TaskStatusResponse;
import com.anaplan.client.dto.responses.TasksResponse;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.CreateTaskError;
import com.anaplan.client.logging.LogUtils;
import com.anaplan.client.transport.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A named entity in an Anaplan model that can initiate tasks on the server.
 */
public abstract class TaskFactory extends NamedObject {

    private static final Logger LOG = LoggerFactory.getLogger(TaskFactory.class);

    protected TaskFactory(Model model, NamedObjectData data) {
        super(model, data);
    }

    /**
     * Creates a task for an Import, Export, Process or Delete action
     *
     * @param taskParametersData
     * @return
     */
    abstract TaskResponse createActionTask(TaskParametersData taskParametersData);

    /**
     * Fetches list of tasks for the action specified by ID.
     *
     * @param offset
     * @return
     */
    abstract TasksResponse getTasks(int offset);

    /**
     * Cancels the running task with the task-ID
     *
     * @param taskId
     * @return
     */
    abstract TaskStatusResponse cancelTask(String taskId);

    /**
     * Fetches the current task status on the server
     *
     * @param taskId
     * @return
     */
    abstract TaskStatusResponse getTaskStatus(String taskId);

    /**
     * Fetches the list of Dump-file chunks for running the action
     *
     * @param taskId
     * @return
     */
    abstract ChunksResponse getDumpFileChunks(String taskId);

    /**
     * Fetches the dump file chunk content in bytes for the specified chunk-ID
     *
     * @param taskId
     * @param chunkId
     * @return
     */
    abstract byte[] getDumpFileChunkContent(String taskId, String chunkId);

    /**
     * Fetches the list of nested dump-file chunks, if any
     *
     * @param taskId
     * @param nestedObjectId
     * @return
     */
    abstract ChunksResponse getNestedDumpFileChunks(String taskId, String nestedObjectId);

    /**
     * Fetches the nested dump-file chunk content
     *
     * @param taskId
     * @param nestedObjectId
     * @param chunkId
     * @return
     */
    abstract byte[] getNestedDumpFileChunkContent(String taskId, String nestedObjectId, String chunkId);

    /**
     * Create a new task on the server.
     *
     * @param taskParameters the run-time parameters to pass to the task
     * @return the newly-created task
     * @since 1.3
     */
    public Task createTask(TaskParameters taskParameters) throws AnaplanAPIException {
        LogUtils.logSeparatorRunAction();
        LOG.info("Running {}: {} (id={})", this.getClass().getSimpleName(), getName(), getId());
        TaskResponse newTask = createActionTask(taskParameters.data);
        if (newTask != null && newTask.getItem() != null) {
            return new Task(this, newTask.getItem());
        }
        throw new CreateTaskError(getId());
    }

    /**
     * Get a list of all server tasks associated with this object.
     *
     * @return the list of server tasks
     */
    public Iterable<Task> getTasks() throws AnaplanAPIException {
        TaskFactory self = this;
        return new Paginator<Task>() {

            @Override
            public Task[] getPage(int offset) {
                TasksResponse response = getTasks(offset);
                setPageInfo(response.getMeta().getPaging());
                if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                    return response.getItem()
                            .stream()
                            .map(taskData -> new Task(self, taskData))
                            .toArray(Task[]::new);
                } else {
                    return new Task[]{};
                }
            }
        };
    }
}
