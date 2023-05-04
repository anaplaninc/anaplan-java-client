//   Copyright 2011 Anaplan Inc.
//
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.anaplan.client;

import com.anaplan.client.dto.ItemMetadataRow;
import com.anaplan.client.dto.ModuleData;
import com.anaplan.client.dto.ViewMetadata;
import com.anaplan.client.dto.ViewMetadataRow;
import com.anaplan.client.dto.responses.ItemData;
import com.anaplan.client.dto.responses.ItemMetadataResponse;
import com.anaplan.client.dto.responses.ViewsResponse;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.exceptions.PageDimensionNotFoundException;
import com.anaplan.client.exceptions.ViewDataNotFoundException;
import com.anaplan.client.exceptions.ViewMetadataNotFoundException;
import com.anaplan.client.exceptions.ViewNotFoundException;
import com.anaplan.client.listwriter.ListItemFileWriter;
import com.anaplan.client.listwriter.MultiColumnCsvTransformer;
import com.anaplan.client.listwriter.SingleColumnCsvTransformer;
import com.anaplan.client.transport.Paginator;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A module within a model.
 *
 * @since 1.1
 */
public class Module extends NamedObject {

  public static final String CSV_SINGLE_COL = "csv_sc";
  public static final String CSV_MULTI_COL = "csv_mc";
  public static final String JSON = "json";
  private static final Logger LOG = LoggerFactory.getLogger(Module.class);

  Module(Model model, ModuleData data) {
    super(model, data);
  }

  /**
   * Splitting the pagedimension and itemdimension using regex
   */
  private static String[] dimensionSplit(String page) {
    String regex = "(?<!\\\\)" + Pattern.quote(Constants.COLON);
    String[] dimensionSplit = page.split(regex);
    if (dimensionSplit.length != 2) {
      throw new IllegalArgumentException("Invalid pageDimension:itemDimension format");
    }
    return dimensionSplit;
  }

  /**
   * Removing escape characters
   */
  private static String escapeBackSlash(String src) {
    return src.replace("\\", StringUtils.EMPTY);
  }

  /**
   * Export list metadata to console or file
   *
   * @param fileType    CSV or JSON
   * @param fileId      The id of the file or null if the export is to the console
   * @param workspaceId The id of the workspace
   * @param modelId     The id of the model
   * @param viewId      The id of the view
   * @param pages       Paginated search: pages
   */
  public void exportViewData(String fileType, String fileId, String workspaceId, String modelId, String viewId,
      String[] pages) throws IOException {
    String searchPages = null;
    if (workspaceId == null || modelId == null) {
      return;
    }
      View view = getView(Optional.ofNullable(viewId).orElse("default"));
      if (fileId == null) {
        fileId = super.getName() + "_" + view.getName() + (JSON.equalsIgnoreCase(fileType) ? ".json" : ".csv");
      }
      if (view == null) {
        LOG.error("View \"{}\" not found in workspace \"{}\", model \"{}\"", viewId, workspaceId, modelId);
        throw new ViewNotFoundException(viewId);
      }
    File targetFile = new File(fileId);
    Collection<List<String>> searchPageSplit = new ArrayList<>();
    ViewMetadata viewMetadata = getViewMetadata(modelId, view);
    if (pages != null) {
      searchPages = getSearchPage(pages, viewMetadata, modelId, viewId, searchPageSplit);
    }
    LOG.info("Starting export for the view - {} ", view.getName());
    try {
      if (CSV_SINGLE_COL.equalsIgnoreCase(fileType)) {
        String linesCsv = getViewDataCsv(modelId, view, searchPages);
        String singleColumnCsv = SingleColumnCsvTransformer.toSingleColumn(viewMetadata, linesCsv);
        ListItemFileWriter.linesToFile(view.getName(), targetFile.toPath(), singleColumnCsv);
      } else if (CSV_MULTI_COL.equalsIgnoreCase(fileType)) {
        String linesCsv = getViewDataCsv(modelId, view, searchPages);
        String multiColumnCsv = MultiColumnCsvTransformer.toMultiColumn(viewMetadata, linesCsv);
        ListItemFileWriter.linesToFile(view.getName(), targetFile.toPath(), multiColumnCsv);
      } else if (JSON.equalsIgnoreCase(fileType)) {
        String linesJson = getApi().getViewDataJson(modelId, view.getId(), searchPages);
        ListItemFileWriter.linesToFile(view.getName(), targetFile.toPath(), linesJson);
      }
      LOG.info("Export for the view completed successfully");
    } catch (AnaplanAPIException | IOException aae) {
      String message;
      try {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonParser parser = factory.createParser(aae.getMessage());
        JsonNode actualObj = mapper.readTree(parser);
        message = actualObj.get("status").get("message").toPrettyString();
      } catch (Exception e) {
        throw aae;
      }
      throw new AnaplanAPIException(message);
    }
  }

