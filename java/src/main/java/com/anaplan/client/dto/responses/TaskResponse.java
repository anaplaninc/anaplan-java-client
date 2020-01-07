package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.TaskData;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 1:55 AM
 */
public class TaskResponse extends ObjectResponse<TaskData> {

    private TaskData task;

    @Override
    public TaskData getItem() {
        return task;
    }

    @Override
    public void setItem(TaskData item) {
        this.task = item;
    }
}
