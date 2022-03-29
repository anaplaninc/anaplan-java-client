package com.anaplan.client.dto;

import com.anaplan.client.dto.responses.BaseListResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

/**
 * List items response
 */
public class ListItemResultData extends BaseListResponse implements Serializable {

  private int added;
  private int deleted;
  private int ignored;
  private int total;
  private int updated;

  @JsonProperty("failures")
  private List<ListFailure> failures;

  public int getDeleted() {
    return deleted;
  }

  public void setDeleted(int deleted) {
    this.deleted = deleted;
  }

  public int getAdded() {
    return added;
  }

  public void setAdded(int added) {
    this.added = added;
  }

  public int getIgnored() {
    return ignored;
  }

  public void setIgnored(int ignored) {
    this.ignored = ignored;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public List<ListFailure> getFailures() {
    return failures;
  }

  public void setFailures(List<ListFailure> failures) {
    this.failures = failures;
  }

  public int getUpdated() {
    return updated;
  }

  public void setUpdated(int updated) {
    this.updated = updated;
  }
}
