package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.TokenInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 8/6/17
 * Time: 9:15 AM
 */
public class AuthenticationResp extends AuthObjectResponse<TokenInfo> {

    private TokenInfo tokenInfo;

    @Override
    @JsonProperty("tokenInfo")
    public TokenInfo getItem() {
        return tokenInfo;
    }

    @Override
    @JsonProperty("tokenInfo")
    public void setItem(TokenInfo item) {
        this.tokenInfo = item;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AuthenticationResp{");
        sb.append("tokenInfo=").append(tokenInfo);
        sb.append('}');
        sb.append(super.toString());
        return sb.toString();
    }
}
