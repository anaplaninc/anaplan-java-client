package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.Meta;
import com.anaplan.client.dto.Status;

import java.io.Serializable;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 7/6/17
 * Time: 12:00 PM
 */
public class BaseResponse implements Serializable {
    private Meta meta;
    private Status status;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
