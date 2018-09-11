package com.anaplan.client.dto;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:34 PM
 */
public class ConnectorParameterData {
    private String sourceIdOrType;
    private String parameterId;
    private String value;

    public String getSourceIdOrType() {
        return sourceIdOrType;
    }

    public String getParameterId() {
        return parameterId;
    }

    public String getValue() {
        return value;
    }

    public void setSourceIdOrType(String sourceIdOrType) {
        this.sourceIdOrType = sourceIdOrType;
    }

    public void setParameterId(String parameterId) {
        this.parameterId = parameterId;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
