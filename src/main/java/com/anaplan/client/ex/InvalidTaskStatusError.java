package com.anaplan.client.ex;

import com.anaplan.client.dto.responses.TaskStatusResponse;

/**
 * Created by Spondon Saha
 * Date: 4/20/18
 * Time: 3:44 PM
 */
public class InvalidTaskStatusError extends RuntimeException {
    public InvalidTaskStatusError(String taskId, TaskStatusResponse response) {
        super("Invalid task status received for ID=" + taskId + " and Response = " + response.toString());
    }
}
