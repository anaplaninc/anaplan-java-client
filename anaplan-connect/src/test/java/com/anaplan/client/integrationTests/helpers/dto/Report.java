package com.anaplan.client.integrationTests.helpers.dto;

import java.util.List;

/**
 * dto for dataprovider yaml files.
 */
public class Report {

  private List<String> messageList;

  public List<String> getMessageList() {
    return this.messageList;
  }

  public void setMessageList(List<String> messageList) {
    this.messageList = messageList;
  }

  @Override
  public String toString() {
    return "Report{" +
        "messageList='" + messageList + '\'' +
        '}';
  }
}
