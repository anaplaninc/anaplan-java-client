package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ExportData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:07 AM
 */
public class ExportsResponse extends ListResponse<ExportData> {

    private List<ExportData> exports;

    @Override
    public List<ExportData> getItem() {
        return exports;
    }

    @Override
    public void setItem(List<ExportData> item) {
        this.exports = item;
    }
}
