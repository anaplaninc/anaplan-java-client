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

import java.util.ArrayList;
import java.util.List;

/**
 * A named entity in an Anaplan model that can initiate tasks on the server.
 */
public abstract class TaskFactory extends NamedObject {

    protected TaskFactory(Model model, Data data) {
        super(model, data);
    }

    /**
     * Create a new task on the server.
     * 
     * @return the newly-created task
     */
    public Task createTask() throws AnaplanAPIException {
        return createTask(new TaskParameters());
    }

    /**
     * Create a new task on the server.
     * 
     * @param taskParameters
     *            the run-time parameters to pass to the task
     * @return the newly-created task
     * @since 1.3
     */
    public Task createTask(TaskParameters taskParameters)
            throws AnaplanAPIException {
        byte[] content = getSerializationHandler().serialize(
                taskParameters.data, TaskParameters.DATA_TYPE);
        String contentType = getSerializationHandler().getContentType();
        content = getTransportProvider().post(getPath() + "/tasks", content,
                contentType, contentType);
        Task.Data data = getSerializationHandler().deserialize(content,
                Task.DATA_TYPE);
        return new Task(this, data);
    }

    /**
     * Get a list of all server tasks associated with this object.
     * 
     * @return the list of server tasks
     */
    public List<Task> getTasks() throws AnaplanAPIException {
        List<Task.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/tasks",
                        getSerializationHandler().getContentType()),
                Task.DATA_LIST_TYPE);
        List<Task> result = new ArrayList<Task>(response.size());
        for (Task.Data taskData : response) {
            Task task = new Task(this, taskData);
            result.add(task);
        }
        return result;
    }
}
