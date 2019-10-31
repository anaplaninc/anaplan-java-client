package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ModelData;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:01 AM
 */
public class ModelResponse extends ObjectResponse<ModelData> {

    private ModelData model;

    @Override
    public ModelData getItem() {
        return model;
    }

    @Override
    public void setItem(ModelData item) {
        this.model = item;
    }
}
