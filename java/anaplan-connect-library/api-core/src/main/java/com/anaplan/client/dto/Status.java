package com.anaplan.client.dto;

import java.io.Serializable;

/**
 * Created by Spondon Saha User: spondonsaha Date: 6/21/17 Time: 7:19 PM
 */
public class Status implements Serializable {

  private int code;
  private String message;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
