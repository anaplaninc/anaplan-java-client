package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ServerFileData;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:05 AM
 */
public class ServerFileResponse extends ObjectResponse<ServerFileData> {

    private ServerFileData file;

    @Override
    public ServerFileData getItem() {
        return file;
    }

    @Override
    public void setItem(ServerFileData item) {
        this.file = item;
    }
}
