package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ServerFileData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:04 AM
 */
public class ServerFilesResponse extends ListResponse<ServerFileData> {

    private List<ServerFileData> files;

    @Override
    public List<ServerFileData> getItem() {
        return files;
    }

    @Override
    public void setItem(List<ServerFileData> item) {
        this.files = item;
    }
}
