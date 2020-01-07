//   Copyright 2012 Anaplan Inc.
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

import com.anaplan.client.dto.ChunkData;
import com.anaplan.client.dto.ServerFileData;
import com.anaplan.client.dto.TaskResultData;
import com.anaplan.client.dto.TaskResultDetailData;
import com.anaplan.client.ex.AnaplanAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of a task completing.
 */

public class TaskResult {

    private static final Logger LOG = LoggerFactory.getLogger(TaskResult.class);
    private Task task;
    private String dumpFileId;
    private TaskResultData data;
    private List<TaskResultDetail> details;
    private List<TaskResult> nestedResults;
    private boolean isNestedResult;

    TaskResult(Task task, String dumpFileId, TaskResultData data) {
        this(task, dumpFileId, data, false);
    }

    TaskResult(Task task, String dumpFileId, TaskResultData data, boolean isNestedResult) {
        this.task = task;
        this.dumpFileId = dumpFileId;
        this.data = data;
        this.isNestedResult = isNestedResult;
        List<TaskResultDetail> details = new ArrayList<>();
        if (null != data.getDetails()) {
            for (TaskResultDetailData detailData : data.getDetails()) {
                details.add(new TaskResultDetail(detailData));
            }
        }
        this.details = Collections.unmodifiableList(details);
        List<TaskResult> nestedResults = new ArrayList<>();
        if (null != data.getNestedResults()) {
            for (TaskResultData nestedData : data.getNestedResults()) {
                nestedResults.add(new TaskResult(task, nestedData.getObjectId(), nestedData, true));
            }
        }
        this.nestedResults = Collections.unmodifiableList(nestedResults);
    }

    /**
     * Return the ID of the object to which this result pertains.
     *
     * @since 1.3
     */
    public String getObjectId() {
        return data.getObjectId();
    }

    /**
     * Return the name of the object to which this result pertains.
     *
     * @since 1.3
     */
    public String getObjectName() {
        return data.getObjectId();
    }

    /**
     * Return true if the task completed succesfully, false if not.
     */
    public boolean isSuccessful() {
        return data.isSuccessful();
    }

    /**
     * Return an immutable list of detail objects.
     */
    public List<TaskResultDetail> getDetails() {
        return details;
    }

    /**
     * Return true iff a failure dump file was produced during the operation.
     */
    public boolean isFailureDumpAvailable() {
        return data.isFailureDumpAvailable();
    }

    /**
     * Return a ServerFile containing the dump file which can be retrieved.
     */
    public ServerFile getFailureDump() {
        if (!isFailureDumpAvailable()) {
            return null;
        }
        ServerFileData serverFileData = new ServerFileData();
        serverFileData.setId(dumpFileId);

        return new ServerFile(task.getSubject().getModel(), serverFileData) {

            @Override
            List<ChunkData> getChunks() {
                if (isNestedResult) {
                    LOG.debug("Fetching {} action's nested-dump file chunks for task={}, dumpfileid={}", task.getSubject().getClass().getSimpleName(), task.getId(), dumpFileId);
                    return task.getSubject().getNestedDumpFileChunks(task.getId(), dumpFileId).getItem();
                } else {
                    LOG.debug("Fetching {} action's dump file chunks for task={}", task.getSubject().getClass().getSimpleName(), task.getId());
                    return task.getSubject().getDumpFileChunks(task.getId()).getItem();
                }
            }

            @Override
            byte[] getChunkContent(String chunkId) {
                if (isNestedResult) {
                    LOG.debug("Downloading Nested-dump data-chunk {}:{}", dumpFileId, chunkId);
                    return task.getSubject().getNestedDumpFileChunkContent(task.getId(), dumpFileId, chunkId);
                } else {
                    LOG.debug("Downloading dump data-chunk {}", chunkId);
                    return task.getSubject().getDumpFileChunkContent(task.getId(), chunkId);
                }
            }

            @Override
            public void upLoad(File source, boolean deleteExisting, int chunkSize)
                    throws AnaplanAPIException {
                throw new AnaplanAPIException("Cannot upload to failure dump");
            }
        };
    }

    /**
     * Return an immutable List of nested TaskResults, one for each nested operation.
     *
     * @since 1.3
     */
    public List<TaskResult> getNestedResults() {
        return nestedResults;
    }

    /**
     * Append a textual representation of this task result.
     *
     * @param out The object to receive the formatted output
     * @return The out parameter passed to this method
     * @since 1.3
     */
    public Appendable appendTo(Appendable out) throws IOException {
        for (TaskResultDetail detail : details) {
            if (detail != null) {
                detail.appendTo(out);
                out.append('\n');
            }
        }
        for (TaskResult nestedResult : nestedResults) {
            nestedResult.appendTo(out);
            out.append('\n');
        }
        return out;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        try {
            return appendTo(new StringBuilder()).toString();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }
}
