package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.DeviceCodeInfo;

public class DeviceCodeResp extends ObjectResponse<DeviceCodeInfo> {

  private DeviceCodeInfo deviceCodeInfo;

  @Override
  public DeviceCodeInfo getItem() {
    return deviceCodeInfo;
  }

  @Override
  public void setItem(DeviceCodeInfo deviceCodeInfo) {
    this.deviceCodeInfo = deviceCodeInfo;
  }
}
