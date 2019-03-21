package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.WorkspaceData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 9:34 AM
 */
public class WorkspacesResponse extends ListResponse<WorkspaceData> {

    private List<WorkspaceData> workspaces;

    @Override
    public List<WorkspaceData> getItem() {
        return workspaces;
    }

    @Override
    public void setItem(List<WorkspaceData> item) {
        this.workspaces = item;
    }
}
