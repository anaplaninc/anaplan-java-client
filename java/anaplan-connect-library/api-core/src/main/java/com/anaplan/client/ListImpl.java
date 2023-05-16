package com.anaplan.client;

import static com.anaplan.client.Constants.BATCH_SIZE;

import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.dto.FileType;
import com.anaplan.client.dto.ListFailure;
import com.anaplan.client.dto.ListItem;
import com.anaplan.client.dto.ListItemParametersData;
import com.anaplan.client.dto.ListItemResultData;
import com.anaplan.client.dto.ListMetadata;
import com.anaplan.client.dto.ListMetadataProperty;
import com.anaplan.client.dto.ListSubset;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A list item object within an Anaplan model.
 */
public class ListImpl implements ListFactory {


  public enum ListAction {
    ADD,
    UPDATE,
    DELETE
  }
  private static final Logger LOG = LoggerFactory.getLogger(ListImpl.class);
  public static final String NAME = "NAME";
  public static final String CODE = "CODE";
  public static final String PARENT = "PARENT";
  public static final String ID = "ID";
  public static final String JDBC = "JDBC.";

  private final String listId;
  private final String modelId;
  private final String workspaceId;
  private final boolean numberedList;
  private final AnaplanAPI anaplanAPI;
  private final int batchSize;
  private final ListMetadata listMetadata;
  private final List<String> booleanParamList;
  private final Service service;

  public ListImpl(final Service service, String workspaceId, String modelId,
                  final String listId, final boolean isJDBC) {
    this(service, workspaceId, modelId, listId, BATCH_SIZE, isJDBC);
  }

  public ListImpl(final Service service, String workspaceId, String modelId,
                  final String listId, final int batchSize, final boolean isJDBC) {
    this.service = service;
    this.anaplanAPI = service.getApiProvider().get();
    this.workspaceId = service.getWorkspace(workspaceId).getId();
    this.modelId = service.getModel(workspaceId, modelId).getId();
    this.listId = service.getList(listId, this.workspaceId, this.modelId).getId();
    final ListMetadata list = service.getListMetadata(this.workspaceId, this.modelId, this.listId);
    this.numberedList = list.getNumberedList();
    this.listMetadata = service.getListMetadata(this.workspaceId, this.modelId, this.listId);
    this.batchSize = batchSize;
    this.booleanParamList = isJDBC ? Utils.getBooleanParams(this.listMetadata.getProperties()) : new ArrayList<>(0);
  }

  @Override
  public ListItemResultData deleteItemsList(final ListItemParametersData itemParametersData) {
    return anaplanAPI.deleteItemsList(workspaceId, modelId, listId,
        itemParametersData);
  }

  @Override
  public ListItemResultData deleteItemsList(final List<String[]> rows, final String[] header,
                                            final Map<String, String> headerMap) {
    ListItemParametersData itemParametersData = getDeleteItems(rows, header, headerMap);
    return anaplanAPI.deleteItemsList(workspaceId, modelId, listId,
        itemParametersData);
  }

  /**
   * Get data from file
   *
   * @param source the item source path
   * @param itemMapPath the item names path
   * @param fileType {@link FileType}
   * @return listItemResultData {@link ListItemResultData}
   * @throws IOException io errors
   */
  public ListItemResultData doActionToItems(final Path source, final Path itemMapPath,
                                            final FileType fileType, final ListAction action)
      throws IOException, CsvValidationException {
    Utils.isFileAndReadable(source);
    if (itemMapPath != null) {
      Utils.isFileAndReadable(itemMapPath);
    }

    if (fileType == FileType.CSV) {
      return getDataFromCSV(source, itemMapPath, getContent(), action);
    } else if (fileType == FileType.JSON) {
      return parseBatchJSON(source, itemMapPath, getContent(), action);
    }
    return new ListItemResultData();
  }

  public ListItemResultData addItemsToList(final ListItemParametersData listItemParametersData) {
    return anaplanAPI.addItemsToList(workspaceId, modelId, listId,
        listItemParametersData);
  }

  public ListItemResultData updateItemsList(final ListItemParametersData itemParametersData) {
    return this.anaplanAPI.updateItemsList(workspaceId, modelId, listId, itemParametersData);
  }

