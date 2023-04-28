package com.anaplan.client.dto.responses;

import java.io.Serializable;

/**
 * Large view , list response
 */
public class BaseLargeResponse extends BaseListResponse implements Serializable {
    private String requestId;
    private String requestState;
    private String url;
    private int availablePages;
    private boolean successful;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestState() {
        return requestState;
    }

    public void setRequestState(String requestState) {
        this.requestState = requestState;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getAvailablePages() {
        return availablePages;
    }

    public void setAvailablePages(int availablePages) {
        this.availablePages = availablePages;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

}
