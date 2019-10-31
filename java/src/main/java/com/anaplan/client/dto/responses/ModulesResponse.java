package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ModuleData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:02 AM
 */
public class ModulesResponse extends ListResponse<ModuleData> {

    private List<ModuleData> modules;

    @Override
    public List<ModuleData> getItem() {
        return modules;
    }

    @Override
    public void setItem(List<ModuleData> item) {
        this.modules = item;
    }
}
