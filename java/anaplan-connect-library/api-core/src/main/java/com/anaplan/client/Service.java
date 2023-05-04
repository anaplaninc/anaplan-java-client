//   Copyright 2011, 2012 Anaplan Inc.
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

import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.auth.Authenticator;
import com.anaplan.client.dto.ListItem;
import com.anaplan.client.dto.ListMetadata;
import com.anaplan.client.dto.ListName;
import com.anaplan.client.dto.ModelData;
import com.anaplan.client.dto.ModuleData;
import com.anaplan.client.dto.ViewData;
import com.anaplan.client.dto.responses.ListItemsResponse;
import com.anaplan.client.dto.responses.ListMetadataResponse;
import com.anaplan.client.dto.responses.ListNamesResponse;
import com.anaplan.client.dto.responses.ModelResponse;
import com.anaplan.client.dto.responses.ModelsResponse;
import com.anaplan.client.dto.responses.ModulesResponse;
import com.anaplan.client.dto.responses.ViewsResponse;
import com.anaplan.client.dto.responses.WorkspaceResponse;
import com.anaplan.client.dto.responses.WorkspacesResponse;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.exceptions.ListItemsNotFoundException;
import com.anaplan.client.exceptions.ListMetadataNotFoundException;
import com.anaplan.client.exceptions.ListNamesNotFoundException;
import com.anaplan.client.exceptions.ListNotFoundException;
import com.anaplan.client.exceptions.ModelNotFoundException;
import com.anaplan.client.exceptions.WorkspaceNotFoundException;
import com.anaplan.client.listwriter.ListItemFileWriter;
import com.anaplan.client.listwriter.ListMetadataCsvWriter;
import com.anaplan.client.listwriter.ListNamesCsvWriter;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.Paginator;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;


/**
 * An authenticated connection to the Anaplan API service.
 */
