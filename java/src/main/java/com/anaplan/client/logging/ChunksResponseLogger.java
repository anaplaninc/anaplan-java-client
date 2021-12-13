package com.anaplan.client.logging;

import com.anaplan.client.dto.Status;
import com.anaplan.client.dto.responses.ChunksResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunksResponseLogger {
    private static final Logger LOG = LoggerFactory.getLogger(ChunksResponseLogger.class);

    private ChunksResponseLogger() {
    }

    public static void log(ChunksResponse response) {
        if(response == null) {
            LOG.debug("ChunksResponse is null");
            return;
        }
        Status status = response.getStatus();
        if(status == null) {
            LOG.debug("Status in ChunksResponse is null");
            return;
        }
        if(status.getCode() != HttpStatus.SC_OK) {
            LOG.debug("ChunksResponse status: " + status.getCode()+", Message: " + status.getMessage());
        }
    }
}
