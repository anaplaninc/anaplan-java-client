package com.anaplan.client.dto;

import com.anaplan.client.Constants;
import com.anaplan.client.ListImpl;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListItem extends NamedObjectData implements Serializable {

  private String parent;
  private String parentId;
  private String listId;
  private String listName;
  private Boolean write;
  private Boolean read;
  private Map<String, Boolean> subsets;
  private Map<String, String> properties;

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getListId() {
    return listId;
  }

  public void setListId(String listId) {
    this.listId = listId;
  }

  public String getListName() {
    return listName;
  }

  public void setListName(String listName) {
    this.listName = listName;
  }

  public Boolean isWrite() {
    return write;
  }

  public void setWrite(Boolean write) {
    this.write = write;
  }

  public Boolean isRead() {
    return read;
  }

  public void setRead(Boolean read) {
    this.read = read;
  }

  public Map<String, Boolean> getSubsets() {
    return subsets;
  }

  public void setSubsets(Map<String, Boolean> subsets) {
    this.subsets = subsets;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /**
   *
   * @param columns source header
   * @param parentMap header mapped
   * @param prop properties order mapped
   * @param subsets subsets order mapped
   * @return {@link ListItem}
   */
  public static ListItem mapCSVToItemData(final String[] columns, final Map<String, Integer> parentMap, final Map<Integer, String> prop,
      final Map<Integer, String> subsets, final List<String> listMetadata, final boolean isNumberedList, final boolean fromJDBC) {
    final ListItem itemData = new ListItem();
    if (columns == null) {
      return itemData;
    }
    final Integer parentIndex = parentMap.get(ListImpl.PARENT);
    final Integer nameIndex = parentMap.get(ListImpl.NAME);
    final Integer codeIndex = parentMap.get(ListImpl.CODE);
    final Integer idIndex = parentMap.get(ListImpl.ID);
    final int index = nameIndex == null ? 0 : nameIndex;
    final String name = Boolean.TRUE.equals(isNumberedList) ? null : columns[index];
    itemData.setName(name);
    itemData.setCode(columns[codeIndex == null ? 2 : codeIndex]);
    final String parent = parentIndex == null || "".equals(columns[parentIndex]) ? null : columns[parentIndex];
    itemData.setParent(parent);
    itemData.setId(idIndex == null ? null : columns[idIndex]);

    if (prop != null) {
      if (fromJDBC) {
        itemData.setProperties(getJDBCPropertiesFromMeta(prop, columns, listMetadata));
      } else {
        itemData.setProperties(getPropertiesFromMeta(prop, columns));
      }
    }
    if (subsets != null) {
      Map<String, Boolean> sub = subsets.keySet().stream().filter(key -> key < columns.length)
          .collect(
              Collectors.toMap(subsets::get, key -> (columns[key].equals(Constants.ONE) || Boolean.parseBoolean(columns[key])),
                      (a, b) -> b));
      itemData.setSubsets(sub);
    }
    return itemData;
  }

  private static Map<String, String> getJDBCPropertiesFromMeta(final Map<Integer, String> prop,
      final String[] columns, final List<String> listProperties) {
    if (prop.isEmpty()) {
      return new HashMap<>(0);
    }
    final Map<String, String> properties = new HashMap<>();
    for (Iterator<Integer> iterator = prop.keySet().iterator(); iterator.hasNext();) {
      Integer key = iterator.next();
      if (key >= columns.length) {
        continue;
      }
      final String propName = prop.get(key);
      boolean isBoolean = listProperties.contains(propName);
      String propValue = columns[key];
      //For JDBC source properties we convert 1 and 0 values to true and false if the property is boolean type
      if (isBoolean) {
        listProperties.remove(propName);
        if (Constants.ONE.equals(propValue)) {
          propValue = Boolean.TRUE.toString();
        } else if (Constants.ZERO.equals(propValue)) {
          propValue = Boolean.FALSE.toString();
        }
      }
      properties.put(propName, propValue);
    }
    return properties;
  }

  private static Map<String, String> getPropertiesFromMeta(final Map<Integer, String> prop,
      final String[] columns) {
    if (prop.isEmpty()) {
      return new HashMap<>(0);
    }
    final Map<String, String> properties = new HashMap<>();
    for (Iterator<Integer> iterator = prop.keySet().iterator(); iterator.hasNext();) {
      Integer key = iterator.next();
      if (key >= columns.length) {
        continue;
      }
      final String propName = prop.get(key);
      String propValue = columns[key];
      properties.put(propName, propValue);
    }
    return properties;
  }
}
