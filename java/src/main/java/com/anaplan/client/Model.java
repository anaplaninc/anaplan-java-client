//   Copyright 2011, 2012 Anaplan Inc.
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

import com.anaplan.client.dto.ModelData;
import com.anaplan.client.dto.ServerFileData;
import com.anaplan.client.dto.responses.ActionsResponse;
import com.anaplan.client.dto.responses.ExportsResponse;
import com.anaplan.client.dto.responses.ImportsResponse;
import com.anaplan.client.dto.responses.ModulesResponse;
import com.anaplan.client.dto.responses.ProcessesResponse;
import com.anaplan.client.dto.responses.ServerFileResponse;
import com.anaplan.client.dto.responses.ServerFilesResponse;
import com.anaplan.client.ex.ActionsNotFoundException;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.CreateImportDatasourceError;
import com.anaplan.client.ex.ExportsNotFoundException;
import com.anaplan.client.ex.ImportsNotFoundException;
import com.anaplan.client.ex.ModulesNotFoundException;
import com.anaplan.client.ex.ProcessesNotFoundException;
import com.anaplan.client.ex.ServerFilesNotFoundException;
import com.anaplan.client.transport.Paginator;
import feign.FeignException;

/**
 * An Anaplan model.
 */
public class Model extends AnaplanApiClientObject {

    private final Workspace workspace;
    private final ModelData data;

    Model(Workspace workspace, ModelData data) {
        super(workspace.getService());
        this.workspace = workspace;
        this.data = data;
    }

    final Workspace getWorkspace() {
        return workspace;
    }

    /**
     * Get the identifier of the model.
     *
     * @return The identifier (GUID) for this model
     */
    public String getId() {
        return data.getId();
    }

