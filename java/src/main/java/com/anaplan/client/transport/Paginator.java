package com.anaplan.client.transport;

import com.anaplan.client.dto.Paging;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/26/17
 * Time: 11:32 AM
 */
public abstract class Paginator<ENTITY> implements Iterable<ENTITY> {

    private Paging pageInfo;
    private int currentIndex = 0;
    private ENTITY[] entities;

    public Paginator() {
        setEntities(getPage(0));
    }

    public abstract ENTITY[] getPage(int offset);

    @Override
    public Iterator<ENTITY> iterator() {
        return new Iterator<ENTITY>() {
            @Override
            public boolean hasNext() {
                return currentIndex + pageInfo.getOffset() < pageInfo.getTotalSize();
            }

            @Override
            public ENTITY next() {
                if (hasNext()) {
                    if (currentIndex == entities.length) {
                        entities = getPage(pageInfo.getOffset() + pageInfo.getCurrentPageSize());
                        currentIndex = 0;
                    }
                    return entities[currentIndex++];
                }
                throw new NoSuchElementException();
            }
        };
    }

    public Paging getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(Paging pageInfo) {
        this.pageInfo = pageInfo;
    }

    public void setEntities(ENTITY[] entities) {
        this.entities = entities;
    }
}
