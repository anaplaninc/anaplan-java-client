package com.anaplan.client;

import com.anaplan.client.api.AnaplanAPI;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class LargeDataExport {

  private static final int WAIT = 100;
  public enum TYPE_LARGE_EXPORT {LIST_EXPORT, VIEW_EXPORT}

  private final AnaplanAPI api;
  private final TYPE_LARGE_EXPORT largeExportType;

  private LargeDataExport(final AnaplanAPI api, final TYPE_LARGE_EXPORT largeExportType) {
    this.api = api;
    this.largeExportType = largeExportType;
  }

  public static LargeDataExport getLargeDataExportService(final AnaplanAPI api,
      final TYPE_LARGE_EXPORT largeExportType) {
    return new LargeDataExport(api, largeExportType);
  }

  private String getViewRequestData(final String workspaceId, final String modelId,
      final String id, final String requestId)
      throws InterruptedException {
    ViewRequestData requestDataStatus;
    String state;
    //Wait data to be ready for download or cancelled
    while (true) {
      requestDataStatus = api
          .getViewRequestStatus(workspaceId, modelId, id, requestId);
      state = requestDataStatus.getViewReadRequest().getRequestState();
      if (Objects.equals(state, Constants.CANCELLED)) {
        return StringUtils.EMPTY;
      } else if (Objects.equals(state, Constants.COMPLETE)) {
        break;
      }
      synchronized (this) {
        this.wait(WAIT);
      }
    }

    final int pages = requestDataStatus.getViewReadRequest().getAvailablePages();
    if (pages == 0) {
      return StringUtils.EMPTY;
    }
    //The data is splitted into max 10mb per page
    return getAllPagesFromLargeExport(pages, workspaceId, modelId, id, requestId);
  }

  private String getListRequestData(final String workspaceId, final String modelId,
      final String id, final String requestId)
      throws InterruptedException {
    LargeRequestData requestDataStatus;
    String state;
    //Wait data to be ready for download or cancelled
    while (true) {
      requestDataStatus = api
          .getListRequestStatus(workspaceId, modelId, id, requestId);
      state = requestDataStatus.getListReadRequest().getRequestState();
      if (Objects.equals(state, Constants.CANCELLED)) {
        return StringUtils.EMPTY;
      } else if (Objects.equals(state, Constants.COMPLETE)) {
        break;
      }
      synchronized (this) {
        this.wait(WAIT);
      }
    }

    final int pages = requestDataStatus.getListReadRequest().getAvailablePages();
    if (pages == 0) {
      return StringUtils.EMPTY;
    }
    //The data is splitted into max 10mb per page
    return getAllPagesFromLargeExport(pages, workspaceId, modelId, id, requestId);
  }

  public String getRequestData(final String workspaceId, final String modelId,
      final String id, final String requestId)
      throws InterruptedException {

    if (largeExportType == TYPE_LARGE_EXPORT.LIST_EXPORT) {
      return getListRequestData(workspaceId, modelId,
          id, requestId);
    } else if (largeExportType == TYPE_LARGE_EXPORT.VIEW_EXPORT) {
      return getViewRequestData(workspaceId, modelId,
          id, requestId);
    }
    return StringUtils.EMPTY;
  }

  private String getAllPagesFromLargeExport(final int lastPage, final String workspaceId,
      final String modelId,
      final String viewId, final String requestId) {
    if (Integer.max(lastPage, 0) == 0) {
      return StringUtils.EMPTY;
    }
    final StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < lastPage; i++) {
      if (largeExportType == TYPE_LARGE_EXPORT.LIST_EXPORT) {
        buffer
            .append(api.downloadCSVListRequest(workspaceId, modelId, viewId, requestId, i));
      } else if (largeExportType == TYPE_LARGE_EXPORT.VIEW_EXPORT) {
        buffer
            .append(api.downloadCSVViewRequestPage(workspaceId, modelId, viewId, requestId, i));
      }
    }
    return buffer.toString();
  }

}
