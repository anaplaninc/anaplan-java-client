package com.anaplan.client.dto;

import java.util.Map;

public class ListItem {

  private String id;
  private String name;
  private String code;
  private String parent;
  private String parentId;
  private String listId;
  private String listName;
  private Boolean write;
  private Boolean read;
  private Map<String, Boolean> subsets;
  private Map<String, String> properties;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

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

}
