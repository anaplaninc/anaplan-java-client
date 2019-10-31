package com.anaplan.client.dto;

import java.io.Serializable;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 8/6/17
 * Time: 2:14 AM
 */
public class TokenInfo implements Serializable {
    private String tokenId;
    private String tokenValue;
    private String refreshTokenId;
    private long expiresAt;

    public TokenInfo() {
    }

    public String getTokenId() {
        return this.tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenValue() {
        return this.tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public String getRefreshTokenId() {
        return this.refreshTokenId;
    }

    public void setRefreshTokenId(String refreshTokenId) {
        this.refreshTokenId = refreshTokenId;
    }

    public Long getExpiresAt() {
        return this.expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TokenInfo{");
        sb.append("tokenId='").append(tokenId).append('\'');
        sb.append(", tokenValue='").append(tokenValue).append('\'');
        sb.append(", refreshTokenId='").append(refreshTokenId).append('\'');
        sb.append(", expiresAt=").append(expiresAt);
        sb.append('}');
        return sb.toString();
    }
}
