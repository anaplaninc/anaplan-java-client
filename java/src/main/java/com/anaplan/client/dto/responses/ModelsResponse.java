package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ModelData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:21 AM
 */
public class ModelsResponse extends ListResponse<ModelData> {

    private List<ModelData> models;

    @Override
    public List<ModelData> getItem() {
        return models;
    }

    @Override
    public void setItem(List<ModelData> item) {
        this.models = item;
    }
}
