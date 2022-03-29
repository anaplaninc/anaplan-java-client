package com.anaplan.client.dto;

import java.io.Serializable;

/**
 * A list failure object that contains information about CRUD operations.
 */
public class ListFailure implements Serializable {

  private int requestIndex;
  private String failureType;
  private String failureMessageDetails;
  private ListItem listItem;

  public int getRequestIndex() {
    return requestIndex;
  }

  public void setRequestIndex(int requestIndex) {
    this.requestIndex = requestIndex;
  }

  public String getFailureType() {
    return failureType;
  }

  public void setFailureType(String failureType) {
    this.failureType = failureType;
  }

  public String getFailureMessageDetails() {
    return failureMessageDetails;
  }

  public void setFailureMessageDetails(String failureMessageDetails) {
    this.failureMessageDetails = failureMessageDetails;
  }

  public ListItem getListItem() {
    return listItem;
  }

  public void setListItem(ListItem listItem) {
    this.listItem = listItem;
  }
}