  /**
   * Parse CSV file item source
   *
   * @param source the items path file
   * @param itemFile the item names path
   * @param metaContent {@link MetaContent}
   * @return {@link ListItemResultData}
   * @throws IOException input-output exception
   */
  public ListItemResultData getDataFromCSV(final Path source,
                                           @Nullable final Path itemFile,
                                           final MetaContent metaContent,
                                           final ListAction action)
      throws IOException, CsvValidationException {

    Map<String, String> properties = new HashMap<>(10);
    if (itemFile != null) {
      properties = Utils.getPropertyFile(new FileInputStream(itemFile.toFile()));
    }
    return parseSource(source, properties, metaContent, action);
  }

  private ListItemResultData parseSource(final Path source, final Map<String, String> mappings,
                                         final MetaContent metaContent,
                                         final ListAction action)
      throws IOException, CsvValidationException {
    try (CSVReader csvReader = new CSVReader(new FileReader(source.toFile()))) {
      final String[] header = csvReader.readNext();
      verifyHeaderMapping(header, mappings, metaContent.getSubsets(), metaContent.getPropNames());
      final Map<Integer, String> propMap = new HashMap<>();
      final Map<Integer, String> subsetsMap = new HashMap<>();
      final Map<String, Integer> parentMap = new HashMap<>(3);
      parseHeader(header, mappings, parentMap, metaContent, propMap, subsetsMap);

      List<ListItem> itemList;
      ListItemResultData overallItemResultData = new ListItemResultData();

      while (!(itemList = parseFileBatch(csvReader, parentMap, propMap, subsetsMap)).isEmpty()) {
        getBatchResultData(itemList, overallItemResultData, action);
      }

      return overallItemResultData;
    }
  }

  public boolean verifyHeaderMapping(final String[] headerSource,
      final Map<String, String> mappingProperty,
      final List<String> propertiesModel,
      final List<String> subsetModel) {
    Set<String> invalidSourceHeaders = mappingProperty.keySet().stream()
        .filter(key-> !key.toUpperCase().startsWith("JDBC"))
        .filter(key -> Arrays.stream(headerSource).noneMatch(s -> s.equalsIgnoreCase(key))).collect(
            Collectors.toSet());
    Set<String> invalidTargetHeaders = mappingProperty.entrySet().stream()
        .filter(e -> !e.getKey().toUpperCase().startsWith("JDBC"))
        .filter(e -> Objects.nonNull(e.getValue()))
        .map(e -> e.getValue().trim())
        .filter(v -> !("name".equalsIgnoreCase(v)
            || "".equals(v)
            || "parent".equalsIgnoreCase(v)
            || "code".equalsIgnoreCase(v)
            || Objects.nonNull(Utils.findInList(v, propertiesModel))
            || Objects.nonNull(Utils.findInList(v, subsetModel))))
        .collect(Collectors.toSet());
    if (!invalidSourceHeaders.isEmpty() || !invalidTargetHeaders.isEmpty()) {
      LOG.warn("The provided mapping file has invalid mappings which will be ignored:");
      invalidSourceHeaders.forEach(k-> LOG.info("Source mapping: {}", k));
      invalidTargetHeaders.forEach(v-> LOG.info("Target mapping: {}", v));
      return false;
    }
    return true;
  }

  private void getFromHeader(final String realColName, final Map<String, Integer> parentMap, final
  Collection<String> prop, final Collection<String> subset, Map<Integer, String> propMap,
                             Map<Integer, String> subsetsMap, int i) {
    if (NAME.equalsIgnoreCase(realColName)) {
      parentMap.put(NAME, i);
    } else if (PARENT.equalsIgnoreCase(realColName)) {
      parentMap.put(PARENT, i);
    } else if (CODE.equalsIgnoreCase(realColName)) {
      parentMap.put(CODE, i);
    } else {
      String exist = Utils.findInList(realColName, prop);
      if (exist != null) {
        propMap.put(i, realColName);
      } else {
        exist = Utils.findInList(realColName, subset);
        if (exist != null) {
          subsetsMap.put(i, realColName);
        }
      }
    }
  }

  public void parseHeader(final String[] header, final Map<String, String> mappings,
                          final Map<String, Integer> parentMap,
                          final MetaContent metaContent, final Map<Integer, String> propMap,
                          final Map<Integer, String> subsetsMap) {
    String realColName;
    int size = header.length;
    for (int i = 0; i < size; i++) {
      final String colName = header[i];
      realColName = getRealColName(colName, mappings);
      getFromHeader(realColName, parentMap, metaContent.getPropNames(), metaContent.getSubsets(),
          propMap, subsetsMap, i);
    }
  }

