package com.anaplan.client.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.io.Serializable;
import java.util.List;

@JsonRootName(value = "metadata")
public class ListMetadata implements Serializable {

  private String id;
  private String name;
  private List<ListMetadataProperty> properties;
  private Boolean hasSelectiveAccess;
  private List<ListSubset> subsets;
  private Boolean productionData;
  private String managedBy;
  private Boolean numberedList;
  private Boolean useTopLevelAsPageDefault;
  private Integer itemCount;
  private Boolean workflowEnabled;
  private Integer permittedItems;
  private String usedInAppliesTo;
  private String topLevelItem;
  private String category;

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }
  public String getTopLevelItem() {
    return topLevelItem;
  }

  public void setTopLevelItem(String topLevelItem) {
    this.topLevelItem = topLevelItem;
  }

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

  public List<ListMetadataProperty> getProperties() {
    return properties;
  }

  public void setProperties(List<ListMetadataProperty> properties) {
    this.properties = properties;
  }

  public Boolean getHasSelectiveAccess() {
    return hasSelectiveAccess;
  }

  public void setHasSelectiveAccess(Boolean hasSelectiveAccess) {
    this.hasSelectiveAccess = hasSelectiveAccess;
  }

  public List<ListSubset> getSubsets() {
    return subsets;
  }

  public void setSubsets(List<ListSubset> subsets) {
    this.subsets = subsets;
  }

  public Boolean getProductionData() {
    return productionData;
  }

  public void setProductionData(Boolean productionData) {
    this.productionData = productionData;
  }

  public String getManagedBy() {
    return managedBy;
  }

  public void setManagedBy(String managedBy) {
    this.managedBy = managedBy;
  }

  public Boolean getNumberedList() {
    return numberedList;
  }

  public void setNumberedList(Boolean numberedList) {
    this.numberedList = numberedList;
  }

  public Boolean getUseTopLevelAsPageDefault() {
    return useTopLevelAsPageDefault;
  }

  public void setUseTopLevelAsPageDefault(Boolean useTopLevelAsPageDefault) {
    this.useTopLevelAsPageDefault = useTopLevelAsPageDefault;
  }

  public Integer getItemCount() {
    return itemCount;
  }

  public void setItemCount(Integer itemCount) {
    this.itemCount = itemCount;
  }

  public Boolean getWorkflowEnabled() {
    return workflowEnabled;
  }

  public void setWorkflowEnabled(Boolean workflowEnabled) {
    this.workflowEnabled = workflowEnabled;
  }

  public Integer getPermittedItems() {
    return permittedItems;
  }

  public void setPermittedItems(Integer permittedItems) {
    this.permittedItems = permittedItems;
  }

  public String getUsedInAppliesTo() {
    return usedInAppliesTo;
  }

  public void setUsedInAppliesTo(String usedInAppliesTo) {
    this.usedInAppliesTo = usedInAppliesTo;
  }
}
