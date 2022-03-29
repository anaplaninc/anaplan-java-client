package com.anaplan.client.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Spondon Saha User: spondonsaha Date: 6/21/17 Time: 3:34 PM
 */
public class TaskParametersData implements Serializable {

  private String localeName;
  private List<ConnectorParameterData> connectorParameters;
  private List<MappingParameterData> mappingParameters;

  public String getLocaleName() {
    return localeName;
  }

  public List<ConnectorParameterData> getConnectorParameters() {
    return connectorParameters;
  }

  public List<MappingParameterData> getMappingParameters() {
    return mappingParameters;
  }

  public void setLocaleName(String localeName) {
    this.localeName = localeName;
  }

  public void setConnectorParameters(List<ConnectorParameterData> connectorParameters) {
    this.connectorParameters = connectorParameters;
  }

  public void setMappingParameters(List<MappingParameterData> mappingParameters) {
    this.mappingParameters = mappingParameters;
  }
}
