package com.anaplan.client.dto;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 1:13 AM
 */
public class Paging {
    private String next;
    private Integer currentPageSize;
    private Integer offset;
    private Integer totalSize;

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public Integer getCurrentPageSize() {
        return currentPageSize;
    }

    public void setCurrentPageSize(Integer currentPageSize) {
        this.currentPageSize = currentPageSize;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }
}
