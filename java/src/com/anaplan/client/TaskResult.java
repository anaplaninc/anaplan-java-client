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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of a task completing.
 */

public class TaskResult {
    static class Data {
        String objectId;
        String objectName;
        boolean successful;
        List<TaskResultDetail.Data> details;
        boolean failureDumpAvailable;
        List<Data> nestedResults;
    }
    private Model model;
    private String dumpFileId;
    private String dumpFilePath;
    private Data data;
    private List<TaskResultDetail> details;
    private List<TaskResult> nestedResults;
    TaskResult(Model model, String dumpFileId, String dumpFilePath, Data data) {
        this.model = model;
        this.dumpFileId = dumpFileId;
        this.dumpFilePath = dumpFilePath;
        this.data = data;
        List<TaskResultDetail> details = new ArrayList<TaskResultDetail>();
        if (null != data.details) {
            for (TaskResultDetail.Data detailData: data.details) {
                details.add(new TaskResultDetail(detailData));
            }
        }
        this.details = Collections.unmodifiableList(details);
        List<TaskResult> nestedResults = new ArrayList<TaskResult>();
        if (null != data.nestedResults) {
            for (TaskResult.Data nestedData: data.nestedResults) {
                nestedResults.add(new TaskResult(model, dumpFileId + "~" + nestedData.objectId, dumpFilePath + "s/" + nestedData.objectId, nestedData));
            }
        }
        this.nestedResults = Collections.unmodifiableList(nestedResults);
    }
    /**
      * Return the ID of the object to which this result pertains.
      * @since 1.3
      */
    public String getObjectId() {
        return data.objectId;
    }
    /**
      * Return the name of the object to which this result pertains.
      * @since 1.3
      */
    public String getObjectName() {
        return data.objectName;
    }
    /**
      * Return true if the task completed succesfully, false if not.
      */
    public boolean isSuccessful() {
        return data.successful;
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
        return data.failureDumpAvailable;
    }
    /**
      * Return a ServerFile containing the dump file which can be retrieved.
      */
    public ServerFile getFailureDump() {
        if (!isFailureDumpAvailable()) {
            return null;
        }
        ServerFile.Data serverFileData = new ServerFile.Data();
        serverFileData.id = dumpFileId;
        return new ServerFile(model, serverFileData) {
            @Override
            String getPath() {
                return dumpFilePath;
            }
            @Override
            public void upLoad(File source, boolean deleteExisting)
                    throws AnaplanAPIException{
                throw new AnaplanAPIException("Cannot upload to failure dump");
            }
        };
    }
    /**
      * Return an immutable List of nested TaskResults, one for each nested operation.
      * @since 1.3
      */
    public List<TaskResult> getNestedResults() {
        return nestedResults;
    }

    /**
      * Append a textual representation of this task result.
      * @param out The object to receive the formatted output
      * @return The out parameter passed to this method
      * @since 1.3
      */
    public Appendable appendTo(Appendable out) throws IOException {
        for (TaskResultDetail detail: details) {
            detail.appendTo(out);
            out.append('\n');
        }
        for (TaskResult nestedResult: nestedResults) {
            nestedResult.appendTo(out);
            out.append('\n');
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        try {
            return appendTo(new StringBuilder()).toString();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }
}
