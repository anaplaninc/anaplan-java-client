package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ImportData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:06 AM
 */
public class ImportsResponse extends ListResponse<ImportData> {

    private List<ImportData> imports;

    @Override
    public List<ImportData> getItem() {
        return imports;
    }

    @Override
    public void setItem(List<ImportData> item) {
        this.imports = item;
    }
}
