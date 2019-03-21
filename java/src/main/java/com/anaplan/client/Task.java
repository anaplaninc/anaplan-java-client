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

import com.anaplan.client.dto.TaskData;
import com.anaplan.client.dto.responses.TaskStatusResponse;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.InvalidTaskStatusError;
import com.anaplan.client.logging.LogUtils;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * A task for processing on the server.
 */
public class Task extends AnaplanApiClientObject {

    private static final Logger LOG = LoggerFactory.getLogger(Task.class);
    private static Thread runningThread;
    private static boolean closingDown = false;
    private static Task runningTask;
    private TaskFactory subject;
    private TaskData data;

    static {
        try {
            Thread cancelThread = new Thread(() -> {
                closingDown = true;
                Thread runner = runningThread;
                if (runner != null) {
                    try {
                        runner.interrupt();
                    } catch (Throwable thrown) {
                        LOG.debug("{}", thrown);
                    }
                }
                cancelRunningTask(runningTask);
            });
            cancelThread.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(cancelThread);
        } catch (Throwable thrown) {
            thrown.printStackTrace();
        }
    }

    Task(TaskFactory subject, TaskData data) {
        super(subject.getModel().getWorkspace().getService());
        this.subject = subject;
        this.data = data;
    }

    /**
     * Fetches the running task if any
     * @return
     */
    public static Task getRunningTask() {
        return runningTask;
    }

    TaskFactory getSubject() {
        return subject;
    }

    String getId() {
        return data.getTaskId();
    }

    /**
     * Send a request to the server to cancel the task.
     * This is not guaranteed to stop a task; a task can only be stopped if it
     * can roll back the changes it made to leave the model in a consistent state.
     *
     * @return The current status of the task
     */
    public TaskStatus cancel() throws AnaplanAPIException {
        TaskStatusResponse response = subject.cancelTask(data.getTaskId());
        if (response != null && response.getItem() != null) {
            return new TaskStatus(this, response.getItem());
        }
        throw new InvalidTaskStatusError(data.getTaskId(), response);
    }

    /**
     * Get the current status of the task.
     *
     * @return The current status of the task
     */
    public TaskStatus getStatus() throws AnaplanAPIException {
        TaskStatusResponse response = subject.getTaskStatus(data.getTaskId());
        if (response != null && response.getItem() != null) {
            return new TaskStatus(this, response.getItem());
        }
        throw new InvalidTaskStatusError(data.getTaskId(), response);
    }

    /**
     * Track the progress of a task on the server until completion. If
     * run from a command line (ie <tt>System.console() != null</tt>) the
     * progress will be displayed on the controlling terminal.
     *
     * @return the result following completion of the task; null otherwise
     */
    public synchronized TaskResult runTask() throws AnaplanAPIException, InterruptedException {
        runningThread = Thread.currentThread();
        runningTask = this;
        TaskResult result = trackRunningTask(runningTask, false);
        runningTask = null;
        runningThread = null;
        return result;
    }

    /**
     * Cancels the running task, called only when the client is terminated.
     */
    private static synchronized void cancelRunningTask(Task runningTask) {
        if (runningTask != null) {
            try {
                if (System.console() != null)
                    System.console().printf("\rClient terminated, cancelling...");
                runningTask.cancel();
                trackRunningTask(runningTask, true);
            } catch (Throwable thrown) {
                LOG.debug("{}", Throwables.getStackTraceAsString(thrown));
                LOG.error(Utils.formatThrowable(thrown));
            } finally {
                System.exit(1);
                try {
                    Thread.currentThread().join();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Could not cancel running task!", e);
                }
            }
        }
    }

    /**
     * Thread safe method to run the task and keep checking the run-status intermittently.
     *
     * @param wasClosingDown
     * @return
     * @throws AnaplanAPIException
     * @throws InterruptedException
     */
    private static synchronized TaskResult trackRunningTask(Task runningTask, boolean wasClosingDown) throws AnaplanAPIException, InterruptedException {
        TaskStatus status = null;
        int interval = 1000;
        int totalTime = 0;
        int failCount = 0;
        try {
            do {
                if (!wasClosingDown && closingDown) {
                    throw new InterruptedException();
                }
                Thread.sleep(interval);
                totalTime += interval;

                if (wasClosingDown) {
                    interval = 500;
                } else if (totalTime > 60000) {
                    interval = 60000;
                } else if (totalTime > 10000) {
                    interval = 10000;
                } else {
                    interval = 1000;
                }
                try {
                    status = runningTask.getStatus();
                    failCount = 0;
                } catch (AnaplanAPIException thrown) {
                    status = null;
                    // Allow up to 30 attempts before giving up.
                    if (++failCount > 30) {
                        throw new AnaplanAPIException(
                                "Task was started, but server cannot be reached"
                                        + " - giving up after 30 attempts", thrown);
                    }
                    LOG.debug("Failed to get status ({}); retrying in {}s\n", Utils.formatThrowable(thrown), interval / 1000);
                    LOG.info("Checking in {}s", interval / 1000);
                }
                if (status != null) {
                    StringBuilder message = new StringBuilder();
                    if (status.getCurrentStep() != null) {
                        message.append(status.getCurrentStep());
                    } else if (status.getTaskState() == null) {
                        message.append(status.getTaskState().toString());
                    } else {
                        message.append(status.getTaskState().getValue());
                    }
                    if (status.getProgress() > 0) {
                        message.append(" (").append(Math.floor(status.getProgress() * 1000) / 10).append("%)");
                    }
                    LOG.info("Run status: {}", message.toString());
                }
            } while (!(wasClosingDown && totalTime > 1000) && (status == null || !(
                    status.getTaskState() == TaskStatus.State.COMPLETE ||
                    status.getTaskState() == TaskStatus.State.CANCELLED)));
        } finally {
            if (status == null || status.getResult() == null) {
                LOG.info("No result was provided.");
            } else if (status.getTaskState() == TaskStatus.State.CANCELLED) {
                StringBuilder message = new StringBuilder();
                message.append("The operation was cancelled");
                if (status.getCancelledBy() != null) {
                    message.append(" by ").append(status.getCancelledBy());
                }
                if (status.getResult() != null) {
                    message.append("; some actions may have completed.");
                } else {
                    message.append(".");
                }
                LOG.info(message.toString());
            } else {
                LogUtils.logSeparatorOperationResponses();
                if (status.getResult() != null) {
                    LOG.info(status.getResult().isSuccessful() ?
                            "<<< The operation was successful >>>  =)" :
                            "!!! The operation failed !!!  =(");
                }
                LogUtils.logSeparatorOperationStatus();
                Arrays.asList(status.getResult().toString().split("\n")).forEach(LOG::info);
            }
        }
        return (status != null) ? status.getResult() : null;
    }
}
