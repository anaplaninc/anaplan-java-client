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

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.Properties;
import java.net.URI;

/**
 * A command-line interface to the Anaplan Connect API library. Running the
 * program with no arguments will display the available options. This class also
 * contains serveral static convenience methods that may be useful by other
 * alternative main-method implementations - these should extend this class to
 * gain access to them as they have protected access.
 */

public abstract class Program {
    private static int debugLevel = 0;
    private static boolean quiet = false;
    private static Service service = null;
    private static URI serviceLocation = null;
    private static URI proxyLocation = null;
    private static boolean proxyLocationSet = false;
    private static String username = null;
    private static String passphrase = null;
    private static String proxyUsername = null;
    private static boolean proxyUsernameSet = false;
    private static String proxyPassphrase = null;
    private static String workspaceId = null;
    private static String modelId = null;
    private static String moduleId = null;
    private static String viewId = null;
    private static String fileId = null;
    private static String importId = null;
    private static String exportId = null;
    private static String actionId = null;
    private static String processId = null;
    private static TaskResult lastResult = null;
    private static boolean somethingDone = false;
    private static String jdbcUrl = null;
    private static Properties jdbcProps = null;
    private static Integer jdbcFetchSize = null;
    private static TaskParameters taskParameters = new TaskParameters();
    private static Task runningTask = null;
    private static Thread runningThread = null;
    private static boolean closingDown = false;

