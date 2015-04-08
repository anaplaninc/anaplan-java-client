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

import java.util.ArrayList;
import java.util.List;

import com.anaplan.client.serialization.TypeWrapper;

/**
 * An Anaplan model.
 */
public class Model extends AnaplanApiClientObject {

    // This data is passed over the wire between client and server
    static final class Data {
        String id;
        String name;

        public boolean equals(Object other) {
            if (!(other != null && other instanceof Data)) {
                return false;
            }
            Data data = (Data) other;
            return id.equals(data.id) && name.equals(data.name);
        }

        public int hashCode() {
            return id.hashCode() * 31 + name.hashCode();
        }
    }

    static final TypeWrapper<List<Data>> DATA_LIST_TYPE = new TypeWrapper<List<Data>>() {
    };

    private final Workspace workspace;
    private final Data data;

    Model(Workspace workspace, Data data) {
        super(workspace.getService());
        this.workspace = workspace;
        this.data = data;
    }

    final Workspace getWorkspace() {
        return workspace;
    }

    final String getPath() {
        return getWorkspace().getPath() + "/models/" + getId();
    }

    /**
     * Get the identifier of the model.
     * 
     * @return The identifier (GUID) for this model
     */
    public String getId() {
        return data.id;
    }

    /**
     * Get the name of the model.
     * 
     * @return The name of this model
     */
    public String getName() {
        return data.name;
    }

    /**
     * Retrieve the list of available modules.
     * 
     * @return A list of the available modules within this model
     * @since 1.1
     */
    public List<Module> getModules() throws AnaplanAPIException {
        List<Module.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/modules",
                        getSerializationHandler().getContentType()),
                Module.DATA_LIST_TYPE);
        List<Module> result = new ArrayList<Module>(response.size());
        for (Module.Data moduleData : response) {
            Module module = new Module(this, moduleData);
            result.add(module);
        }
        return result;
    }

    /**
     * Retrieve a specific module.
     * 
     * @param identifier
     *            The name, code or id for the module
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
    public List<ServerFile> getServerFiles() throws AnaplanAPIException {
        List<ServerFile.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/files",
                        getSerializationHandler().getContentType()),
                ServerFile.DATA_LIST_TYPE);
        List<ServerFile> result = new ArrayList<ServerFile>(response.size());
        for (ServerFile.Data serverFileData : response) {
            ServerFile serverFile = new ServerFile(this, serverFileData);
            result.add(serverFile);
        }
        return result;
    }

    /**
     * Retrieve a specific server file.
     * 
     * @param identifier
     *            The name, code or id for the server file
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
     * @param name
     *            The name of the new data source
     * @param origin
     *            An origin string to help users identify the system that
     *            created the data. This may be null.
     * @return The newly-created server file
     * @since 1.2
     */
    public ServerFile createServerFileImportDataSource(String name,
            String origin) throws AnaplanAPIException {
        ServerFile.Data data = new ServerFile.Data();
        data.name = name;
        data.chunkCount = -1;
        data.origin = origin;
        byte[] content = getSerializationHandler().serialize(data,
                ServerFile.DATA_TYPE);
        String contentType = getSerializationHandler().getContentType();
        content = getTransportProvider().post(getPath() + "/files/" + name,
                content, contentType, contentType);
        if (content != null) {
            data = getSerializationHandler().deserialize(content,
                    ServerFile.DATA_TYPE);
        }
        ServerFile serverFile = new ServerFile(this, data);
        return serverFile;
    }

    /**
     * Retrieve the list of available imports.
     * 
     * @return A list of the available imports within this model
     */
    public List<Import> getImports() throws AnaplanAPIException {
        List<Import.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/imports",
                        getSerializationHandler().getContentType()),
                Import.DATA_LIST_TYPE);
        List<Import> result = new ArrayList<Import>(response.size());
        for (Import.Data importData : response) {
            result.add(new Import(this, importData));
        }
        return result;
    }

    /**
     * Retrieve a specific import.
     * 
     * @param identifier
     *            The name, code or id for the import
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
    public List<Export> getExports() throws AnaplanAPIException {
        List<Export.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/exports",
                        getSerializationHandler().getContentType()),
                Export.DATA_LIST_TYPE);
        List<Export> result = new ArrayList<Export>(response.size());
        for (Export.Data exportData : response) {
            result.add(new Export(this, exportData));
        }
        return result;
    }

    /**
     * Retrieve a specific export.
     * 
     * @param identifier
     *            The name, code or id for the export
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
    public List<Action> getActions() throws AnaplanAPIException {
        List<Action.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/actions",
                        getSerializationHandler().getContentType()),
                Action.DATA_LIST_TYPE);
        List<Action> result = new ArrayList<Action>(response.size());
        for (Action.Data actionData : response) {
            result.add(new Action(this, actionData));
        }
        return result;
    }

    /**
     * Retrieve a specific action.
     * 
     * @param identifier
     *            The name, code or id for the action
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
    public List<Process> getProcesses() throws AnaplanAPIException {
        List<Process.Data> response = getSerializationHandler().deserialize(
                getTransportProvider().get(getPath() + "/processes",
                        getSerializationHandler().getContentType()),
                Process.DATA_LIST_TYPE);
        List<Process> result = new ArrayList<Process>(response.size());
        for (Process.Data actionData : response) {
            result.add(new Process(this, actionData));
        }
        return result;
    }

    /**
     * Retrieve a specific process.
     * 
     * @param identifier
     *            The name, code or id for the process
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

    /**
     * Runs a refresh operation with the specified request. The formats of the
     * request parameter and result are subject to change without notice; it is
     * therefore recommended that this method is not used from anything other
     * than specific Anaplan-supported software.
     * 
     * @param request
     *            The serialized request. The structure of this is not specified
     *            here.
     * @return A result from the processed request. The structure of this is not
     *         specified here.
     * @since 1.1
     */
    public byte[] doRefresh(byte[] request) throws AnaplanAPIException {
        String contentType = getSerializationHandler().getContentType();
        byte[] response = getTransportProvider().post(getPath() + "/refresh",
                request, contentType, contentType);
        return response;
    }
}