  public List<ListItem> parseFileBatch(final CSVReader csvReader,
                                       final Map<String, Integer> parentMap,
                                       final Map<Integer, String> propMap,
                                       final Map<Integer, String> subsetsMap)
      throws IOException, CsvValidationException {
    final List<ListItem> itemList = new ArrayList<>(this.batchSize);
    String[] columns;
    while ((columns = csvReader.readNext()) != null) {
      final ListItem itemData =
          ListItem.mapCSVToItemData(columns, parentMap, propMap, subsetsMap, booleanParamList, listMetadata.getNumberedList(), false);
      itemList.add(itemData);

      if (itemList.size() == this.batchSize) {
        return itemList;
      }
    }
    return itemList;
  }

  private void getBatchResultData(final List<ListItem> itemList,
                                  final ListItemResultData overallItemResultData,
                                  final ListAction action) {
    ListItemResultData batchItemResultData = new ListItemResultData();
    ListItemParametersData listItemParametersData = new ListItemParametersData();
    listItemParametersData.setItems(itemList);

    switch (action) {
      case ADD:
        batchItemResultData = anaplanAPI.addItemsToList(workspaceId, modelId, listId,
            listItemParametersData);
        break;
      case UPDATE:
        batchItemResultData = anaplanAPI.updateItemsList(workspaceId, modelId, listId,
            listItemParametersData);
        break;
      case DELETE:
        batchItemResultData = anaplanAPI.deleteItemsList(workspaceId, modelId, listId,
            listItemParametersData);
    }

    overallItemResultData
        .setUpdated(batchItemResultData.getUpdated() + overallItemResultData.getUpdated());
    overallItemResultData
        .setAdded(batchItemResultData.getAdded() + overallItemResultData.getAdded());
    overallItemResultData
        .setDeleted(batchItemResultData.getDeleted() + overallItemResultData.getDeleted());
    overallItemResultData
        .setIgnored(batchItemResultData.getIgnored() + overallItemResultData.getIgnored());

    for (ListFailure listFailure : CollectionUtils.emptyIfNull(batchItemResultData.getFailures())) {
      listFailure.setListItem(itemList.get(listFailure.getRequestIndex()));
    }

    if (overallItemResultData.getFailures() == null) {
      overallItemResultData.setFailures(batchItemResultData.getFailures());
    } else {
      if (batchItemResultData.getFailures() != null) {
        overallItemResultData.getFailures().addAll(batchItemResultData.getFailures());
      }
      overallItemResultData.setFailures(overallItemResultData.getFailures());
    }
  }

  private String getRealColName(String sourceCol, final Map<String, String> mappings) {
    if (mappings != null && mappings.containsKey(sourceCol)) {
      return mappings.get(sourceCol);
    }
    return sourceCol;
  }

  private KeyValue getPropertyValue(final Map<String, String> properties, final String key) {
    if (properties == null || key == null) {
      return null;
    }
    for (Entry<String, String> keyProp : properties.entrySet()) {
      final String keyPoperty = keyProp.getKey().trim();
      final String valueProperty = keyProp.getValue().trim();
      if (keyPoperty.equalsIgnoreCase(key)) {
        return new KeyValue(keyPoperty, valueProperty);
      }
    }
    return new KeyValue(key, key);
  }

  public MetaContent getContent() {
    final ListMetadata listMetadata =
        service.getListMetadata(workspaceId, modelId, listId);
    List<String> propNames = new ArrayList<>(0);
    if (listMetadata.getProperties() != null) {
      propNames = listMetadata.getProperties().stream()
          .map(ListMetadataProperty::getName).collect(Collectors.toList());
    }
    List<String> subsets = new ArrayList<>(0);
    if (listMetadata.getSubsets() != null) {
      subsets = listMetadata.getSubsets().stream().map(ListSubset::getName)
          .collect(Collectors.toList());
    }

    return new MetaContent(propNames, subsets);
  }

