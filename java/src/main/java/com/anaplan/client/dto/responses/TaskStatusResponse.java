package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.TaskStatusData;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 1:49 AM
 */
public class TaskStatusResponse extends ObjectResponse<TaskStatusData> {

    private TaskStatusData task;

    @Override
    public TaskStatusData getItem() {
        return task;
    }

    @Override
    public void setItem(TaskStatusData item) {
        this.task = item;
    }
}
