package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.MetaList;
import com.anaplan.client.dto.Status;
import java.io.Serializable;

/**
 * Superclass for list items response
 */
public class BaseListResponse implements Serializable {
    private MetaList meta;
    private Status status;

    public MetaList getMeta() {
        return meta;
    }

    public void setMeta(MetaList meta) {
        this.meta = meta;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
