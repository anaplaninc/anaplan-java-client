package com.anaplan.client.dto;

import java.io.Serializable;

/**
 * Meta list object
 */
public class MetaList implements Serializable {
    private String schema;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

}
