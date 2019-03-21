package com.anaplan.client.dto;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:25 PM
 */
public class NamedObjectData extends TaskParametersData {
    private String id;
    private String name;
    private String code;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public <T extends NamedObjectData> T merge(TaskParametersData data, Class<T> dataClass) {
        this.setLocaleName((data.getLocaleName() != null) ? data.getLocaleName() : this.getLocaleName());
        this.setConnectorParameters((data.getConnectorParameters() != null) ? data.getConnectorParameters() : this.getConnectorParameters());
        this.setMappingParameters((data.getMappingParameters() != null) ? data.getMappingParameters() : this.getMappingParameters());
        return dataClass.cast(this);
    }
}
