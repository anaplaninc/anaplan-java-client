package com.anaplan.client;

public enum DeviceTypeToken {
  ROTATABLE("rotatable"),
  NON_ROTATABLE("non-rotatable");
  private String value;
  DeviceTypeToken(String value) {
    this.value = value;
  }
  String value(){
    return this.value;
  }
}
