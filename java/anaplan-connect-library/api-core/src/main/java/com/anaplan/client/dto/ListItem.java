package com.anaplan.client.dto;

import com.anaplan.client.ListImpl;
import java.util.Map;
import java.util.stream.Collectors;

public class ListItem extends NamedObjectData{

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
      final Map<Integer, String> subsets, final Boolean numberedList) {
    final ListItem itemData = new ListItem();
    if (columns == null) {
      return itemData;
    }
    final Integer parentIndex = parentMap.get(ListImpl.PARENT);
    final Integer nameIndex = parentMap.get(ListImpl.NAME);
    final Integer codeIndex = parentMap.get(ListImpl.CODE);
    final Integer idIndex = parentMap.get(ListImpl.ID);
    itemData.setName(numberedList ? null : columns[nameIndex == null ? 0 : nameIndex]);
    itemData.setCode(columns[codeIndex == null ? 2 : codeIndex]);
    itemData.setParent(parentIndex == null ? null : "".equals(columns[parentIndex]) ? null : columns[parentIndex]);
    itemData.setId(idIndex == null ? null : columns[idIndex]);

    if (prop !=null) {
      Map<String, String> properties = prop.keySet().stream().filter(key -> key < columns.length)
          .collect(Collectors.toMap(prop::get, key -> columns[key], (a, b) -> b));
      itemData.setProperties(properties);
    }
    if (subsets !=null) {
      Map<String, Boolean> sub = subsets.keySet().stream().filter(key -> key < columns.length)
          .collect(
              Collectors.toMap(subsets::get, key -> (columns[key].equals("1") || Boolean.parseBoolean(columns[key])),
                      (a, b) -> b));
      itemData.setSubsets(sub);
    }
    return itemData;
  }
}
