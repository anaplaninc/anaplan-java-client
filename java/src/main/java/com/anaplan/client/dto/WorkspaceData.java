package com.anaplan.client.dto;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:41 PM
 */
public class WorkspaceData extends NamedObjectData {

    private String id;
    private boolean active;
    private String name;
    private Long sizeAllowance;

    public WorkspaceData() {
    }

    @Override
    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String getName() {
        return name;
    }

    public Long getSizeAllowance() {
        return sizeAllowance;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setSizeAllowance(Long sizeAllowance) {
        this.sizeAllowance = sizeAllowance;
    }

    public WorkspaceData(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other != null && other instanceof WorkspaceData)) {
            return false;
        }
        WorkspaceData data = (WorkspaceData) other;
        return id.equals(data.id) && name.equals(data.name);
    }

    @Override
    public int hashCode() {
        return id.hashCode() * 31 + name.hashCode();
    }

}
