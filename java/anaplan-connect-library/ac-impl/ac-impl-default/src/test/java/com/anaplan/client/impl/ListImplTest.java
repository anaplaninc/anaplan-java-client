package com.anaplan.client.impl;

import static com.anaplan.client.Constants.BATCH_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.anaplan.client.ListImpl;
import com.anaplan.client.ListImpl.MetaContent;
import com.anaplan.client.Model;
import com.anaplan.client.Utils;
import com.anaplan.client.dto.ListItem;
import com.anaplan.client.dto.ListMetadata;
import com.anaplan.client.dto.ListName;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ListImplTest extends BaseTest {

  private final String listId = "123";
  private Model mockModel;

  @BeforeEach
  public void setUp() throws Exception {
    mockModel = fetchMockModel();
    ListMetadata listName = new ListMetadata();
    listName.setName("list1");
    listName.setId(listId);
    listName.setNumberedList(false);

    ListName ln = new ListName();
    ln.setId(listId);
    doReturn(ln).when(mockModel.getService())
        .getList(listId,mockModel.getWorkspace().getId(), mockModel.getId());

    doReturn(mockModel).when(mockModel.getService())
        .getModel(mockModel.getWorkspace().getId(), mockModel.getId());

    doReturn(listName).when(mockModel.getService())
            .getListMetadata(mockModel.getWorkspace().getId(), mockModel.getId(), listId);
  }

  @Test
  public void getDataFromCSV() throws IOException, CsvValidationException {
    File listItemsCSV = new File("src/test/resources/files/listItems.csv");
    ListImpl list = new ListImpl(mockModel.getService(), mockModel.getWorkspace().getId(), mockModel.getId(), listId, BATCH_SIZE);

    try (CSVReader csvReader = new CSVReader(new FileReader(listItemsCSV))) {
      final String[] header = csvReader.readNext();
      final Map<Integer, String> propMap = new HashMap<>();
      final Map<Integer, String> subsetsMap = new HashMap<>();
      final Map<String, Integer> parentMap = new HashMap<>(3);
      List<String> prop = Arrays.asList("prop1","prop2");
      List<String> subset = Arrays.asList("subset");

      final MetaContent metaContent = new MetaContent(prop, subset);
      list.parseHeader(header, null, parentMap, metaContent, propMap, subsetsMap);
      assertEquals(propMap.size(), 2);
      assertEquals(subsetsMap.size(), 1);
      assertEquals(propMap.get(3), "prop1");
      assertEquals(propMap.get(4), "prop2");

      List<ListItem> items = list.parseFileBatch(csvReader, parentMap, propMap, subsetsMap);
      assertEquals(items.size(), 5);
      assertEquals(items.get(0).getName(), "test1");
      assertEquals(items.get(3).getName(), "test4");
      items = list.parseFileBatch(csvReader, parentMap, propMap, subsetsMap);
      assertEquals(items.size(), 0);
    }

  }

  @Test
  public void getDataFromCSVWithMapping() throws IOException, CsvValidationException {
    File listItemsCSV = new File("src/test/resources/files/listItemsMapped.csv");
    File mappings = new File("src/test/resources/files/listItemMapping.properties");
    ListImpl list = new ListImpl(mockModel.getService(), mockModel.getWorkspace().getId(), mockModel.getId(), listId, BATCH_SIZE);

    try (CSVReader csvReader = new CSVReader(new FileReader(listItemsCSV))) {
      final String[] header = csvReader.readNext();
      final Map<Integer, String> propMap = new HashMap<>();
      final Map<Integer, String> subsetsMap = new HashMap<>();
      final Map<String, Integer> parentMap = new HashMap<>(3);
      List<String> prop = Arrays.asList("prop1","prop2");
      List<String> subset = Arrays.asList("subset");
      final Properties properties = new Properties();
      properties.load(new FileInputStream(mappings));
      final MetaContent metaContent = new MetaContent(prop, subset);

      final Hashtable<String, String> map = new Hashtable<>(10);
      for (final Object key : properties.keySet()){
        final String value = properties.get(key).toString();
        final String replace = key.toString();
        map.put(replace, value);
      }

      list.parseHeader(header, map, parentMap, metaContent, propMap, subsetsMap);
      assertEquals(propMap.size(), 2);
      assertEquals(subsetsMap.size(), 1);
      assertEquals(propMap.get(3), "prop1");
      assertEquals(propMap.get(4), "prop2");

      List<ListItem> items = list.parseFileBatch(csvReader, parentMap, propMap, subsetsMap);
      assertEquals(items.size(), 2);
      assertEquals(items.get(0).getName(), "test1");
      assertEquals(items.get(1).getName(), "test2");

    }
  }

  @Test
  public void getDataFromJSON() throws IOException {
    File listItemsJSON = new File("src/test/resources/files/listItems.json");
    ListImpl list = new ListImpl(mockModel.getService(), mockModel.getWorkspace().getId(), mockModel.getId(), listId, BATCH_SIZE);

    final JsonFactory jsonFactory = new JsonFactory();
    try(final JsonParser jsonParser = jsonFactory.createParser(listItemsJSON)) {
      jsonParser.nextToken();
      final List<String> propNames = new ArrayList<>();
      propNames.add("prop1");
      propNames.add("prop2");

      final List<String> subsets = new ArrayList<>();
      subsets.add("subset");
      final MetaContent metaContent = new MetaContent(propNames, subsets);
      List<ListItem> listItems = list.getItemsFromJson(jsonParser, null, null, null, metaContent);
      List<ListItem> expectedListItems = getExpectedList();

      assertEquals(listItems.size(), expectedListItems.size());
      for (int i = 0; i < listItems.size(); i++) {
        assertListItemsEqual(listItems.get(i), expectedListItems.get(i));
      }
    }
  }

  @Test
  public void getDataFromJSONWithMapping() throws IOException {
    File listItemsJSON = new File("src/test/resources/files/listItemsMapped.json");
    File mappings = new File("src/test/resources/files/listItemMapping.properties");
    ListImpl list = new ListImpl(mockModel.getService(), mockModel.getWorkspace().getId(), mockModel.getId(), listId, BATCH_SIZE);
    final JsonFactory jsonFactory = new JsonFactory();
    try(final JsonParser jsonParser = jsonFactory.createParser(listItemsJSON)) {
      jsonParser.nextToken();
      final List<String> propNames = new ArrayList<>();
      propNames.add("prop1");
      propNames.add("prop2");

      final List<String> subsets = new ArrayList<>();
      subsets.add("subset");
      final MetaContent metaContent = new MetaContent(propNames, subsets);
      final Map<String, String> mapProperties = new HashMap<>();
      Map<String, String> properties = Utils.getPropertyFile(new FileInputStream(mappings));
      List<ListItem> listItems = list.getItemsFromJson(jsonParser, mappings.toPath(), mapProperties, properties, metaContent);
      List<ListItem> expectedListItems = getExpectedList();

      assertEquals(listItems.size(), expectedListItems.size());
      for (int i = 0; i < listItems.size(); i++) {
        assertListItemsEqual(listItems.get(i), expectedListItems.get(i));
      }
    }

  }

  private List<ListItem> getExpectedList() {
    List<ListItem> expectedList = new ArrayList<>();
    ListItem listItem1 = new ListItem();
    listItem1.setName("test1");
    listItem1.setCode("code1");
    Map<String, String> props1 = new HashMap<>(2);
    props1.put("prop1", "value for prop1");
    props1.put("prop2", "10");
    listItem1.setProperties(props1);
    Map<String, Boolean> subsets1 = new HashMap<>(1);
    subsets1.put("subset", true);
    listItem1.setSubsets(subsets1);

    ListItem listItem2 = new ListItem();
    listItem2.setName("test2");
    listItem2.setCode("code2");
    Map<String, String> props2 = new HashMap<>(2);
    props2.put("prop1", "multiline\nstring:8('^&\"\nspecialcharacters");
    props2.put("prop2", "5");
    listItem2.setProperties(props2);
    Map<String, Boolean> subsets2 = new HashMap<>(1);
    subsets2.put("subset", false);
    listItem2.setSubsets(subsets2);

    expectedList.add(listItem1);
    expectedList.add(listItem2);
    return expectedList;
  }

  private ListImpl.MetaContent getMetaContent() {
    List<String> props = new ArrayList<>(2);
    props.add("prop1");
    props.add("prop2");
    List<String> subsets = new ArrayList<>(1);
    subsets.add("subset");
    ListImpl.MetaContent metaContent = new ListImpl.MetaContent(props, subsets);
    return metaContent;
  }

  private void assertListItemsEqual(ListItem listItem1, ListItem listItem2) {
    assertEquals(listItem1.getId(), listItem2.getId());
    assertEquals(listItem1.getName(), listItem2.getName());
    assertEquals(listItem1.getCode(), listItem2.getCode());
    assertEquals(listItem1.getParent(), listItem2.getParent());
    assertEquals(listItem1.getParentId(), listItem2.getParentId());
    assertEquals(listItem1.getListId(), listItem2.getListId());
    assertEquals(listItem1.getListName(), listItem2.getListName());
    assertEquals(listItem1.getProperties(), listItem2.getProperties());
    assertEquals(listItem1.getSubsets(), listItem2.getSubsets());
  }
}