    /**
     * Parse and process the command line. The process will exit with status 1
     * if a serious error occurs; the exit status will be zero otherwise.
     * 
     * @param args
     *            the list of command-line arguments
     */
    public static void main(String... args) {
        try {
            if (args.length == 0) {
                displayHelp();
                return;
            }
            int argi = 0;
            while (argi < args.length) {
                String arg = args[argi++].intern();
                // Options that are not followed by additional parameters
                // come first.
                if (arg == "-h" || arg == "-help") {
                    displayHelp();
                    somethingDone = true;
                } else if (arg == "-d" || arg == "-debug") {
                    ++debugLevel;
                } else if (arg == "-q" || arg == "-quiet") {
                    quiet = true;
                } else if (arg == "-W" || arg == "-workspaces") {
                    somethingDone = true;
                    for (Workspace workspace : getService().getWorkspaces()) {
                        System.out.println(formatTSV(
                                workspace.getId(),
                                workspace.getName()));
                    }
                } else if (arg == "-M" || arg == "-models") {
                    somethingDone = true;
                    Workspace workspace = getWorkspace(workspaceId);
                    if (workspace != null) {
                        for (Model model : workspace.getModels()) {
                            System.out.println(formatTSV(
                                    model.getId(),
                                    model.getName()));
                        }
                    }
                } else if (arg == "-MO" || arg == "-modules") {
                    somethingDone = true;
                    Model model = getModel(workspaceId, modelId);
                    if (model != null) {
                        for (Module module : model.getModules()) {
                            System.out.println(formatTSV(
                                    module.getId(),
                                    module.getCode(),
                                    module.getName()));
                        }
                    }
                } else if (arg == "-VI" || arg == "-views") {
                    somethingDone = true;
                    Module module = getModule(workspaceId, modelId, moduleId);
                    if (module != null) {
                        for (View view : module.getViews()) {
                            System.out.println(formatTSV(
                                    view.getId(),
                                    view.getCode(),
                                    view.getName()));
                        }
                    }
                } else if (arg == "-F" || arg == "-files") {
                    somethingDone = true;
                    Model model = getModel(workspaceId, modelId);
                    if (model != null) {
                        for (ServerFile serverFile : model.getServerFiles()) {
                            System.out.println(formatTSV(
                                            serverFile.getId(),
                                            serverFile.getCode(),
                                            serverFile.getName()));
                        }
                    }
                } else if (arg == "-I" || arg == "-imports") {
                    somethingDone = true;
                    Model model = getModel(workspaceId, modelId);
                    if (model != null) {
                        for (Import serverImport : model.getImports()) {
                            System.out.println(formatTSV(
                                    serverImport.getId(),
                                    serverImport.getCode(),
                                    serverImport.getName(),
                                    serverImport.getImportType(),
                                    serverImport.getSourceFileId()));
                        }
                    }
                } else if (arg == "-A" || arg == "-actions") {
                    somethingDone = true;
                    Model model = getModel(workspaceId, modelId);
                    if (model != null) {
                        for (Action serverAction : model.getActions()) {
                            System.out.println(formatTSV(
                                    serverAction.getId(),
                                    serverAction.getCode(),
                                    serverAction.getName()));
                        }
                    }
                } else if (arg == "-E" || arg == "-exports") {
                    somethingDone = true;
                    Model model = getModel(workspaceId, modelId);
                    if (model != null) {
                        for (Export serverExport : model.getExports()) {
                            System.out.println(formatTSV(
                                    serverExport.getId(),
                                    serverExport.getCode(),
                                    serverExport.getName()));
                        }
                    }
                } else if (arg == "-P" || arg == "-processes") {
                    somethingDone = true;
                    Model model = getModel(workspaceId, modelId);
                    if (model != null) {
                        for (Process serverProcess : model.getProcesses()) {
                            System.out.println(formatTSV(
                                    serverProcess.getId(),
                                    serverProcess.getCode(),
                                    serverProcess.getName()));
                        }
                    }
                } else if (arg == "-emd") {
                    somethingDone = true;
                    Export export = getExport(workspaceId, modelId, exportId);
                    ExportMetadata emd = export.getExportMetadata();
                    String delimiter = emd.getDelimiter();
                    if ("\t".equals(delimiter))
                        delimiter = "\\t";
                    System.out.println("Export: " + export.getName()
                            + "\ncolumns: "
                            + String.valueOf(emd.getColumnCount()) + "\nrows: "
                            + String.valueOf(emd.getRowCount()) + "\nformat: "
                            + emd.getExportFormat() + "\ndelimiter: "
                            + delimiter + "\nencoding: " + emd.getEncoding()
                            + "\nseparator: " + emd.getSeparator());

                    String[] headerNames = emd.getHeaderNames();
                    DataType[] dataTypes = emd.getDataTypes();
                    String[] listNames = emd.getListNames();

                    for (int i = 0; i < headerNames.length; i++) {
                        System.out.println(" col " + String.valueOf(i)
                                + ":\n  name: " + headerNames[i] + "\n  type: "
                                + dataTypes[i].toString() + "\n  list: "
                                + listNames[i]);
                    }
                } else if (arg == "-x" || arg == "-execute") {
                    TaskFactory taskFactory = null;
                    if (importId != null) {
                        somethingDone = true;
                        taskFactory = getImport(workspaceId, modelId, importId);
                    } else if (exportId != null) {
                        somethingDone = true;
                        taskFactory = getExport(workspaceId, modelId, exportId);
                    } else if (actionId != null) {
                        somethingDone = true;
                        taskFactory = getAction(workspaceId, modelId, actionId);
                    } else if (processId != null) {
                        taskFactory = getProcess(workspaceId, modelId,
                                processId);
                    }
                    if (taskFactory != null) {
                        somethingDone = true;
                        lastResult = runTask(
                                taskFactory.createTask(taskParameters));
                    } else {
                        System.err.println("An import, export, action or "
                                + "process must be specified before " + arg);
                    }

                } else if (arg == "-gets" || arg == "-getc") {
                    somethingDone = true;
                    String sourceId = null;
                    if (fileId != null) {
                        sourceId = fileId;
                    } else if (exportId != null) {
                        sourceId = exportId;
                    }
                    if (null != sourceId) {
                        ServerFile serverFile = getServerFile(workspaceId,
                                modelId, sourceId, false);
                        if (serverFile != null) {
                            if (arg == "-gets") {
                                InputStream inputStream = serverFile
                                        .getDownloadStream();
                                byte[] buffer = new byte[4096];
                                int read;
                                do {
                                    if (0 < (read = inputStream.read(buffer))) {
                                        System.out.write(buffer, 0, read);
                                    }
                                } while (-1 != read);
                                System.out.flush();
                                inputStream.close();
                            } else {
                                CellReader cellReader = serverFile
                                        .getDownloadCellReader();
                                String[] row = cellReader.getHeaderRow();
                                do {
                                    StringBuilder line = new StringBuilder();
                                    for (int i = 0; i < row.length; ++i) {
                                        if (line.length() > 0)
                                            line.append('\t');
                                        line.append(row[i]);
                                    }
                                    System.out.println(line);
                                    row = cellReader.readDataRow();
                                } while (null != row);
                            }
                        }
                    }

                } else if (arg == "-puts" || arg == "-putc") {
                    somethingDone = true;
                    ServerFile serverFile = getServerFile(workspaceId, modelId,
                            fileId, true);
                    if (serverFile != null) {
                        if (arg == "-puts") {
                            OutputStream uploadStream = serverFile
                                    .getUploadStream();
                            byte[] buf = new byte[4096];
                            int read;
                            do {
                                if (0 < (read = System.in.read(buf))) {
                                    uploadStream.write(buf, 0, read);
                                }
                            } while (-1 != read);
                            uploadStream.close();
                        } else {
                            CellWriter cellWriter = serverFile
                                    .getUploadCellWriter();
                            LineNumberReader lnr = new LineNumberReader(
                                    new InputStreamReader(System.in));
                            String line;
                            while (null != (line = lnr.readLine())) {
                                String[] row = line.split("\\t");
                                if (1 == lnr.getLineNumber()) {
                                    cellWriter.writeHeaderRow(row);
                                } else {
                                    cellWriter.writeDataRow(row);
                                }
                            }
                            cellWriter.close();
                        }
                        System.out.println("Upload to " + fileId
                                + " completed.");
                    }
                    // Now check the additional parameter is present before
                    // processing consuming options
                } else if (argi >= args.length) {
                    displayHelp();
                    return;
                } else if (arg == "-s" || arg == "-service") {
                    serviceLocation = new URI(args[argi++]);
                } else if (arg == "-u" || arg == "-user") {
                    String auth = args[argi++];
                    int colonPosition = auth.indexOf(':');
                    if (colonPosition != -1) {
                        setUsername(auth.substring(0, colonPosition));
                        setPassphrase(auth.substring(colonPosition + 1));
                    } else {
                        setUsername(auth);
                        setPassphrase("?");
                    }
                } else if (arg == "-v" || arg == "-via") {
                    URI uri = new URI(args[argi++]);
                    setProxyLocation(new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), null, null, null));
                } else if (arg == "-vu" || arg == "-viauser") {
                    String auth = args[argi++];
                    int colonPosition = auth.indexOf(':');
                    if (colonPosition != -1) {
                        setProxyUsername(auth.substring(0, colonPosition));
                        setProxyPassphrase(auth.substring(colonPosition + 1));
                    } else {
                        setProxyUsername(auth);
                        setProxyPassphrase("?");
                    }
                } else if (arg == "-w" || arg == "-workspace") {
                    workspaceId = args[argi++];
                } else if (arg == "-m" || arg == "-model") {
                    modelId = args[argi++];
                } else if (arg == "-mo" || arg == "-module") {
                    moduleId = args[argi++];
                } else if (arg == "-vi" || arg == "-view") {
                    viewId = args[argi++];
                } else if (arg == "-f" || arg == "-file") {
                    fileId = args[argi++];
                } else if (arg == "-g" || arg == "-get") {
                    somethingDone = true;
                    File targetFile = new File(args[argi++]);
                    String sourceId;
                    if (fileId != null) {
                        sourceId = fileId;
                    } else if (exportId != null) {
                        sourceId = exportId;
                    } else {
                        sourceId = targetFile.getName();
                    }
                    ServerFile serverFile = getServerFile(workspaceId, modelId,
                            sourceId, false);
                    if (serverFile != null) {
                        serverFile.downLoad(targetFile, true);
                        System.out.println("The server file " + sourceId
                                + " has been downloaded to " + targetFile);
                    }
                } else if (arg == "-p" || arg == "-put") {
                    somethingDone = true;
                    File sourceFile = new File(args[argi++]);
                    String destId = fileId == null ? sourceFile.getName()
                            : fileId;
                    ServerFile serverFile = getServerFile(workspaceId, modelId,
                            destId, true);
                    if (serverFile != null) {
                        serverFile.upLoad(sourceFile, true);
                        System.out.println("The file \"" + sourceFile
								+ "\" has been uploaded as " + destId + ".");
                    }
                } else if (arg == "-i" || arg == "-import") {
                    importId = args[argi++];
                    exportId = null;
                    actionId = null;
                    processId = null;
                } else if (arg == "-e" || arg == "-export") {
                    importId = null;
                    exportId = args[argi++];
                    actionId = null;
                    processId = null;
                } else if (arg == "-a" || arg == "-action") {
                    importId = null;
                    exportId = null;
                    actionId = args[argi++];
                    processId = null;
                } else if (arg == "-pr" || arg == "-process") {
                    importId = null;
                    exportId = null;
                    actionId = null;
                    processId = args[argi++];
                } else if (arg == "-xl" || arg == "-locale") {
                    String[] localeName = args[argi++].split("_");
                    taskParameters.setLocale(localeName[0],
                            localeName.length > 0 ? localeName[1] : null);
                } else if (arg == "-xc" || arg == "-connectorproperty") {
                    String[] propEntry = args[argi++].split(":", 2);
                    if (propEntry.length != 2) {
                        throw new IllegalArgumentException("expected " + arg
                                + " [(<source>|<type>)/]property:(value|?)");
                    }

                    String[] propKey = propEntry[0].split("/", 2);
                    String prompt = propEntry[0];
                    if (propKey.length < 2) {
                        prompt = "Import source/" + prompt;
                    }
                    String property = propKey[propKey.length - 1];
                    String propValue = promptForValue(prompt, propEntry[1],
                            property.toLowerCase().endsWith("password"));
                    if (propKey.length == 2) {
                        taskParameters.addConnectorParameter(propKey[0],
                                propKey[1], propValue);
                    } else {
                        taskParameters.addConnectorParameter(propKey[0],
                                propValue);
                    }
                } else if (arg == "-xm" || arg == "-mappingproperty") {
                    String[] propEntry = args[argi++].split(":", 2);
                    if (propEntry.length != 2) {
                        throw new IllegalArgumentException("expected " + arg
                                + " [(<import id>|<import name>)/]dimension"
                                + ":(value|?)");
                    }
                    String[] propKey = propEntry[0].split("/", 2);
                    String propValue = promptForValue(propEntry[0],
                            propEntry[1], false);
                    if (propKey.length == 2) {
                        taskParameters.addMappingParameter(propKey[0],
                                propKey[1], propValue);
                    } else {
                        taskParameters.addMappingParameter(propKey[0],
                                propValue);
                    }
                } else if (arg == "-o" || arg == "-output") {
                    File outputFile = new File(args[argi++]);
                    retrieveOutput(lastResult, outputFile);
                } else if (arg == "-loadclass") {
                    String className = args[argi++];
                    try {
                        Class.forName(className);
                    } catch (Throwable thrown) {
                        if (debugLevel > 0) {
                            thrown.printStackTrace();
                        } else {
                            System.err.println("Warning: -loadclass failed ("
                                    + formatThrowable(thrown) + ")");
                        }
                    }
                } else if (arg == "-jdbcurl") {
                    jdbcUrl = args[argi++];
                    jdbcProps = new Properties();
                } else if (arg.startsWith("-jdbcuser")) {
                    String jdbcuser = args[argi++];
                    int colonIndex = jdbcuser.indexOf(":");
                    if (-1 == colonIndex) {
                        jdbcuser = promptForValue("JDBC user", jdbcuser, false);
                        jdbcProps.put("user", jdbcuser);
                    } else {
                        String jdbcpassword = jdbcuser
                                .substring(1 + colonIndex);
                        jdbcuser = jdbcuser.substring(0, colonIndex);
                        jdbcuser = promptForValue("JDBC User", jdbcuser, false);
                        jdbcpassword = promptForValue("JDBC Password",
                                jdbcpassword, true);
                        jdbcProps.put("user", jdbcuser);
                        jdbcProps.put("password", jdbcpassword);
                    }
                } else if (arg.startsWith("-jdbcfetchsize")) {
                    try {
                        jdbcFetchSize = new Integer(args[argi++]);
                    } catch (NumberFormatException nfe) {
                        System.err.println("Warning: failed to parse"
                               + " jdbcfetchsize parameter");
                    }
                } else if (arg.startsWith("-jdbcproperty")) {
                    String propertyName = args[argi++];
                    String propertyValue = null;
                    int colonIndex = propertyName.indexOf(":");
                    if (-1 != colonIndex) {
                        propertyValue = propertyName.substring(1 + colonIndex);
                        propertyName = propertyName.substring(0, colonIndex);
                    }
                    propertyValue = promptForValue("JDBC " + propertyName,
                            propertyValue, propertyName.equals("password"));
                    jdbcProps.put(propertyName, propertyValue);
                } else if (arg == "-jdbcquery") {
                    somethingDone = true;
                    String query = args[argi++];
                    ServerFile serverFile = getServerFile(workspaceId, modelId,
                            fileId, true);
                    if (serverFile != null) {
                        CellWriter cellWriter = null;
                        CellReader cellReader = null;
                        try {
                            cellWriter = serverFile.getUploadCellWriter();
                            cellReader = new JDBCCellReader(jdbcUrl, jdbcProps,
                                    query, jdbcFetchSize, debugLevel > 0);
                            String[] row = cellReader.getHeaderRow();
                            cellWriter.writeHeaderRow(row);
                            int rowCount = 0;
                            do {
                                if (null != (row = cellReader.readDataRow())) {
                                    cellWriter.writeDataRow(row);
                                    ++rowCount;
                                }
                            } while (null != row);
                            cellWriter.close();
                            cellWriter = null;
                            System.out.println("Transferred " + rowCount
                                    + " records");
                        } finally {
                            if (cellReader != null)
                                cellReader.close();
                            if (cellWriter != null)
                                cellWriter.abort();
                        }
                    }
                } else {
                    displayHelp();
                    return;
                }
            }
            if (!somethingDone) {
                displayHelp();
            }
            closeDown();
        } catch (Throwable thrown) {
            if (debugLevel > 0) {
                // A stack trace for those who want it
                Throwable cause = thrown;
                while (null != cause.getCause()) {
                    System.err.println(cause);
                    cause = cause.getCause();
                }
                cause.printStackTrace();
            } else if (!(thrown instanceof InterruptedException)) {
                // Some brevity for those who don't
                System.err.println(formatThrowable(thrown));
            }
            // System.exit causes abrupt termination, but the status is useful
            // when run from an automated script.
            closeDown();
            System.exit(1);
        }
    }

    /**
     * Release any resources held by this class.
     * 
     * @since 1.3
     */
    protected static synchronized void closeDown() {
        if (service != null && runningTask == null) {
            service.close();
            service = null;
        }
    }

    /**
     * Track the progress of a task on the server until completion. If
     * run from a command line (ie <tt>System.console() != null</tt>) the
     * progress will be displayed on the controlling terminal.
     * 
     * @param task
     *            The task to run
     * @return the result following completion of the task; null otherwise
     * @since 1.3
     */
    protected static synchronized TaskResult runTask(Task task)
            throws AnaplanAPIException, InterruptedException {
        runningTask = task;
        runningThread = Thread.currentThread();
        TaskResult result = trackRunningTask(false);
        runningThread = null;
        runningTask = null;
        return result;
    }

    static {
        try {
            Thread cancelThread = new Thread() {
                public void run() {
                    closingDown = true;
                    Thread runner = runningThread;
                    if (runner != null) {
                        try {
                            runner.interrupt();
                        } catch (Throwable thrown) {
                            if (debugLevel > 0)
                                thrown.printStackTrace();
                        }
                    }
                    cancelRunningTask();
                }
            };
            cancelThread.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(cancelThread);
        } catch (Throwable thrown) {
            thrown.printStackTrace();
        }
    }

    private static synchronized void cancelRunningTask() {
        if (runningTask != null) {
            try {
                if (System.console() != null)
                    System.console().printf("\rClient terminated, cancelling...");
                runningTask.cancel();
                trackRunningTask(true);
            } catch (Throwable thrown) {
                if (debugLevel > 0) {
                    Throwable cause = thrown;
                    while (null != cause.getCause()) {
                        System.err.println(cause);
                        cause = cause.getCause();
                    }
                    cause.printStackTrace();
                } else {
                    System.err.println(formatThrowable(thrown));
                }
            } finally {
                runningTask = null;
                closeDown();
            }
        }
    }

    private static synchronized TaskResult trackRunningTask(boolean wasClosingDown) throws AnaplanAPIException, InterruptedException {
        TaskStatus status = null;
        int lastStatusLength = 4;
        int interval = 1000;
        int totalTime = 0;
        int failCount = 0;
        if (System.console() != null)
            System.console().printf("\n");
        do {
            if (!wasClosingDown && closingDown) {
                throw new InterruptedException();
            }
            if (interval > 0) {
                Thread.sleep(interval);
                totalTime += interval;
            }
            if (totalTime > 60000) {
                interval = 60000;
            } else if (totalTime > 10000) {
                interval = 10000;
            } else {
                interval = 1000;
            }
            try {
                status = runningTask.getStatus();
                failCount = 0;
            } catch (AnaplanAPIException thrown) {
                status = null;
                // Allow up to 30 attempts before giving up.
                if (++failCount > 30) {
                    System.err.println();
                    throw new AnaplanAPIException(
                            "Task was started, but server cannot now be reached"
                            + " - giving up after 30 attempts", thrown);
                } else if (!quiet && System.console() != null) {
                    String message = "";
                    if (debugLevel > 0) {
                        message = "Failed to get status ("
                                + formatThrowable(thrown) + "); retrying in "
                                + (interval / 1000) + "s\n";
                    } else {
                        message = "Checking in " + (interval / 1000) + "s";
                    }
                    String format = "\r%-" + lastStatusLength + "s";
                    System.console().printf(format, message);
                    lastStatusLength = message.length();
                }
            }
            if (status != null && !quiet && System.console() != null) {
                String message;
                if (status.getCurrentStep() != null) {
                    message = status.getCurrentStep();
                } else switch (status.getTaskState()) {
                    case NOT_STARTED:
                        message = "Waiting to start";
                    break;
                    case IN_PROGRESS:
                        message = "In progress";
                    break;
                    case COMPLETE:
                        message = "Complete";
                    break;
                    case CANCELLING:
                        message = "Cancelling";
                    break;
                    case CANCELLED:
                        message = "Cancelled";
                    break;
                    default:
                        message = status.getTaskState().toString();
                        message = message.replace('_', ' ');
                        message = message.charAt(0)
                            + message.substring(1).toLowerCase();
                }
                if (status.getProgress() > 0) {
                    message += " ("
                            + Math.floor(status.getProgress() * 1000) / 10
                            + "%)";
                }
                String format = "\r%-" + lastStatusLength + "s";
                System.console().printf(format, message);
                lastStatusLength = message.length();
            }
        } while (status == null || !(
                    status.getTaskState() == TaskStatus.State.COMPLETE
                    || status.getTaskState() == TaskStatus.State.CANCELLED));
        String message;
        if (status.getTaskState() == TaskStatus.State.CANCELLED) {
            message = "The operation was cancelled";
            if (status.getCancelledBy() != null) {
                message += " by " + status.getCancelledBy();
            }
            if (status.getResult() != null) {
                message += "; some actions may have completed.";
            } else {
                message += ".";
            }
        } else {
            if (status.getResult() != null) {
                message = status.getResult().isSuccessful() ?
                        "The operation was successful." :
                        "The operation failed.";
            } else {
                message = "No result was provided.";
            }
        }
        if (System.console() != null) {
            System.console().printf("\r%-" + lastStatusLength + "s\n", message);
        } else {
            System.out.println(message);
        }
        if (status.getResult() != null) {
            System.out.println(status.getResult().toString());
        }
        return status.getResult();
    }

    /**
     * Retrieve any dump file(s) from the server following an import or process
     * containing 1+ imports. If the result is for a process, then
     * outputLocation will be a directory.
     * 
     * @param taskResult
     *            the result of running the task
     * @param outputLocation
     *            the location in which to store the data
     * @since 1.3
     */
    protected static void retrieveOutput(TaskResult taskResult,
            File outputLocation) throws AnaplanAPIException, IOException {
        if (taskResult != null) {
            if (!taskResult.getNestedResults().isEmpty()) {
                if (outputLocation.exists() && !outputLocation.isDirectory()) {
                    throw new IllegalArgumentException(
                            "Process dumps require a directory, but path \""
                            + outputLocation.getPath() + " is not a directory");
				}
				if (!outputLocation.exists() && !outputLocation.mkdirs()) {
					throw new AnaplanAPIException("Failed to create directory "
							+ outputLocation.getPath());
				}
				int index = 0;
				for (TaskResult nestedResult : taskResult.getNestedResults()) {
					ServerFile nestedDumpServerFile = nestedResult
							.getFailureDump();
					if (nestedDumpServerFile != null) {
						String fileName = "" + index;
						if (nestedResult.getObjectId() != null) {
							fileName += "-" + nestedResult.getObjectId();
						}
						if (nestedResult.getObjectName() != null) {
							fileName += "-" + nestedResult.getObjectName();
						}
						File nestedFile = new File(outputLocation, fileName);
						nestedDumpServerFile.downLoad(nestedFile, true);
						System.out.println("Dump file written to \""
                                + nestedFile + "\"");
					}
					++index;
				}

			} else if (taskResult.isFailureDumpAvailable()) {
				ServerFile failureDump = taskResult.getFailureDump();
				failureDump.downLoad(outputLocation, true);
				System.out.println("Dump file written to \"" + outputLocation
                        + "\"");
			}
		} else {
			System.out.println("No dump file is available.");
			if (outputLocation.exists() && !outputLocation.isDirectory()) {
				outputLocation.delete();
			}
		}
	}

	/**
	 * Provide a suitable error message from an exception.
	 * 
	 * @param thrown
	 *            the exception
	 * @return a message describing the exception
	 * @since 1.3
	 */
	protected static String formatThrowable(Throwable thrown) {
		StringBuilder message = new StringBuilder(
				thrown instanceof AnaplanAPIException ? "AnaplanAPI" : thrown
						.getClass().getSimpleName());
		if (message.length() > 9 && message.toString().endsWith("Exception")) {
			message.delete(message.length() - 9, message.length());
		}
		for (int i = 1; i < message.length() - 1; ++i) {
			char pc = message.charAt(i - 1);
			char ch = message.charAt(i);
			char nc = message.charAt(i + 1);
			if (Character.isUpperCase(ch)) {
				if (!Character.isUpperCase(nc)) {
					message.setCharAt(i, Character.toLowerCase(ch));
				}
				if (!Character.isUpperCase(pc) || !Character.isUpperCase(nc)) {
					message.insert(i++, ' ');
				}
			}
		}
		if (null != thrown.getMessage()) {
			message.append(": ").append(thrown.getMessage());
		}
		if (null != thrown.getCause()) {
			message.append(" (").append(formatThrowable(thrown.getCause()))
					.append(')');
		}
		return message.toString();
	}

	/**
	 * Format values as tab-separated text
	 * 
	 * @param values
	 *            a list of values
	 * @return tab-separated text
	 * @since 1.3
	 */
	protected static String formatTSV(Object... values) {
		StringBuilder tsv = new StringBuilder();
		for (Object value : values) {
			if (tsv.length() > 0)
				tsv.append('\t');
			if (value != null)
				tsv.append(value.toString());
		}
		return tsv.toString();
	}

	/**
	 * Locate or optionally create a server file on the server. An error message
	 * will be produced if the workspace, model or (if create is false) server
	 * file cannot be located.
	 * 
	 * @param workspaceId
	 *            the name or ID of the workspace
	 * @param modelId
	 *            the name or ID of the model
	 * @param fileId
	 *            the name or ID of the server file
	 * @param create
	 *            if true and the file does not exist on the server, create a
	 *            new file
	 * @return the server file, or null if the workspace, model or (if create is
	 *         false) server file could not be located.
	 * @since 1.3
	 */
	protected static ServerFile getServerFile(String workspaceId,
			String modelId, String fileId, boolean create)
			throws AnaplanAPIException {
		Model model = getModel(workspaceId, modelId);
		if (model == null) {
			return null;
		}
		if (fileId == null || fileId.isEmpty()) {
			System.err.println("A file ID must be provided");
			return null;
		}
		ServerFile serverFile = model.getServerFile(fileId);
		if (serverFile == null) {
			if (create) {
				serverFile = model.createServerFileImportDataSource(fileId,
						"Anaplan Connect");
			} else {
				System.err.println("File \"" + fileId
                        + "\" not found in workspace " + workspaceId
						+ ", model " + modelId);
			}
		}
		return serverFile;
	}

	/**
	 * Locate a import definition on the server. An error message will be
	 * produced if the workspace, model or import definition cannot be located.
	 * 
	 * @param workspaceId
	 *            the name or ID of the workspace
	 * @param modelId
	 *            the name or ID of the model
	 * @param importId
	 *            the name or ID of the import definition
	 * @return the import definition, or null if not found
	 * @since 1.3
	 */
	protected static Import getImport(String workspaceId, String modelId,
			String importId) throws AnaplanAPIException {
		Model model = getModel(workspaceId, modelId);
		if (model == null) {
			return null;
		}
		if (importId == null || importId.isEmpty()) {
			System.err.println("An import ID, code or name must be provided");
		}
		Import serverImport = model.getImport(importId);
		if (serverImport == null) {
			System.err.println("Import \"" + importId
                    + "\" not found in workspace " + workspaceId + ", model "
					+ modelId);
		}
		return serverImport;
	}

	/**
	 * Locate a saved export on the server. An error message will be produced if
	 * the workspace, model or saved export cannot be located.
	 * 
	 * @param workspaceId
	 *            the name or ID of the workspace
	 * @param modelId
	 *            the name or ID of the model
	 * @param exportId
	 *            the name or ID of the saved export
	 * @return the saved export, or null if not found
	 * @since 1.3
	 */
	protected static Export getExport(String workspaceId, String modelId,
			String exportId) throws AnaplanAPIException {
		Model model = getModel(workspaceId, modelId);
		if (model == null) {
			return null;
		}
		if (exportId == null || exportId.isEmpty()) {
			System.err.println("An export ID, code or name must be provided");
		}
		Export serverExport = model.getExport(exportId);
		if (serverExport == null) {
			System.err.println("Export \"" + exportId
                    + "\" not found in workspace " + workspaceId + ", model "
					+ modelId);
		}
		return serverExport;
	}

	/**
	 * Locate a saved action on the server. An error message will be produced if
	 * the workspace, model or saved action cannot be located.
	 * 
	 * @param workspaceId
	 *            the name or ID of the workspace
	 * @param modelId
	 *            the name or ID of the model
	 * @param actionId
	 *            the name or ID of the saved action
	 * @return the saved action, or null if not found
	 * @since 1.3
	 */
	protected static Action getAction(String workspaceId, String modelId,
			String actionId) throws AnaplanAPIException {
		Model model = getModel(workspaceId, modelId);
		if (model == null) {
			return null;
		}
		if (actionId == null || actionId.isEmpty()) {
			System.err.println("An action ID, code or name must be provided");
		}
		Action serverAction = model.getAction(actionId);
		if (serverAction == null) {
			System.err.println("Action \"" + actionId
                    + "\" not found in workspace " + workspaceId + ", model "
					+ modelId);
		}
		return serverAction;
	}

	/**
	 * Locate a process definition on the server. An error message will be
	 * produced if the workspace, model or process definition cannot be located.
	 * 
	 * @param workspaceId
	 *            the name or ID of the workspace
	 * @param modelId
	 *            the name or ID of the model
	 * @param processId
	 *            the name or ID of the process definition
	 * @return the process definition, or null if not found
	 * @since 1.3
	 */
	protected static Process getProcess(String workspaceId, String modelId,
			String processId) throws AnaplanAPIException {
		Model model = getModel(workspaceId, modelId);
		if (model == null) {
			return null;
		}
		if (processId == null || processId.isEmpty()) {
			System.err.println("A process ID, code or name must be provided");
		}
		Process serverProcess = model.getProcess(processId);
		if (serverProcess == null) {
			System.err.println("Process \"" + processId
                    + "\" not found in workspace " + workspaceId + ", model "
					+ modelId);
		}
		return serverProcess;
	}

	/**
	 * Locate a saved view on the server. An error message will be produced if
	 * the workspace, model, module or saved view cannot be located.
	 * 
	 * @param workspaceId
	 *            the name or ID of the workspace
	 * @param modelId
	 *            the name or ID of the model
	 * @param moduleId
	 *            the name or ID of the module
	 * @param viewId
	 *            the name or ID of the saved view
	 * @return the saved view, or null if not found
	 * @since 1.3
	 */
	protected static View getView(String workspaceId, String modelId,
			String moduleId, String viewId) throws AnaplanAPIException {
		Module module = getModule(workspaceId, modelId, moduleId);

		if (module == null) {
			return null;
		}
		if (viewId == null || viewId.isEmpty()) {
			System.err.println("A view ID must be provided");
			return null;
		}
		View view = module.getView(viewId);
		if (view == null) {
			System.err.println("View \"" + viewId
                    + "\" not found in workspace \"" + workspaceId
                    + "\", model \"" + modelId + "\", module \"" + moduleId
                    + "\"");
		}
		return view;
	}

	/**
	 * Locate a module on the server. An error message will be produced if the
	 * workspace, model or module cannot be located.
	 * 
	 * @param workspaceId
	 *            the name or ID of the workspace
	 * @param modelId
	 *            the name or ID of the model
	 * @param moduleId
	 *            the name or ID of the module
	 * @return the module, or null if not found
	 * @since 1.3
	 */
	protected static Module getModule(String workspaceId, String modelId,
			String moduleId) throws AnaplanAPIException {
		Model model = getModel(workspaceId, modelId);

		if (model == null) {
			return null;
		}
		if (moduleId == null || moduleId.isEmpty()) {
			System.err.println("A module ID must be provided");
			return null;
		}
		Module module = model.getModule(moduleId);
		if (module == null) {
			System.err.println("Module \"" + moduleId
                    + "\" not found in workspace \"" + workspaceId
                    + "\", model \"" + modelId + "\"");
		}
		return module;
	}

	/**
	 * Locate a model on the server. An error message will be produced if the
	 * workspace or model cannot be located.
	 * 
	 * @param workspaceId
	 *            the name or ID of the workspace
	 * @param modelId
	 *            the name or ID of the model
	 * @return the model, or null if not found
	 * @since 1.3
	 */
	protected static Model getModel(String workspaceId, String modelId)
			throws AnaplanAPIException {
		Workspace workspace = getWorkspace(workspaceId);
		if (workspace == null) {
			return null;
		}
		if (modelId == null || modelId.isEmpty()) {
			System.err.println("A model ID must be provided");
			return null;
		}
		Model model = workspace.getModel(modelId);
		if (model == null) {
			System.err.println("Model \"" + modelId
                    + "\" not found in workspace \"" + workspaceId + "\"");
		}
		return model;
	}

	/**
	 * Locate a workspace on the server. An error message will be produced if
	 * the workspace cannot be located.
	 * 
	 * @param workspaceId
	 *            the name or ID of the workspace
	 * @return the workspace, or null if not found
	 * @since 1.3
	 */
	protected static Workspace getWorkspace(String workspaceId)
			throws AnaplanAPIException {
		if (workspaceId == null || workspaceId.isEmpty()) {
			System.err.println("A workspace ID must be provided");
			return null;
		}
		Workspace result = getService().getWorkspace(workspaceId);
		if (result == null) {
			System.err.println("Workspace \"" + workspaceId
                    + "\" does not exist or is not available to this user");
		}
		return result;
	}

	/**
	 * Returns the client service instance. If this is the first invocation of
	 * this method, then a new instance will be created, using the current
	 * service credentials, location, proxy and debug settings.
	 * 
	 * @return the service instance
	 * @since 1.3
	 */
	protected static Service getService() throws AnaplanAPIException {
		if (service == null) {
			service = serviceLocation != null ? new Service(serviceLocation)
					: new Service();
			service.setDebugLevel(debugLevel);
			service.setServiceCredentials(getServiceCredentials());
            if (proxyLocationSet) {
                service.setProxyLocation(proxyLocation);
                Credentials proxyCredentials = getProxyCredentials();
                if (proxyCredentials != null) {
                    service.setProxyCredentials(proxyCredentials);
                }
            }
		}
		return service;
	}

    /**
     * Gathers the Anaplan service credentials.
     *
     * @return the credentials for the Anaplan service, obtained from
     * getUsername() and getPassphrase()
     * @since 1.3.1
     */
    protected static Credentials getServiceCredentials() {
        return new Credentials(getUsername(), getPassphrase());
    }

    /**
     * Gathers the proxy credentials if set. If the user name is of the form
     * "domain\u005cuser" or "domain\u005cworkstation\u005cuser" it is interpreted accordingly.
     *
     * @return the credentials for the proxy, or null if not specified.
     * @since 1.3.1
     */
    protected static Credentials getProxyCredentials() {
        if (!proxyUsernameSet) {
            return null;
        }
        String[] parts = getProxyUsername().split("\\\\");
        if (parts.length > 1) {
            String domain = parts[0];
            String workstation = null;
            String user = parts[1];
            if (parts.length > 2) {
                workstation = user;
                user = parts[2];
            }
            return new Credentials(user, getProxyPassphrase(), domain, workstation);
        }
        return new Credentials(parts[0], getProxyPassphrase());
    }
	/**
	 * Returns the username set using setUsername(). If null, empty or '?' and a
	 * system console is available, then the user will be prompted for a value.
	 * 
	 * @return the username
	 * @since 1.3
	 */
	protected static String getUsername() {
		if (username == null || username.isEmpty()) {
			Console console = System.console();
			if (console != null) {
				username = console.readLine("Username:");
			} else {
				throw new UnsupportedOperationException(
						"Username must be specified");
			}
		}
		return username;
	}

	/**
	 * Returns the password set using setPassword(). If null, empty or '?' and a
	 * system console is available, then the user will be securely prompted for
	 * a value.
	 * 
	 * @return the password
	 * @since 1.3
	 */
	protected static String getPassphrase() {
		if (passphrase == null || passphrase.isEmpty() || passphrase.equals("?")) {
			Console console = System.console();
			if (console != null) {
				passphrase = new String(console.readPassword("Password:"));
			} else {
				throw new UnsupportedOperationException(
						"Password must be specified");
			}
		}
		return passphrase;
	}

	/**
	 * Set the service location. This is the production API server by default.
	 * 
	 * @param serviceLocation
	 *            the URI of the API service endpoint
	 * @since 1.3
	 */
	protected static void setServiceLocation(URI serviceLocation) {
		Program.serviceLocation = serviceLocation;
	}

	/**
	 * Set the proxy location. If null, no proxy will be used, and a direct
	 * connection to the internet is assumed. If not set, proxy settings will be automatically detected, based on the platform.
	 * 
	 * @param proxyLocation
	 *            the URI of the proxy, or null if no proxy is to be used
	 * @since 1.3
	 */
	protected static void setProxyLocation(URI proxyLocation) {
		Program.proxyLocation = proxyLocation;
        Program.proxyLocationSet = true;
	}

	/**
	 * Returns the proxy username set using setProxyUsername(). If it was set to '?' and a
	 * system console is available, then the user will be prompted for a value.
	 * 
	 * @return the proxy username
	 * @since 1.3.1
	 */
	protected static String getProxyUsername() {
		if ("?".equals(proxyUsername)) {
			Console console = System.console();
			if (console != null) {
				proxyUsername = console.readLine("Proxy username:");
			} else {
				throw new UnsupportedOperationException(
						"Proxy username must be specified");
			}
		}
		return proxyUsername;
	}

	/**
	 * Returns the proxy password set using setProxyPassword(). If it was set to '?' and a
	 * system console is available, then the user will be securely prompted for
	 * a value.
	 * 
	 * @return the proxy password
	 * @since 1.3.1
	 */
	protected static String getProxyPassphrase() {
		if ("?".equals(proxyPassphrase)) {
			Console console = System.console();
			if (console != null) {
				proxyPassphrase = new String(console.readPassword("Proxy password:"));
			} else {
				throw new UnsupportedOperationException(
						"Proxy password must be specified");
			}
		}
		return proxyPassphrase;
	}

	/**
	 * Set the debug level. If this is increased then more verbose output will
	 * be produced. The default setting is zero.
	 * 
	 * @param debugLevel
	 *            an integer debug level, nominally in the range 0..2
	 * @since 1.3
	 */
	protected static void setDebugLevel(int debugLevel) {
		Program.debugLevel = debugLevel;
	}

	/**
	 * Set the username of the Anaplan account for the service to use.
	 * 
	 * @param username
	 *            the email address of the Anaplan user
	 * @since 1.3
	 */
	protected static void setUsername(String username) {
		Program.username = username;
	}

	/**
	 * Set the passphrase of the Anaplan account for the service to use.
	 * 
	 * @param passphrase
	 *            the passphrase of the Anaplan user
	 * @since 1.3
	 */
	protected static void setPassphrase(String passphrase) {
		Program.passphrase = passphrase;
	}

	/**
	 * Set the username for an authenticating proxy
	 * 
	 * @param username
	 *            the username for the authenticating proxy
	 * @since 1.3.1
	 */
	protected static void setProxyUsername(String username) {
		Program.proxyUsername = username;
        proxyUsernameSet = true;
	}

	/**
	 * Set the passphrase for an authenticating proxy
	 * 
	 * @param passphrase
	 *            the passphrase for the authenticating proxy
	 * @since 1.3.1
	 */
	protected static void setProxyPassphrase(String passphrase) {
		Program.proxyPassphrase = passphrase;
	}

	/**
	 * Check and if necessary prompt for a value. If propertyValue is null,
	 * empty or '?', and the program is associated with a terminal (ie
	 * <tt>System.console() != null</tt>), then prompt the user using the value
	 * in propertyName, followed by a colon. If password is true, then the value
	 * input by the user will not be echoed.
	 * 
	 * @param propertyName
	 *            the value to be displayed to the user if prompted
	 * @param propertyValue
	 *            the value supplied
	 * @param password
	 *            flag to suppress echoing
	 * @return the value passed in propertyValue, or the value entered by the
	 *         user if it was necessary to prompt
	 * @throws UnsupportedOperationException
	 *             if there is no terminal associated with the program and a
	 *             value was not supplied.
	 * @since 1.3
	 */
	protected static String promptForValue(String propertyName,
			String propertyValue, boolean password) {
		if (null == propertyValue || 0 == propertyValue.length()
				|| propertyValue.equals("?")) {
			Console console = System.console();
			if (console != null) {
				if (password) {
					propertyValue = new String(
							console.readPassword(propertyName + ":"));
				} else {
					propertyValue = new String(console.readLine(propertyName
							+ ":"));
				}
			} else {
				throw new UnsupportedOperationException("Value for "
						+ propertyName + " must be specified");
			}
		}
		return propertyValue;
	}

	private static void displayHelp() {
		System.err.println("Options are:\n"
            + "\n"
            + "General:\n"
            + "--------\n"
            + "(-h|-help): display this help\n"
            + "(-d|-debug): Show more detailed output\n"
            + "(-q|-quiet): Show less detailed output\n"
            + "\n"
            + "Connection:\n"
            + "-----------\n"
            + "(-s|-service) <service URI>: API service endpoint"
            + " (defaults to https://api.anaplan.com/)\n"
            + "(-u|-user) <username>[:<password>]"
            + ": Anaplan user name + (optional) password\n"
            + "(-v|-via) <proxy URI>: use specified proxy\n"
            + "(-vu|-viauser) [<domain>[\\<workstation>]\\]<username>[:<password>]: use proxy credentials\n"
            + "\n"
            + "Workspace Contents:\n"
            + "-------------------\n"
            + "(-W|-workspaces): list available workspaces\n"
            + "(-w|-workspace) (<id>|<name>): select a workspace by id/name\n"
            + "(-M|-models): list available models in selected workspace\n"
            + "(-m|-model) (<id>|<name>): select a model by id/name\n"
            + "(-MO|-modules): list available modules in selected model\n"
            + "(-mo|-module): (<id>|<name>): select a module by id/name\n"
            + "(-VI|-views): list available views in selected module\n"
            + "(-vi|-view): (<id>|<name>): select a view by id/name\n"
            + "(-F|-files): list available server files in selected model\n"
            + "(-f|-file) (<id>|<name>): select a server file by id/name\n"
            + "\n"
            + "Data Transfer:\n"
            + "--------------\n"
            + "(-g|-get) <local path>: Download specified server file to local file\n"
            + "-gets Write specified server file to standard output\n"
            + "-getc Write tab-separated server file to standard output\n"
            + "(-p|-put) <local path>: Upload to specified server file from local file\n"
            + "-puts Upload to specified server file from standard input\n"
            + "-putc Upload to specified server file from tab-separated standard input\n"
            + "\n"
            + "Server Actions:\n"
            + "---------------\n"
            + "(-I|-imports): list available imports in selected model\n"
            + "(-i|-import) (<id>|<name>): select an import by id/name\n"
            + "(-E|-exports): list available exports in selected model\n"
            + "(-e|-export) (<id>|<name>): select an export by id/name\n"
            + "(-A|-actions): list available actions in selected model\n"
            + "(-a|-action) (<id>|<name>): select an action by id/name\n"
            + "(-P|-processes): list available processes in selected model\n"
            + "(-pr|-process) <id/name>: select a process by id/name\n"
            + "(-xl|-locale) <locale> Specify locale (eg en_US) to perform server opertion\n"
            + "(-xc|-connectorproperty) [(<source>|<type>)/]property:(value|?):\n"
            + "    specify import data source connection property\n"
            + "(-xm|-mappingproperty) [(<import id>|<import name>)/]dimension:(value|?):\n"
            + "    specify prompt-at-runtime import mapping value"
            + "(-x|-execute): Run the selected import/export/action/process\n"
            + "\n"
            + "Action Information:\n"
            + "-------------------\n"
            + "(-o|-output) <local path>: Retrieve dump file(s) for completed import/process\n"
            + "-emd <local path>: Get metadata for an export\n"
            + "\n"
            + "JDBC:\n"
            + "-----\n"
            + "-loadclass <class name>: Load a Java class\n"
            + "-jdbcurl: JDBC URL for -jdbcquery to connect to\n"
            + "-jdbcuser (<username>|?)[:(<password>|?)]: JDBC username and password\n"
            + "-jdbcproperty <propname>:(<propval>|?): set JDBC connection property\n"
            + "-jdbcquery <query>: retrieve data from JDBC data source\n"
            + "-jdbcfetchsize <size>: hint to transfer <size> records at a time\n");
    }
}
