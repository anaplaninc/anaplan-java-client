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

import com.anaplan.client.serialization.TypeWrapper;

/**
 * The status of a task on the server.
 */
public class TaskStatus {

    /**
      * Represents the current state of a task on the server.
      */
    public static enum State {
        /** The task is scheduled but has not started yet.*/
        NOT_STARTED,
        /** The task has started and is in progress.*/
        IN_PROGRESS,
        /** The task has completed, either successfully or unsuccessfully.*/
        COMPLETE,
        /** The task has been cancelled, and is in the process of rolling back any changes made */
        CANCELLING,
        /** The task has been cancelled, and the process of rolling back any changes made has completed */
        CANCELLED;
    }

    // Data passed over the wire from the server
    static final class Data {
        String taskId;
        double progress;
        String currentStep;
        State taskState;
        TaskResult.Data result;
        String cancelledBy;
    }

    static final TypeWrapper<Data> DATA_TYPE = new TypeWrapper<Data>(){};

    private Task task;

    private Data data;

    private TaskResult result;

    TaskStatus(Task task, Data data) {
        this.task = task;
        this.data = data;
        if (null != data.result) {
            Model model = task.getSubject().getModel();
            String dumpFileId = task.getId();
            String dumpFilePath = task.getPath() + "/dump";
            result = new TaskResult(model, dumpFileId, dumpFilePath, data.result);
        }
    }

    Task getTask() {
        return task;
    }

    /**
      * Get the task's state.
      * @return The state of the task
      */
    public State getTaskState() {
        return data.taskState;
    }

    /**
      * Get a description of current activity.
      * @return The activity currently being performed by the server.
      */
    public String getCurrentStep() {
        return data.currentStep;
    }

    /**
      * Get an estimate of task completion.
      * This can be converted to a percentage by multiplying by 100.
      * @return The degree of completion for the task
      *         <p>
      *         0.0 = not started
      *         <p>
      *         1.0 = complete
      */
    public double getProgress() {
        return data.progress;
    }

    /**
      * Get the result of a completed task.
      * @return the result of the task if completed; otherwise null
      */
    public TaskResult getResult() {
        return result;
    }

    /**
      * Get the name of the user who cancelled the task, if different from
      * the user associated with this task.
      * @return the full name of the cancelling user if applicable; null otherwise.
      * @since 1.3.1
      */
    public String getCancelledBy() {
        return data.cancelledBy;
    }
}