  /**
   * Return items from jdbc
   * @param header the jdbc columns
   * @param headerMap names mapped
   * @param rows content
   * @return {@link ListItemParametersData}
   */
  public ListItemParametersData getListItemFromJDBC(final String[] header,
                                                    final Map<String, String> headerMap,
                                                    final List<String[]> rows) throws IOException {

    if (header == null || header.length == 0 || rows.isEmpty()) {
      return new ListItemParametersData();
    }

    final Map<String, Integer> parentMap = new HashMap<>();

    final MetaContent metaContent = getContent();
    final Map<Integer, String> mapPropIndex = new HashMap<>(header.length);
    final Map<Integer, String> mapSubsetIndex = new HashMap<>(header.length);
    for (int i = 0; i < header.length; i++) {
      if (headerMap != null && !headerMap.isEmpty()) {
        final String colValue = Utils.getParsedValue(headerMap.get(header[i]));
        if (colValue != null) {
          header[i] = colValue;
        }
      }
      getFromHeader(header[i], parentMap, metaContent.getPropNames(), metaContent.getSubsets(),
          mapPropIndex, mapSubsetIndex, i);
    }
    final List<ListItem> itemsParameterData = new ArrayList<>(rows.size());
    for (final String[] row : rows) {
      itemsParameterData
          .add(ListItem
              .mapCSVToItemData(row, parentMap, mapPropIndex, mapSubsetIndex, booleanParamList, listMetadata.getNumberedList(), true));
    }

    final ListItemParametersData listItemParametersData = new ListItemParametersData();
    listItemParametersData.setItems(itemsParameterData);
    return listItemParametersData;
  }

  /**
   * Return items to be deleted
   * @param rows the content
   * @param header columns
   * @param headerMap names mapped
   * @return {@link ListItemParametersData}
   */
  public ListItemParametersData getDeleteItems(final List<String[]> rows, final String[] header,
                                               final Map<String, String> headerMap) {
    if (Utils.collectionIsEmpty(rows) || header == null || header.length == 0) {
      return new ListItemParametersData();
    }
    final ListItemParametersData itemParametersData = new ListItemParametersData();
    final List<ListItem> items = new ArrayList<>(rows.size());
    IndexCodes index;
    String id = ID;
    String code = CODE;
    if (!Utils.mapIsEmpty(headerMap)) {
      for (final Entry<String, String> key : headerMap.entrySet()) {
        final String value = key.getValue();
        if (ID.equalsIgnoreCase(value)) {
          id = key.getKey();
        } else if (CODE.equalsIgnoreCase(value)) {
          code = key.getKey();
        }
      }
    }
    index = getIndex(header, code, id);

    for (final String[] line : rows) {
      final ListItem item = new ListItem();
      item.setName(index.getNameIndex() > -1 ? line[index.getNameIndex()] : null);
      item.setCode(index.getCodeIndex() > -1 ? line[index.getCodeIndex()] : null);
      items.add(item);
    }
    itemParametersData.setItems(items);
    return itemParametersData;
  }

  private IndexCodes getIndex(final String[] header, final String code, final String id) {
    final IndexCodes indexCodes = new IndexCodes();
    int lng = header.length;
    for (int i = 0; i < lng; i++) {
      if (code.equalsIgnoreCase(header[i])) {
        indexCodes.setCodeIndex(i);
      } else if (id.equalsIgnoreCase(header[i])) {
        indexCodes.setNameIndex(i);
      }
    }
    return indexCodes;
  }

  /**
   *
   * @param file the file json content
   * @param itemFile the names map path
   * @param metaContent {@link MetaContent} list metadata
   * @param action {@link ListAction}
   * @return {link ListItemResultData}
   * @throws IOException parsing error
   */
  public ListItemResultData parseBatchJSON(final Path file, final Path itemFile,
                                           final MetaContent metaContent, final ListAction action)
      throws IOException {
    final JsonFactory jsonFactory = new JsonFactory();
    final ListItemResultData overallItemResultData = new ListItemResultData();
    try (final JsonParser jsonParser = jsonFactory.createParser(file.toFile())) {
      final JsonToken current = jsonParser.nextToken();
      if (current != JsonToken.START_ARRAY) {
        return null;
      }
      Map<String, String> properties = null;
      Map<String, String> mapProperties = null;
      if (itemFile != null) {
        properties = Utils.getPropertyFile(new FileInputStream(itemFile.toFile()));
        mapProperties = new HashMap<>();
      }
      List<ListItem> listItems;
      while (!(listItems = getItemsFromJson(jsonParser, itemFile, mapProperties, properties,
          metaContent)).isEmpty()) {
        addJSONBatch(overallItemResultData, listItems, action);
        listItems.clear();
      }
    }
    return overallItemResultData;
  }

