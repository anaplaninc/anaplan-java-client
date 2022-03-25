package com.anaplan.client.transport;

import com.anaplan.client.dto.Paging;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by Spondon Saha User: spondonsaha Date: 6/26/17 Time: 11:32 AM
 */
public abstract class Paginator<ENTITY> implements Iterable<ENTITY> {

  private Paging pageInfo;
  private int nextIndex;
  private ENTITY[] entities;
  private ENTITY nextEntity;

  /*
   * Select the first entity if present and prepare to read from second entity (index 1)
   */
  public Paginator() {
    setEntities(getPage(0));
    selectFirstEntity();
    nextIndex = 1;
  }

  private void selectFirstEntity() {
    if (entities.length > 0) {
      nextEntity = entities[0];
    } else {
      nextEntity = null;
    }
  }

  public abstract ENTITY[] getPage(int offset);

  /**
   * Returns an iterator for ENTITYs
   * The iterator implementation always tries to read ahead the next ENTITY so when hasNext() is called before next() we know if there is one available
   * When it reaches the end of the current page it tries to read the next page to see if there is another available ENTITY
   *
   * @return An ENTITY iterator
   */
  @Override
  public Iterator<ENTITY> iterator() {
    return new Iterator<ENTITY>() {

      @Override
      public boolean hasNext() {
        return nextEntity != null;
      }

      @Override
      public ENTITY next() {
        if (nextEntity == null) {
          throw new NoSuchElementException();
        }
        ENTITY toReturn = nextEntity;
        nextEntity = null;
        if (nextIndex < entities.length) {
          nextEntity = entities[nextIndex++];
        } else if (nextIndex == entities.length && nextIndex + pageInfo.getOffset() < pageInfo
            .getTotalSize()) {
          entities = getPage(pageInfo.getOffset() + pageInfo.getCurrentPageSize());
          selectFirstEntity();
          nextIndex = 0;
        }
        return toReturn;
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
