package com.anaplan.client.dto;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:28 PM
 */
public class ModelData extends NamedObjectData {
    private String id;
    private String name;
    private String activeState;
    private String currentWorkspaceId;
    private String currentWorkspaceName;
    private String modelUrl;
    private String[] categoryValues;

    public ModelData() {
    }

    public ModelData(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getActiveState() {
        return activeState;
    }

    public void setActiveState(String activeState) {
        this.activeState = activeState;
    }

    public String getCurrentWorkspaceId() {
        return currentWorkspaceId;
    }

    public void setCurrentWorkspaceId(String currentWorkspaceId) {
        this.currentWorkspaceId = currentWorkspaceId;
    }

    public String getCurrentWorkspaceName() {
        return currentWorkspaceName;
    }

    public void setCurrentWorkspaceName(String currentWorkspaceName) {
        this.currentWorkspaceName = currentWorkspaceName;
    }

    public String getModelUrl() {
        return modelUrl;
    }

    public void setModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }

    public String[] getCategoryValues() {
        return categoryValues;
    }

    public void setCategoryValues(String[] categoryValues) {
        this.categoryValues = categoryValues;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other != null && other instanceof ModelData)) {
            return false;
        }
        ModelData data = (ModelData) other;
        return id.equals(data.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
