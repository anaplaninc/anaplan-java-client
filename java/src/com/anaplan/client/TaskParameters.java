//   Copyright 2012 Anaplan Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
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

import com.anaplan.client.serialization.TypeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
  * Run-time parameters to be used when starting a task on the server.
  * @since 1.3
  */
public class TaskParameters {

    static class Data {
        String localeName;
        List<ConnectorParameterData> connectorParameters;
        List<MappingParameterData> mappingParameters;
    }
    static class ConnectorParameterData {
        String sourceIdOrType;
        String parameterId;
        String value;
    }
    static class MappingParameterData {
        String importId;
        String entityType;
        String entityName;
    }
    static final TypeWrapper<Data> DATA_TYPE = new TypeWrapper<Data>() {};

    Data data = new Data();

    /**
      * Enumerate the known types of data source
      * @since 1.3
      */
    public static enum SourceType {
        /** A file uploaded, initially through the browser, to the server. */
        UPLOADED_FILE,
        /** A view in another model in the same workspace. */
        MODEL_OBJECT,
        /** A SOQL query to be run on a Salesforce.com instance. */
        SALESFORCE_API;
    }

    /**
      * Enumerate the known parameters that can be set for a data source
      * @since 1.3
      */
    public static enum ConnectorParameter {
        /** Instance of third-party system to connect to. */
        INSTANCE,
        /** User ID to connect to third-party system with. */
        USERID,
        /** Password to connect to third-party system with. */
        PASSWORD,
        /** Additional security token for third-party system. */
        SECURITY_TOKEN;
    }

    /**
      * Default constructor.
      * The locale is initially set to the default locale
      * (java.util.Locale.getDefault()).
      */
    public TaskParameters() {
        data.localeName = Locale.getDefault().toString();
    }

    /**
      * Set the locale to be used.
      * @param language the ISO-639 language code
      * @param country the ISO-3166 country code
      */
    public void setLocale(String language, String country) {
        if (language == null) {
            data.localeName = country.toUpperCase();
        } else if (country == null) {
            data.localeName = language.toLowerCase();
        } else {
            data.localeName = language.toLowerCase() + "_" + country.toUpperCase();
        }
    }
    /**
      * Add a connector parameter.
      * @param parameterId a string identifying the parameter to be set.
      * @param parameterValue the parameter value.
      */
    public void addConnectorParameter(String parameterId, String parameterValue) {
        addConnectorParameter((String) null, parameterId, parameterValue);
    }
    /**
      * Add a connector parameter.
      * @param connectorParameter a ConnectorParameter identifying the parameter to be set.
      * @param parameterValue the parameter value.
      */
    public void addConnectorParameter(ConnectorParameter connectorParameter, String parameterValue) {
        addConnectorParameter((String) null, connectorParameter.name(), parameterValue);
    }
    /**
      * Add a connector parameter, specific to all sources having the specified type.
      * @param sourceType the type of the data sources to which this applies.
      * @param parameterId a string identifying the parameter to be set.
      * @param parameterValue the parameter value.
      */
    public void addConnectorParameter(SourceType sourceType, String parameterId, String parameterValue) {
        addConnectorParameter(sourceType.name(), parameterId, parameterValue);
    }
    /**
      * Add a connector parameter, specific to all sources having the specified type.
      * @param sourceType the type of the data sources to which this applies.
      * @param connectorParameter a ConnectorParameter identifying the parameter to be set.
      * @param parameterValue the parameter value.
      */
    public void addConnectorParameter(SourceType sourceType, ConnectorParameter connectorParameter, String parameterValue) {
        addConnectorParameter(sourceType.name(), connectorParameter.name(), parameterValue);
    }
    /**
      * Add a connector parameter, specific to a either single data source or
      * all sources having the specified type.
      * @param sourceIdOrType the name or ID of the data source to which this applies; or a type of data source. The SourceType enumeration contains a list of identifiers that is complete at the time of release of Anaplan Connect; others may be added at the server side, however.
      * @param connectorParameter a ConnectorParameter identifying the parameter to be set.
      * @param parameterValue the parameter value.
      */
    public void addConnectorParameter(String sourceIdOrType, ConnectorParameter connectorParameter, String parameterValue) {
        addConnectorParameter(sourceIdOrType, connectorParameter.name(), parameterValue);
    }
    /**
      * Add a connector parameter, specific to a either single data source or
      * all sources having the specified type.
      * @param sourceIdOrType the name or ID of the data source to which this applies; or a type of data source. The SourceType enumeration contains a list of identifiers that is complete at the time of release of Anaplan Connect; others may be added at the server side, however.
      * @param parameterId a string identifying the parameter to be set.
      * @param parameterValue the parameter value.
      */
    public void addConnectorParameter(String sourceIdOrType, String parameterId, String parameterValue) {
        if (data.connectorParameters == null) {
            data.connectorParameters = new ArrayList<ConnectorParameterData>();
        }
        ConnectorParameterData connectorParameterData = new ConnectorParameterData();
        connectorParameterData.sourceIdOrType = sourceIdOrType;
        connectorParameterData.parameterId = parameterId.toUpperCase();
        connectorParameterData.value = parameterValue;
        data.connectorParameters.add(connectorParameterData);
    }
    /**
      * Add a mapping parameter.
      * @param entityType the name of the dimension, mapped to prompt, to which this parameter pertains
      * @param entityName the value to be supplied
      */
    public void addMappingParameter(String entityType, String entityName) {
        addMappingParameter(null, entityType, entityName);
    }
    /**
      * Add a mapping parameter.
      * @param importId the ID or name of the import to which this applies.
      * @param entityType the name of the dimension, mapped to prompt, to which this parameter pertains
      * @param entityName the value to be supplied
      */
    public void addMappingParameter(String importId, String entityType, String entityName) {
        if (data.mappingParameters == null) {
            data.mappingParameters = new ArrayList<MappingParameterData>();
        }
        MappingParameterData mappingParameterData = new MappingParameterData();
        mappingParameterData.importId = importId;
        mappingParameterData.entityType = entityType;
        mappingParameterData.entityName = entityName;
        data.mappingParameters.add(mappingParameterData);
    }
}

