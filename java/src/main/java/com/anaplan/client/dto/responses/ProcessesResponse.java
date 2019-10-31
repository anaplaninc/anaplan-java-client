package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ProcessData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:10 AM
 */
public class ProcessesResponse extends ListResponse<ProcessData> {

    private List<ProcessData> processes;

    @Override
    public List<ProcessData> getItem() {
        return processes;
    }

    @Override
    public void setItem(List<ProcessData> item) {
        this.processes = item;
    }
}
