package com.anaplan.client.dto;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:28 PM
 */
public class ModelData {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (!(other != null && other instanceof ModelData)) {
            return false;
        }
        ModelData data = (ModelData) other;
        return id.equals(data.id) && name.equals(data.name);
    }

    public int hashCode() {
        return id.hashCode() * 31 + name.hashCode();
    }
}