public class Service implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(Service.class);
  private static final URI PRODUCTION_API_ROOT;
  private static final URI PRODUCTION_AUTH_API_ROOT;

  static {
    PRODUCTION_API_ROOT = URI.create("https://api.anaplan.com/");
    PRODUCTION_AUTH_API_ROOT = URI.create("https://auth.anaplan.com/");
  }

  private final ConnectionProperties props;
  private Supplier<AnaplanAPI> apiProvider;
  private Authenticator authProvider;

  // Cached Workspace instances
  private final Map<String, Reference<Workspace>> workspaceCache = new WeakHashMap<>();

  public Service(ConnectionProperties properties, Authenticator authProvider, Supplier<AnaplanAPI> apiProvider) {
    if (properties.getApiServicesUri() == null) {
      properties.setApiServicesUri(PRODUCTION_API_ROOT);
    }
    if (properties.getAuthServiceUri() == null) {
      properties.setAuthServiceUri(PRODUCTION_AUTH_API_ROOT);
    }
    LOG.info("Initializing Service...");
    this.props = properties;
    this.authProvider = authProvider;
    this.apiProvider = apiProvider;
  }

  public Supplier<AnaplanAPI> getApiProvider() {
    return apiProvider;
  }

  public void setApiProvider(Supplier<AnaplanAPI> apiProvider) {
    this.apiProvider = apiProvider;
  }

  public Authenticator getAuthProvider() {
    return authProvider;
  }

  public void setAuthProvider(Authenticator authProvider) {
    this.authProvider = authProvider;
  }

  /**
   * Authenticates using provided credentials
   */
  public void authenticate() {
    Preconditions.checkNotNull(props.getApiCredentials(), "No service credentials present to authenticate with.");
    authProvider.authToken();
  }

  /**
   * Retrieves the list of available workspaces.
   *
   * @return The list of workspaces this user has access to
   * @throws AnaplanAPIException an error occurred.
   */

  public Iterable<Workspace> getWorkspaces() throws AnaplanAPIException {
    Service self = this;
    return new Paginator<Workspace>() {

      @Override
      public Workspace[] getPage(int offset) {
        WorkspacesResponse response = apiProvider.get().getWorkspaces(offset);
        setPageInfo(response.getMeta().getPaging());
        if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
          return response.getItem()
              .stream()
              .map(workspaceData -> {
                Workspace workspace = new Workspace(self, workspaceData);
                cacheWorkspace(workspace);
                return workspace;
              })
              .toArray(Workspace[]::new);
        } else {
          return new Workspace[]{};
        }
      }
    };
  }

  /**
   * Retrieves the list of available models for a user.
   *
   * @return The list of models this user has access to.
   * @throws AnaplanAPIException an error occurred.
   */

  public Iterable<ModelData> getModels() throws AnaplanAPIException {
    return new Paginator<ModelData>() {

      @Override
      public ModelData[] getPage(int offset) {
        ModelsResponse response = apiProvider.get().getModels(offset);
        setPageInfo(response.getMeta().getPaging());
        if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
          return response.getItem().stream().toArray(ModelData[]::new);
        } else {
          return new ModelData[]{};
        }
      }
    };
  }

  /**
   * Retrieves the available modules for a user workspace/model
   *
   * @return The list of modules
   * @throws AnaplanAPIException an error occurred
   */
  public Iterable<ModuleData> getModules(String workspaceId, String modelId)
      throws AnaplanAPIException {
    return new Paginator<ModuleData>() {

      @Override
      public ModuleData[] getPage(int offset) {
        ModulesResponse response = apiProvider.get().getModules(workspaceId, modelId, offset);
        setPageInfo(response.getMeta().getPaging());
        if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
          return response.getItem().stream().toArray(ModuleData[]::new);
        } else {
          return new ModuleData[]{};
        }
      }
    };
  }

  /**
   * Retrieves the available views for a user workspace/model/module
   *
   * @return The list of views
   * @throws AnaplanAPIException an error occurred
   */
  public Iterable<ViewData> getViews(String modelId, String moduleId)
      throws AnaplanAPIException {
    return new Paginator<ViewData>() {

      @Override
      public ViewData[] getPage(int offset) {
        ViewsResponse response = apiProvider.get().getViews(modelId, moduleId, offset);
        setPageInfo(response.getMeta().getPaging());
        if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
          return response.getItem().stream().toArray(ViewData[]::new);
        } else {
          return new ViewData[]{};
        }
      }
    };
  }

  private void cacheWorkspace(Workspace workspace) {
    // Cache workspace by both name and ID. This is not ideal but needed to support `getWorkspace` method as
    // it can take either name or ID as parameter
    workspaceCache.put(workspace.getId(), new WeakReference<>(workspace));
    workspaceCache.put(workspace.getName(), new WeakReference<>(workspace));
  }

  private Workspace getWorkspaceFromCache(String workspaceNameOrId) {
    // Get workspace with given name or ID from cache
    Reference<Workspace> workspaceReference = workspaceCache.get(workspaceNameOrId);
    return workspaceReference == null ? null : workspaceReference.get();
  }

  /**
   * Retrieve a reference to a workspace from its workspaceId.
   *
   * @param workspaceId The workspace ID.
   * @return The workspace; null if no such workspace exists or the user is not permitted to access the workspace.
   * @throws com.anaplan.client.exceptions.WorkspaceNotFoundException an error occurred.
   */
    public Workspace getWorkspaceById(@Nonnull String workspaceId)
        throws AnaplanAPIException {
      Workspace workspace = getWorkspaceFromCache(workspaceId);

      if (workspace == null) {
        try {
          WorkspaceResponse response = apiProvider.get().getWorkspace(workspaceId);
          workspace = new Workspace(this, response.getItem());
          cacheWorkspace(workspace);
        } catch (Exception e) {
          throw new WorkspaceNotFoundException(workspaceId);
        }
      }
      return workspace;
    }

  /**
   * Retrieve a reference to a workspace from its workspaceId or name.
   *
   * @param workspaceIdOrName The workspace ID or name of the workspace.
   * @return The workspace; null if no such workspace exists or the user is not permitted to access the workspace.
   * @throws com.anaplan.client.exceptions.WorkspaceNotFoundException an error occurred.
   */
  public Workspace getWorkspace(@Nonnull String workspaceIdOrName)
      throws AnaplanAPIException {
    Workspace workspace = getWorkspaceFromCache(workspaceIdOrName);

    if (workspace == null) {
      try {
        for (Workspace w : getWorkspaces()) {
          if (workspaceIdOrName.equals(w.getId()) || workspaceIdOrName.equalsIgnoreCase(w.getName())) {
            workspace = w;
            break;
          }
        }
      } catch (Exception e) {
        throw new WorkspaceNotFoundException(workspaceIdOrName);
      }
      if (workspace == null) {
        throw new WorkspaceNotFoundException(workspaceIdOrName);
      }
    }
    return workspace;
  }

  /**
   * Found list by name or id
   * @param listIdOrName list id or name
   * @param workspaceId the workspace id
   * @param modelId the model id
   * @return {@link ListName}
   * @throws AnaplanAPIException the error
   */
  public ListName getList(final String listIdOrName, final String workspaceId, final String modelId)
      throws AnaplanAPIException {
    final Iterable<ListName> listNames = getListNames(workspaceId, modelId);
    try {
      for (final ListName ln : listNames) {
        if (listIdOrName.equals(ln.getId()) || listIdOrName.equalsIgnoreCase(ln.getName())) {
          return ln;
        }
      }
    } catch (final Exception e) {
      throw new ListNotFoundException(listIdOrName + " list is not found on workspace:" + workspaceId + " and model: " + modelId);
    }
    throw new ListNotFoundException(
        listIdOrName + " list is not found on workspace:" + workspaceId + " and model: " + modelId);
  }

  /**
   * Locate a model on the server. An error message will be produced if the workspace or model cannot be located.
   * Note this method only supports workspace and model IDs, not names.
   *
   * @param workspaceId the ID of the workspace
   * @param modelId     the ID of the model
   * @return the model, or null if not found
   */
  public Model getModelById(@Nonnull String workspaceId, @Nonnull String modelId)
      throws ModelNotFoundException {
    Workspace workspace = getWorkspaceById(workspaceId);
    try {
      ModelResponse response = apiProvider.get().getModel(modelId);
      return new Model(workspace, response.getItem());
    } catch (Exception e) {
      throw new ModelNotFoundException(modelId, e);
    }
  }

  /**
   * Locate a model on the server. An error message will be produced if the workspace or model cannot be located.
   *
   * @param workspaceId the name or ID of the workspace
   * @param modelId     the name or ID of the model
   * @return the model, or null if not found
   * @since 1.3
   */
  public Model getModel(@Nonnull String workspaceId, @Nonnull String modelId)
      throws AnaplanAPIException {
    Workspace workspace = getWorkspace(workspaceId);
    if (workspace == null) {
      return null;
    }
    if (modelId == null || modelId.isEmpty()) {
      LOG.error("A model ID must be provided");
      return null;
    }
    Model model = null;
    for (Model m : workspace.getModels()) {
      if (modelId.equals(m.getId()) || modelId.equalsIgnoreCase(m.getName())) {
        model = m;
        break;
      }
    }
    if (model == null) {
      LOG.error("Model \"{}\" not found in workspace \"{}\"", modelId, workspaceId);
    }
    return model;
  }

  /**
   * Export list metadata to console or file
   *
   * @param fileType    CSV or JSON
   * @param fileId      The id of the file or null if the export is to the console
   * @param workspaceId The id of the workspace
   * @param modelId     The id of the model
   * @param listId      The id of the list
   */
  public void exportListMetadata(String fileType, String fileId, String workspaceId, String modelId, String listId) {
    if (workspaceId != null && modelId != null && listId != null) {
      ListName listName = getListName(workspaceId, modelId, listId);
      if (listName != null) {
        ListMetadata listMetadata = getListMetadata(workspaceId, modelId, listName.getId());
        if (fileId == null) {
          ObjectMapper objectMapper = ObjectMapperProvider.getObjectMapper().copy();
          objectMapper.setSerializationInclusion(Include.NON_ABSENT);
          try {
            final String name = listMetadata.getName();
            final String meta = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(listMetadata);
            LOG.info("List metadata - {}:\n{}",
                name, meta
                );
          } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
          }
        } else {
          exportMetaFromFile(fileId, listMetadata, fileType);
        }
      }
    }
  }

  private void exportMetaFromFile(final String fileId, final ListMetadata listMetadata, final String fileType) {
    File targetFile = new File(fileId);
    if (Constants.CSV.equalsIgnoreCase(fileType)) {
      Stream<String> lines = ListMetadataCsvWriter.getLines(listMetadata);
      ListItemFileWriter.linesToFile(listMetadata.getName(), targetFile.toPath(), lines);
    } else if (Constants.JSON.equalsIgnoreCase(fileType)) {
      ObjectMapper objectMapper = ObjectMapperProvider.getObjectMapper().copy();
      objectMapper.setSerializationInclusion(Include.NON_ABSENT);
      ListItemFileWriter
          .listToFile("List metadata - " + listMetadata.getName(), targetFile.toPath(),
              Arrays.asList(listMetadata),
              objectMapper);
    }
  }

  /**
   * Export list names to console or file
   *
   * @param fileType    CSV or JSON
   * @param fileId      The id of the file or null if the export is to the console
   * @param workspaceId The id of the workspace
   * @param modelId     The id of the model
   */
  public void exportListNames(String fileType, String fileId, String workspaceId, String modelId) {
    if (workspaceId != null && modelId != null) {
      List<ListName> listNames = StreamSupport.stream(getListNames(workspaceId, modelId).spliterator(), false)
          .collect(Collectors.toList());
      if (fileId == null) {
        final String logInfo = Utils.formatTSV("id", "name");
        LOG.info(logInfo);
        listNames
            .forEach(listName -> LOG.info(Utils.formatTSV(listName.getId(), listName.getName())));
      } else {
        File targetFile = new File(fileId);
        if (Constants.CSV.equalsIgnoreCase(fileType)) {
          ListItemFileWriter
              .linesToFile("list names", targetFile.toPath(),
                  ListNamesCsvWriter.getLines(listNames));
        } else if (Constants.JSON.equalsIgnoreCase(fileType)) {
          ObjectMapper objectMapper = ObjectMapperProvider.getObjectMapper().copy();
          objectMapper.setSerializationInclusion(Include.NON_ABSENT);
          ListItemFileWriter.listToFile("list names", targetFile.toPath(), listNames, objectMapper);
        }
      }
    }
  }

  /**
   * Export list items to console or file
   *
   * @param fileType    CSV or JSON
   * @param fileId      The id of the file or null if the export is to the console
   * @param workspaceId The id of the workspace
   * @param modelId     The id of the model
   * @param listId      The id of the list
   * @param includeAll  If all list item details should be included
   */
  public void exportListItems(String fileType, String fileId, String workspaceId, String modelId, String listId,
      boolean includeAll) throws ListNotFoundException {
    boolean isCsv = Constants.CSV.equalsIgnoreCase(fileType);
    boolean isJson = Constants.JSON.equalsIgnoreCase(fileType);
    if (!isCsv && !isJson) {
      LOG.error(
          "Only supported list items export types are csv and json, please use the get:csv or get:json option");
      return;
    }
    File targetFile = new File(fileId);
    if (workspaceId != null && modelId != null && listId != null) {
      ListName listName = getListName(workspaceId, modelId, listId);
      if (listName != null) {
        LOG.info("Started export for list {}", listName.getName());
        if (isCsv) {
          String listItemsCsv = getListItemsCsv(workspaceId, modelId, listName.getId(), includeAll);
          ListItemFileWriter.listItemToFile(listName.getName(), targetFile.toPath(), listItemsCsv);
        } else {
          ObjectMapper objectMapper = ObjectMapperProvider.getObjectMapper().copy();
          objectMapper.setSerializationInclusion(Include.NON_ABSENT);
          List<ListItem> listItems = getListItems(workspaceId, modelId, listName.getId(), includeAll);
          ListItemFileWriter
              .listToFile("List - " + listName.getName(), targetFile.toPath(), Arrays.asList(listItems), objectMapper);
        }
      } else {
        throw new ListNotFoundException(modelId);
      }
    }
  }

  /**
   * Get ListName for a list identifier
   *
   * @param workspaceId Workspace id
   * @param modelId     Model id
   * @param listId      List Id
   * @return ListName or null
   */
  public ListName getListName(String workspaceId, String modelId, String listId) {
    Iterable<ListName> listNames = getListNames(workspaceId, modelId);
    for (ListName listName : listNames) {
      if (listId.equals(listName.getId()) || listId.equals(listName.getName())) {
        return listName;
      }
    }
    return null;
  }

  /**
   * Retrieve the list metadata.
   *
   * @param workspaceId The id of the workspace
   * @param modelId     The id of the model
   * @param listId      The id of the list
   * @return The matadata for a list
   * @since 2.0
   */
  public ListMetadata getListMetadata(String workspaceId, String modelId, String listId)
      throws AnaplanAPIException {
    try {
      ListMetadataResponse response = apiProvider.get().getListMetadata(workspaceId, modelId, listId);
      return Optional.ofNullable(response)
          .map(ListMetadataResponse::getItem)
          .orElse(null);
    } catch (Exception e) {
      throw new ListMetadataNotFoundException(listId, e);
    }
  }

  /**
   * Retrieve the available list names
   *
   * @param workspaceId The id of the workspace
   * @param modelId     The id of the model
   * @return List names
   * @since 2.0
   */
  public Iterable<ListName> getListNames(String workspaceId, String modelId) throws AnaplanAPIException {
    try {
      return new Paginator<ListName>() {
        @Override
        public ListName[] getPage(int offset) {
          ListNamesResponse response = apiProvider.get()
              .getListNames(workspaceId, modelId, offset);
          setPageInfo(response.getMeta().getPaging());
          if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
            List<ListName> item = response.getItem();
            return item.stream().toArray(ListName[]::new);
          } else {
            return new ListName[]{};
          }
        }
      };
    } catch (Exception e) {
      throw new ListNamesNotFoundException(modelId, e);
    }
  }

  /**
   * Retrieve the available lists items. This is used for up to 1 mil records max.
   *
   * @param workspaceId The id of the workspace
   * @param modelId     The id of the model
   * @return A list of the available lists within this model
   * @since 2.0
   */
  public List<ListItem> getListItems(String workspaceId, String modelId, String listId,
      boolean includeAll) throws AnaplanAPIException {
    try {
      ListItemsResponse response = apiProvider.get()
          .getListItems(workspaceId, modelId, listId, includeAll);
      if (response != null && response.getItem() != null) {
        return response.getItem();
      } else {
        return Collections.emptyList();
      }
    } catch (Exception e) {
      throw new ListItemsNotFoundException(listId, e);
    }
  }

  /**
   * Retrieve the available lists items as a CSV string. This is used for up to 1 mil records max.
   *
   * @param workspaceId The id of the workspace
   * @param modelId     The id of the model
   * @return A list of the available lists within this model
   * @since 2.0
   */
  public String getListItemsCsv(String workspaceId, String modelId, String listId, boolean includeAll)
      throws AnaplanAPIException {
    try {
      return apiProvider.get().getListItemsCsv(workspaceId, modelId, listId, includeAll);
    } catch (Exception e) {
      throw new ListItemsNotFoundException(listId, e);
    }
  }

  /**
   * Release any system resources associated with this instance.
   */
  @Override
  public void close() {
    if (apiProvider != null) {
      apiProvider = null;
      authProvider = null;
    }
  }


}
