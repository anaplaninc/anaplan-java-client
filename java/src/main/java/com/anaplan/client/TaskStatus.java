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

import com.anaplan.client.dto.TaskStatusData;

/**
 * The status of a task on the server.
 */
public class TaskStatus {

    /**
     * Represents the current state of a task on the server.
     */
    public enum State {
        /**
         * The task is scheduled but has not started yet.
         */
        NOT_STARTED("Waiting to start"),
        /**
         * The task has started and is in progress.
         */
        IN_PROGRESS("In Progress"),
        /**
         * The task has completed, either successfully or unsuccessfully.
         */
        COMPLETE("Complete"),
        /**
         * The task has been cancelled, and is in the process of rolling back any changes made
         */
        CANCELLING("Cancelling"),
        /**
         * The task has been cancelled, and the process of rolling back any changes made has completed
         */
        CANCELLED("Cancelled");

        private final String description;

        State(String description) {
            this.description = description;
        }

        public String getValue() {
            return description;
        }
    }

    private Task task;

    private TaskStatusData data;

    private TaskResult result;

    TaskStatus(Task task, TaskStatusData data) {
        this.task = task;
        this.data = data;
        if (null != data.getResult()) {
            String dumpFileId = task.getId();
            result = new TaskResult(task, dumpFileId, data.getResult());
        }
    }

    Task getTask() {
        return task;
    }

    /**
     * Get the task's state.
     *
     * @return The state of the task
     */
    public State getTaskState() {
        return data.getTaskState();
    }

    /**
     * Get a description of current activity.
     *
     * @return The activity currently being performed by the server.
     */
    public String getCurrentStep() {
        return data.getCurrentStep();
    }

    /**
     * Get an estimate of task completion.
     * This can be converted to a percentage by multiplying by 100.
     *
     * @return The degree of completion for the task
     * <p>
     * 0.0 = not started
     * <p>
     * 1.0 = complete
     */
    public double getProgress() {
        return data.getProgress();
    }

    /**
     * Get the result of a completed task.
     *
     * @return the result of the task if completed; otherwise null
     */
    public TaskResult getResult() {
        return result;
    }

    /**
     * Get the name of the user who cancelled the task, if different from
     * the user associated with this task.
     *
     * @return the full name of the cancelling user if applicable; null otherwise.
     * @since 1.3.1
     */
    public String getCancelledBy() {
        return data.getCancelledBy();
    }
}