    /**
     * Retrieve the list of available modules.
     *
     * @return A list of the available modules within this model
     * @since 1.1
     */
    public Iterable<Module> getModules() throws AnaplanAPIException {
        try {
            Model self = this;
            return new Paginator<Module>() {

                @Override
                public Module[] getPage(int offset) {
                    ModulesResponse response = getApi().getModules(workspace.getId(), getId(), offset);
                    setPageInfo(response.getMeta().getPaging());
                    if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                        return response.getItem()
                                .stream()
                                .map(moduleData -> new Module(self, moduleData))
                                .toArray(Module[]::new);
                    } else {
                        return new Module[]{};
                    }
                }
            };
        } catch (FeignException e) {
            throw new ModulesNotFoundException(getId(), e);
        }
    }

    /**
     * Retrieve a specific module.
     *
     * @param identifier The name, code or id for the module
     * @return The module, or null if no such object exists
     * @since 1.1
     */
    public Module getModule(String identifier) throws AnaplanAPIException {
        for (Module module : getModules()) {
            if (identifier.equals(module.getId())
                    || identifier.equalsIgnoreCase(module.getCode())
                    || identifier.equalsIgnoreCase(module.getName())) {
                return module;
            }
        }
        return null;
    }

    /**
     * Retrieve the list of available server files.
     *
     * @return A list of the available server files within this model
     */
    public Iterable<ServerFile> getServerFiles() throws AnaplanAPIException {
        try {
            Model self = this;
            return new Paginator<ServerFile>() {

                @Override
                public ServerFile[] getPage(int offset) {
                    ServerFilesResponse response = getApi().getServerFiles(workspace.getId(), getId(), offset);
                    setPageInfo(response.getMeta().getPaging());
                    if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                        return response.getItem()
                                .stream()
                                .map(serverFileData -> new ServerFile(self, serverFileData))
                                .toArray(ServerFile[]::new);
                    } else {
                        return new ServerFile[]{};
                    }
                }
            };
        } catch (FeignException e) {
            throw new ServerFilesNotFoundException(getId(), e);
        }
    }

    /**
     * Retrieve a specific server file.
     *
     * @param identifier The name, code or id for the server file
     * @return The server file, or null if no such object exists
     */
    public ServerFile getServerFile(String identifier)
            throws AnaplanAPIException {
        for (ServerFile serverFile : getServerFiles()) {
            if (identifier.equals(serverFile.getId())
                    || identifier.equalsIgnoreCase(serverFile.getCode())
                    || identifier.equalsIgnoreCase(serverFile.getName())) {
                return serverFile;
            }
        }
        return null;
    }

    /**
     * Create a new file-backed import data source.
     *
     * @param name   The name of the new data source
     * @param origin An origin string to help users identify the system that
     *               created the data. This may be null.
     * @return The newly-created server file
     * @since 1.2
     */
    public ServerFile createServerFileImportDataSource(String name, String origin) throws AnaplanAPIException {
        ServerFileData requestData = new ServerFileData();
        requestData.setName(name);
        requestData.setChunkCount(-1);
        requestData.setOrigin(origin);

        ServerFileResponse response = getApi().createImportDataSource(
                workspace.getId(),
                getId(),
                name,
                requestData);
        if (response == null || response.getItem() == null) {
            throw new CreateImportDatasourceError(name);
        }
        return new ServerFile(this, response.getItem());
    }

    /**
     * Retrieve the list of available imports.
     *
     * @return A list of the available imports within this model
     */
    public Iterable<Import> getImports() throws AnaplanAPIException {
        try {
            Model self = this;
            return new Paginator<Import>() {
                @Override
                public Import[] getPage(int offset) {
                    ImportsResponse response = getApi().getImports(workspace.getId(), getId(), offset);
                    setPageInfo(response.getMeta().getPaging());
                    if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                        return response.getItem()
                                .stream()
                                .map(importData -> new Import(self, importData))
                                .toArray(Import[]::new);
                    } else {
                        return new Import[]{};
                    }
                }
            };
        } catch (FeignException e) {
            throw new ImportsNotFoundException(getId(), e);
        }
    }

    /**
     * Retrieve a specific import.
     *
     * @param identifier The name, code or id for the import
     * @return The Import object
     */
    public Import getImport(String identifier) throws AnaplanAPIException {
        for (Import serverImport : getImports()) {
            if (identifier.equals(serverImport.getId())
                    || identifier.equalsIgnoreCase(serverImport.getCode())
                    || identifier.equalsIgnoreCase(serverImport.getName())) {
                return serverImport;
            }
        }
        return null;
    }

    /**
     * Retrieve the list of available exports.
     *
     * @return A list of the available exports within this model
     */
    public Iterable<Export> getExports() throws AnaplanAPIException {
        try {
            Model self = this;
            return new Paginator<Export>() {
                @Override
                public Export[] getPage(int offset) {
                    ExportsResponse response = getApi().getExports(workspace.getId(), getId(), offset);
                    setPageInfo(response.getMeta().getPaging());
                    if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                        return response.getItem()
                                .stream()
                                .map(exportData -> new Export(self, exportData))
                                .toArray(Export[]::new);
                    } else {
                        return new Export[]{};
                    }
                }
            };
        } catch (FeignException e) {
            throw new ExportsNotFoundException(getId(), e);
        }
    }

    /**
     * Retrieve a specific export.
     *
     * @param identifier The name, code or id for the export
     * @return The Export object
     */
    public Export getExport(String identifier) throws AnaplanAPIException {
        for (Export serverExport : getExports()) {
            if (identifier.equals(serverExport.getId())
                    || identifier.equalsIgnoreCase(serverExport.getCode())
                    || identifier.equalsIgnoreCase(serverExport.getName())) {
                return serverExport;
            }
        }
        return null;
    }

    /**
     * Retrieve a list of available actions.
     *
     * @return A list of the available actions within this model
     * @since 1.1
     */
    public Iterable<Action> getActions() throws AnaplanAPIException {
        try {
            Model self = this;
            return new Paginator<Action>() {
                @Override
                public Action[] getPage(int offset) {
                    ActionsResponse response = getApi().getActions(workspace.getId(), getId(), offset);
                    setPageInfo(response.getMeta().getPaging());
                    if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                        return response.getItem()
                                .stream()
                                .map(actionData -> new Action(self, actionData))
                                .toArray(Action[]::new);
                    } else {
                        return new Action[]{};
                    }
                }
            };
        } catch (FeignException e) {
            throw new ActionsNotFoundException(getId(), e);
        }
    }

    /**
     * Retrieve a specific action.
     *
     * @param identifier The name, code or id for the action
     * @return The Action object
     * @since 1.1
     */
    public Action getAction(String identifier) throws AnaplanAPIException {
        for (Action serverAction : getActions()) {
            if (identifier.equals(serverAction.getId())
                    || identifier.equalsIgnoreCase(serverAction.getCode())
                    || identifier.equalsIgnoreCase(serverAction.getName())) {
                return serverAction;
            }
        }
        return null;
    }

    /**
     * Retrieve a list of available processes.
     *
     * @return A list of the available processes within this model
     * @since 1.3
     */
    public Iterable<Process> getProcesses() throws AnaplanAPIException {
        try {
            Model self = this;
            return new Paginator<Process>() {
                @Override
                public Process[] getPage(int offset) {
                    ProcessesResponse response = getApi().getProcesses(workspace.getId(), getId(), offset);
                    setPageInfo(response.getMeta().getPaging());
                    if (getPageInfo().getCurrentPageSize() > 0 && response.getItem() != null) {
                        return response.getItem()
                                .stream()
                                .map(processData -> new Process(self, processData))
                                .toArray(Process[]::new);
                    } else {
                        return new Process[]{};
                    }
                }
            };
        } catch (FeignException e) {
            throw new ProcessesNotFoundException(getId(), e);
        }
    }

    /**
     * Retrieve a specific process.
     *
     * @param identifier The name, code or id for the process
     * @return The Process object
     * @since 1.3
     */
    public Process getProcess(String identifier) throws AnaplanAPIException {
        for (Process serverProcess : getProcesses()) {
            if (identifier.equals(serverProcess.getId())
                    || identifier.equalsIgnoreCase(serverProcess.getCode())
                    || identifier.equalsIgnoreCase(serverProcess.getName())) {
                return serverProcess;
            }
        }
        return null;
    }
}
