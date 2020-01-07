package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.TokenInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 9/14/17
 * Time: 2:45 PM
 */
public class ValidationTokenResp extends AuthObjectResponse<TokenInfo> {

    private TokenInfo tokenInfo;

    @Override
    @JsonProperty("tokenInfo")
    public TokenInfo getItem() {
        return tokenInfo;
    }

    @Override
    @JsonProperty("tokenInfo")
    public void setItem(TokenInfo tokenInfo) {
        this.tokenInfo = tokenInfo;
    }
}