  /**
   * Return items from json content
   * @param jsonParser {@link JsonParser}
   * @param itemFile path to names
   * @param mapProperties names mapped
   * @param properties item names
   * @param metaContent {@link MetaContent} list metadata
   * @return {@link List<ListItem>}
   * @throws IOException parsing error
   */
  public List<ListItem> getItemsFromJson(final JsonParser jsonParser, final Path itemFile,
                                         final Map<String, String> mapProperties,
                                         final Map<String, String> properties,
                                         final MetaContent metaContent) throws IOException {
    final List<ListItem> listItems = new ArrayList<>(BATCH_SIZE);
    JsonToken current;
    while ((current = jsonParser.nextToken()) != JsonToken.END_ARRAY && current != null) {
      if (current == JsonToken.START_OBJECT) {
        final ListItem item = new ListItem();
        item.setSubsets(new HashMap<>(0));
        item.setProperties(new HashMap<>(0));
        List<String> header = getHeader(item, jsonParser, itemFile, mapProperties, metaContent, properties);
        if (properties != null) {
          verifyHeaderMapping(header.toArray(new String[0]), properties, metaContent.getPropNames(),
              metaContent.getSubsets());
        }
        listItems.add(item);
        if (listItems.size() == BATCH_SIZE) {
          return listItems;
        }
      } else {
        jsonParser.skipChildren();
      }
    }
    return listItems;
  }

  private List<String> getHeader(final ListItem item, final JsonParser jsonParser,
      final Path itemFile, final Map<String, String> mapProperties, final MetaContent metaContent, final Map<String, String> properties)
      throws IOException {
    List<String> header = new ArrayList<>();
    while ((jsonParser.nextToken()) != JsonToken.END_OBJECT) {
      String name = jsonParser.getCurrentName();
      if (name == null) {
        continue;
      }
      jsonParser.nextToken();
      final String value = jsonParser.getValueAsString();
      header.add(name);
      if (!"".equalsIgnoreCase(name) && itemFile != null) {
        final String mapValue = mapProperties.get(name);
        if (mapValue != null) {
          name = mapValue;
        } else {
          final KeyValue key = getPropertyValue(properties, name);
          if (key != null) {
            name = key.getValue();
            mapProperties.put(key.getKey(), key.getValue());
          }
        }
      }
      getItemFromJson(metaContent, jsonParser, item, name, value);
    }
    return header;
  }

  private void getItemFromJson(final MetaContent metaContent, final JsonParser jp,
                               final ListItem item, final String name,
                               final String value) throws IOException {
    if (NAME.equalsIgnoreCase(name) || "".equalsIgnoreCase(name)) {
      item.setName(value);
    } else if (CODE.equalsIgnoreCase(name)) {
      item.setCode(value);
    } else if (PARENT.equalsIgnoreCase(name)) {
      item.setParent(value == null || value.trim().equals("") ? null : value);
    } else {
      String exist = Utils.findInList(name, metaContent.getPropNames());
      if (exist != null) {
        item.getProperties().put(exist, value);
      } else {
        exist = Utils.findInList(name, metaContent.getSubsets());
        if (exist != null) {
          item.getSubsets().put(exist, jp.getValueAsBoolean());
        }
      }
    }
  }

  private void addJSONBatch(final ListItemResultData overallItemResultData,
                            final List<ListItem> listItems, final ListAction action) {
    getBatchResultData(listItems, overallItemResultData, action);
    listItems.clear();
  }

  /**
   * Contains list properties and subsets
   */
  public static class MetaContent {

    final List<String> propNames;
    final List<String> subsets;

    public MetaContent(final List<String> propNames, final List<String> subsets) {
      this.propNames = propNames;
      this.subsets = subsets;
    }

    public List<String> getPropNames() {
      return propNames;
    }

    public List<String> getSubsets() {
      return subsets;
    }
  }

  private static class IndexCodes {

    int codeIndex = -1;
    int nameIndex = -1;

    public void setCodeIndex(int codeIndex) {
      this.codeIndex = codeIndex;
    }

    public void setNameIndex(int nameIndex) {
      this.nameIndex = nameIndex;
    }

    public int getCodeIndex() {
      return codeIndex;
    }

    public int getNameIndex() {
      return nameIndex;
    }
  }

  public static class KeyValue {

    final String key;
    final String value;

    public KeyValue(final String key, final String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }
}
