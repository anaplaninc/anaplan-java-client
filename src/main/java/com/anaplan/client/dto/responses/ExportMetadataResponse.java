package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ExportMetadata;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:27 AM
 */
public class ExportMetadataResponse extends ObjectResponse<ExportMetadata> {

    private ExportMetadata exportMetadata;

    @Override
    public ExportMetadata getItem() {
        return exportMetadata;
    }

    @Override
    public void setItem(ExportMetadata item) {
        this.exportMetadata = item;
    }
}
