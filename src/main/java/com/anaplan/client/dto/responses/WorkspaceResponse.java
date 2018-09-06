package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.WorkspaceData;

/**
 * Created by Spondon Saha
 * Date: 4/18/18
 * Time: 9:46 AM
 */
public class WorkspaceResponse extends ObjectResponse<WorkspaceData> {

    private WorkspaceData workspace;

    @Override
    public WorkspaceData getItem() {
        return workspace;
    }

    @Override
    public void setItem(WorkspaceData item) {
        this.workspace = item;
    }
}
