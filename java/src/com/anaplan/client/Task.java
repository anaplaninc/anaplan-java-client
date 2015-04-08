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

import java.util.List;

import com.anaplan.client.serialization.TypeWrapper;

/**
 * A task for processing on the server.
 */
public class Task extends AnaplanApiClientObject {

    static class Data {
        String taskId;
    }

    static final TypeWrapper<Data> DATA_TYPE = new TypeWrapper<Data>(){};
    static final TypeWrapper<List<Data>> DATA_LIST_TYPE
            = new TypeWrapper<List<Data>>(){};

    private NamedObject subject;

    private Data data;

    Task(NamedObject subject, Data data) {
        super(subject.getModel().getWorkspace().getService());
        this.subject = subject;
        this.data = data;
    }

    @Override
    String getPath() {
        return subject.getPath() + "/tasks/" + data.taskId;
    }

    NamedObject getSubject() {
        return subject;
    }

    String getId() {
        return data.taskId;
    }

    /**
     * Send a request to the server to cancel the task.
     * This is not guaranteed to stop a task; a task can only be stopped if it
     * can roll back the changes it made to leave the model in a consistent state.
     * @return The current status of the task
     */
    public TaskStatus cancel() throws AnaplanAPIException {
        TaskStatus.Data response = getSerializationHandler().deserialize(
                getTransportProvider().delete(getPath(),
                    getSerializationHandler().getContentType()),
                TaskStatus.DATA_TYPE);
        return new TaskStatus(this, response);
    }

    /**
     * Get the current status of the task.
     * @return The current status of the task
     */
    public TaskStatus getStatus() throws AnaplanAPIException {
        byte[] content = getTransportProvider().get(getPath(),
                    getSerializationHandler().getContentType());
        TaskStatus.Data taskStatusData = getSerializationHandler().deserialize(
                content, TaskStatus.DATA_TYPE);
        return new TaskStatus(this, taskStatusData);
    }

}
