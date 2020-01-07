package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.TaskData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 1:58 AM
 */
public class TasksResponse extends ListResponse<TaskData> {

    private List<TaskData> tasksDatas;

    @Override
    public List<TaskData> getItem() {
        return tasksDatas;
    }

    @Override
    public void setItem(List<TaskData> item) {
        this.tasksDatas = item;
    }
}
