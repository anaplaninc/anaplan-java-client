package com.anaplan.client.listwriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anaplan.client.Constants;
import com.anaplan.client.Utils;
import com.anaplan.client.dto.ListItem;
import com.anaplan.client.dto.ListMetadataProperty;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ListItemTest {
  static Map<Integer, String> prop;
  static String[] columns;
  static String[] columnsFalse;
  static List<String> listProperties;
  static List<String> listNoProperties;
  static Method jdbcMethod;
  static Method method;

  @BeforeAll
  static void init() throws NoSuchMethodException {
    jdbcMethod = ListItem.class.getDeclaredMethod("getJDBCPropertiesFromMeta", Map.class, String[].class, List.class);
    jdbcMethod.setAccessible(true);
    method = ListItem.class.getDeclaredMethod("getPropertiesFromMeta", Map.class, String[].class);
    method.setAccessible(true);
    prop = new HashMap<>();
    prop.put(0,"a");
    prop.put(1, "b");
    columns = new String[]{"b", "1"};
    listProperties = new ArrayList<>();
    listProperties.add("b");
    listNoProperties = new ArrayList<>(0);
    columnsFalse = new String[]{"1", "0"};
  }

  @Test
  void testJDBCPropertyFromMeta()
      throws InvocationTargetException, IllegalAccessException {

    HashMap result = (HashMap) jdbcMethod.invoke(Map.class, prop, columns, listProperties);
    assertEquals("true", result.get("b"));
  }

  @Test
  void testPropertyFromMeta()
      throws InvocationTargetException, IllegalAccessException {

    HashMap result = (HashMap) method.invoke(Map.class, prop, columns);
    assertEquals("1", result.get("b"));
  }

  @Test
  void testPropertyFalseFromMeta()
      throws InvocationTargetException, IllegalAccessException {
    listProperties.add("b");
    HashMap result = (HashMap) jdbcMethod.invoke(Map.class, prop, columnsFalse, listProperties);
    assertEquals("false", result.get("b"));
  }

  @Test
  void testNoPropertyFromMeta()
      throws InvocationTargetException, IllegalAccessException {
    HashMap result = (HashMap) jdbcMethod.invoke(Map.class, prop, columns, listNoProperties);
    assertEquals("1", result.get("b"));
  }

  @Test
  void testInitListImpl() {
    final List<ListMetadataProperty> properties = new ArrayList<>();
    ListMetadataProperty p1 =new ListMetadataProperty();
    p1.setName("a");
    SortedMap map1 = new TreeMap();
    map1.put(Constants.DATA_TYPE, "uknown");
    p1.setFormatMetadata(map1);

    ListMetadataProperty p2 =new ListMetadataProperty();
    p2.setName("b");
    SortedMap map2 = new TreeMap();
    map2.put(Constants.DATA_TYPE, Constants.BOOLEAN);
    p2.setFormatMetadata(map2);

    properties.add(p1);
    properties.add(p2);
    List<String> result = Utils.getBooleanParams(properties);

    assertEquals("b", result.get(0));
    assertEquals(1, result.size());
  }
}