package com.anaplan.client.dto;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:28 PM
 */
public class ModelData {
    private String id;

    public String getId() {
        return id;
    }

    public ModelData(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object other) {
        if (!(other != null && other instanceof ModelData)) {
            return false;
        }
        ModelData data = (ModelData) other;
        return id.equals(data.id);
    }

    public int hashCode() {
        return id.hashCode();
    }
}
