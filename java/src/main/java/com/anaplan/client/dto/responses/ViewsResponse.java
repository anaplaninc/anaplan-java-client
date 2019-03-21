package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ViewData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:22 AM
 */
public class ViewsResponse extends ListResponse<ViewData> {

    private List<ViewData> views;

    @Override
    public List<ViewData> getItem() {
        return views;
    }

    @Override
    public void setItem(List<ViewData> item) {
        this.views = item;
    }
}
