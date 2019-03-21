package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.MetaAuth;

import java.io.Serializable;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 8/6/17
 * Time: 2:20 PM
 */
public class BaseAuthResponse implements Serializable {
    private MetaAuth meta;
    private String status;
    private String statusMessage;

    public MetaAuth getMeta() {
        return meta;
    }

    public void setMeta(MetaAuth meta) {
        this.meta = meta;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseAuthResponse{");
        sb.append("meta=").append(meta);
        sb.append(", status='").append(status).append('\'');
        sb.append(", statusMessage='").append(statusMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
