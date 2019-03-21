package com.anaplan.client.dto;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:35 PM
 */
public class MappingParameterData {
    private String importId;
    private String entityType;
    private String entityName;

    public String getImportId() {
        return importId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setImportId(String importId) {
        this.importId = importId;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