  private String getSearchPage(String[] pages, ViewMetadata viewMetadata, final String modelId, final String viewId, Collection<List<String>> searchPageSplit) {
    for (String page : pages) {
      String[] dimensions = dimensionSplit(page);
      final String dimensionEscaped = escapeBackSlash(dimensions[0]);
      ViewMetadataRow viewMetadataRow = getViewMetadataRow(viewMetadata, dimensionEscaped);
      if (viewMetadataRow != null) {
        String itemId = getItemId(viewMetadataRow, modelId, escapeBackSlash(dimensions[1]));
        searchPageSplit.add(Arrays.asList(viewMetadataRow.getId(), itemId));
      } else {
        LOG.error("PageDimension \"{}\" not found in view \"{}\", model \"{}\"", dimensionEscaped,
            viewId, modelId);
        throw new PageDimensionNotFoundException(escapeBackSlash(dimensions[0]));
      }
    }
    return searchPageSplit.stream().map(
        nextList -> nextList.stream()
            .collect(Collectors.joining(Constants.COLON)))
        .collect(Collectors.joining(","));
  }

  /**
   * Get grid CSV data for specific age or if that is null returns data for the first paage
   *
   * @param modelId Model id
   * @param view    View
   * @return The CSV in grid format
   */
  private String getViewDataCsv(String modelId, View view, String searchPages) {
    try {
      return getApi().getViewDataCsv(modelId, view.getId(), searchPages);
    } catch (Exception e) {
      throw new ViewDataNotFoundException(view.getId(), e);
    }
  }

  /**
   * Retrieve view metadata
   *
   * @param modelId Model id
   * @param view    View
   * @return View metadata
   */
  private ViewMetadata getViewMetadata(String modelId, View view) {
    try {
      return getApi().getViewMetadata(modelId, view.getId()).getViewMetadata();
    } catch (Exception e) {
      throw new ViewMetadataNotFoundException(view.getId(), e);
    }
  }

  /**
   * Retrieve item metadata
   *
   * @param modelId Model id
   * @return View metadata
   */
  private ItemMetadataResponse getItemMetadata(ViewMetadataRow viewMetadataRow, String modelId, String itemId) {
    ItemData itemData = new ItemData();
    List<String> names = new ArrayList<>();
    names.add(itemId);
    itemData.setNames(names);
    //Time and Version dimensions do not support codes.
    if (!viewMetadataRow.getName().equals(Constants.TIME_DIMENSION) || !viewMetadataRow.getName().equals(Constants.VERSION_DIMENSION)) {
      List<String> codes = new ArrayList<>();
      codes.add(itemId);
      itemData.setCodes(codes);
    }
    return getApi().getItemId(modelId, viewMetadataRow.getId(), itemData);
  }

  /**
   * Get all pageDimensionId for page names
   */

  private ViewMetadataRow getViewMetadataRow(ViewMetadata viewMetadata, String pageDimensionIdOrName) {
    if (pageDimensionIdOrName == null) {
      return null;
    }
    if (viewMetadata != null) {
      return viewMetadata.getPages().stream()
          .filter(pageDimension -> pageDimensionIdOrName.equals(pageDimension.getName()) || pageDimensionIdOrName.equals(pageDimension.getId())
              || pageDimensionIdOrName.equals(pageDimension.getCode()))
          .findAny().orElse(null);
    }
    return null;
  }

  /**
   * Get all itemIds for Item names/codes/ids
   */

  private String getItemId(ViewMetadataRow viewMetadataRow, String modelId, String itemIdOrName) {
    if (itemIdOrName == null) {
      return null;
    } else {
      ItemMetadataResponse itemMetadataResponse = getItemMetadata(viewMetadataRow, modelId, itemIdOrName);
      if (itemMetadataResponse.getItem() != null) {
        ItemMetadataRow itemMetadataRow = itemMetadataResponse.getItem().stream()
            .filter(item -> itemIdOrName.equals(item.getId()) || itemIdOrName.equals(item.getName()) || itemIdOrName.equals(item.getCode()))
            .findAny().orElse(null);
        return itemMetadataRow == null ? "" : itemMetadataRow.getId();
      }
      return itemIdOrName;
    }
  }

  /**
   * Retrieve a list of available views.
   *
   * @return A list of the available views within this module
   */
  public Iterable<View> getViews() throws AnaplanAPIException {
    Module self = this;
    return new Paginator<View>() {

      @Override
      public View[] getPage(int offset) {
        ViewsResponse response = getApi().getViews(getModel().getId(), getId(), offset);
        setPageInfo(response.getMeta().getPaging());
        if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
          return response.getItem()
              .stream()
              .map(viewData -> new View(self, viewData))
              .toArray(View[]::new);
        } else {
          return new View[]{};
        }
      }
    };
  }

  /**
   * Retrieve a specific view.
   *
   * @param identifier The name, code or id for the view
   * @return The view object
   */
  public View getView(String identifier) throws AnaplanAPIException {
    for (View view : getViews()) {
      if (identifier.equals(view.getId())
          || identifier.equalsIgnoreCase(view.getCode())
          || identifier.equalsIgnoreCase(view.getName())) {
        return view;
      }
    }
    return null;
  }
}