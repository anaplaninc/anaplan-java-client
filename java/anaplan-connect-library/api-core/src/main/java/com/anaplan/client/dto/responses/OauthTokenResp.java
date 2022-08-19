package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.OauthTokenInfo;

public class OauthTokenResp extends ObjectResponse<OauthTokenInfo>{

  private OauthTokenInfo oauthTokenInfo;

  @Override
  public OauthTokenInfo getItem() {
    return oauthTokenInfo;
  }

  @Override
  public void setItem(OauthTokenInfo oauthTokenInfo) {
    this.oauthTokenInfo = oauthTokenInfo;
  }
}
