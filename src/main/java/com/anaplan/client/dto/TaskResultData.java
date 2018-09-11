package com.anaplan.client.dto;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:37 PM
 */
public class TaskResultData {

    private String objectId;
    private String objectName;
    private boolean successful;
    private List<TaskResultDetailData> details;
    private boolean failureDumpAvailable;
    private List<TaskResultData> nestedResults;

    public String getObjectId() {
        return objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public List<TaskResultDetailData> getDetails() {
        return details;
    }

    public boolean isFailureDumpAvailable() {
        return failureDumpAvailable;
    }

    public List<TaskResultData> getNestedResults() {
        return nestedResults;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public void setDetails(List<TaskResultDetailData> details) {
        this.details = details;
    }

    public void setFailureDumpAvailable(boolean failureDumpAvailable) {
        this.failureDumpAvailable = failureDumpAvailable;
    }

    public void setNestedResults(List<TaskResultData> nestedResults) {
        this.nestedResults = nestedResults;
    }
}
