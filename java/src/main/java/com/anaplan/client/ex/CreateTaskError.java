package com.anaplan.client.ex;

/**
 * Created by Spondon Saha
 * Date: 4/20/18
 * Time: 2:40 PM
 */
public class CreateTaskError extends RuntimeException {
    public CreateTaskError(String taskId) {
        super("Unable to create task with ID=" + taskId);
    }
}
