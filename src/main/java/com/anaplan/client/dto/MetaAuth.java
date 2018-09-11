package com.anaplan.client.dto;

import java.io.Serializable;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 8/6/17
 * Time: 2:24 AM
 */
public class MetaAuth implements Serializable {
    private String validationUrl;

    public String getValidationUrl() {
        return this.validationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }
}
