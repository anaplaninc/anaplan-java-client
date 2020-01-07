package com.anaplan.client.dto.responses;

import com.anaplan.client.dto.ChunkData;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:15 AM
 */
public class ChunksResponse extends ListResponse<ChunkData> {

    private List<ChunkData> chunks;

    @Override
    public List<ChunkData> getItem() {
        return chunks;
    }

    @Override
    public void setItem(List<ChunkData> item) {
        this.chunks = item;
    }
}
