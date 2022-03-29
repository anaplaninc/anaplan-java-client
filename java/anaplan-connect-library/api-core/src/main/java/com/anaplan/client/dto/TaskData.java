package com.anaplan.client.dto;

import java.io.Serializable;

/**
 * Created by Spondon Saha User: spondonsaha Date: 6/21/17 Time: 3:33 PM
 */
public class TaskData implements Serializable {

  private String taskId;

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
}
