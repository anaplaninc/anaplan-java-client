package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ActionData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:08 AM
 */
public class ActionsResponse extends ListResponse<ActionData> {

    private List<ActionData> actions;

    @Override
    public List<ActionData> getItem() {
        return actions;
    }

    @Override
    public void setItem(List<ActionData> item) {
        this.actions = item;
    }
}
