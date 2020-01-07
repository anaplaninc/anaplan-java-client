package com.anaplan.client.ex;

/**
 * Used for throwing an error whenever the a file-chunk is not available from the server or
 * there was an error in flight while fetching the chunk.
 */
public class NoChunkError extends RuntimeException {
    public NoChunkError(String chunkId) {
        super("Could not fetch Chunk:" + chunkId);
    }
}
