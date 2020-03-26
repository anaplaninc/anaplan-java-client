//   Copyright 2011, 2013 Anaplan Inc.
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

import com.anaplan.client.auth.Credentials;
import com.anaplan.client.auth.KeyStoreManager;
import com.anaplan.client.dto.ChunkData;
import com.anaplan.client.dto.ExportMetadata;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.BadSystemPropertyError;
import com.anaplan.client.ex.NoChunkError;
import com.anaplan.client.ex.PrivateKeyException;
import com.anaplan.client.jdbc.JDBCCellReader;
import com.anaplan.client.jdbc.JDBCCellWriter;
import com.anaplan.client.jdbc.JDBCConfig;
import com.anaplan.client.logging.LogUtils;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.retryer.AnaplanJdbcRetryer;
import com.anaplan.client.transport.retryer.FeignApiRetryer;
import com.google.common.base.Strings;
import com.opencsv.CSVParser;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

/**
 * A command-line interface to the Anaplan Connect API library. Running the
 * program with no arguments will display the available options. This class also
 * contains several static convenience methods that may be useful by other
 * alternative main-method implementations - these should extend this class to
 * gain access to them as they have protected access.
 */

public abstract class Program {

    private static int debugLevel = 0;
    private static boolean quiet = false;
    private static Service service = null;
    private static URI serviceLocation = null;
    private static URI authServiceUrl;
    private static URI proxyLocation = null;
    private static boolean proxyLocationSet = false;
    private static String username = null;
    private static String passphrase = null;
    private static String proxyUsername = null;
    private static boolean proxyUsernameSet = false;
    private static String proxyPassphrase = null;
    private static String keyStorePath = null;
    private static String keyStoreAlias = null;
    private static String keyStorePassword = null;
    private static String privateKeyPath = null;
    private static String certificatePath = null;
    private static boolean userCertificateAuthentication = false;
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
    private static TaskParameters taskParameters = new TaskParameters();

    private static final int MIN_CHUNK_SIZE = 1;
    private static final int MAX_CHUNK_SIZE = MIN_CHUNK_SIZE * 50;
    private static int chunkSize = fetchChunkSize(String.valueOf(MIN_CHUNK_SIZE));
    private static int maxRetryCount = Constants.MIN_RETRY_COUNT;
    private static int retryTimeout = Constants.MIN_RETRY_TIMEOUT_SECS;
    private static int httpConnectionTimeout = Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS;
    private ConnectionProperties properties;

    private static final Logger LOG = LoggerFactory.getLogger(Program.class);

    /**
     * Parse and process the command line. The process will exit with status 1
     * if a serious error occurs; the exit status will be zero otherwise.
     *
     * @param args the list of command-line arguments
     */
    //TODO: Modularize main()
    public static void main(String... args) {

        System.setProperty("file.encoding", StandardCharsets.UTF_8.name());

        // Register the PID within the logback context
        String name = String.valueOf(ManagementFactory.getRuntimeMXBean().getName());
        if (name != null) {
            MDC.put("process_id", name.split("@")[0]);
        }

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
                } else if (arg == "-version") {
                    displayVersion();
                    somethingDone = true;
                } else if (arg == "-d" || arg == "-debug") {
                    if (debugLevel++ == 0) {
                        LogUtils.enableDebugLogging();
                        displayVersion();
                    }
                } else if (arg == "-q" || arg == "-quiet") {
                    quiet = true;
                } else if (arg == "-F" || arg == "-files") {
                    somethingDone = true;
                    Model model = getModel(workspaceId, modelId);
                    if (model != null) {
                        for (ServerFile serverFile : model.getServerFiles()) {
                            LOG.info(Utils.formatTSV(
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
                            LOG.info(Utils.formatTSV(
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
                            LOG.info(Utils.formatTSV(
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
                            LOG.info(Utils.formatTSV(
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
                            LOG.info(Utils.formatTSV(
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
                    if ("\t".equals(delimiter)) {
                        delimiter = "\\t";
                    }
                    LOG.info("Export: " + export.getName()
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
                        LOG.info(" col " + String.valueOf(i)
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
                        Task task = taskFactory.createTask(taskParameters);
                        lastResult = task.runTask();
                    } else {
                        LOG.error("An import, export, action or "
                                          + "process must be specified before " + arg);
                    }

                } else if (arg == "-gets" || arg == "-getc") {
                    somethingDone = true;
                    String sourceId = null;
                    if (fileId != null) {
                        sourceId = fileId;
                    } else if (exportId != null) {
                        if (lastResult != null && lastResult.isSuccessful()) {
                            sourceId = exportId;
                        } else {
                            LOG.error("Export failed - ignoring content");
                        }
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
                                        if (line.length() > 0) {
                                            line.append('\t');
                                        }
                                        line.append(row[i]);
                                    }
                                    LOG.info(line.toString());
                                    row = cellReader.readDataRow();
                                } while (null != row);
                            }
                        }
                    }

                } else if (arg == "-ch" || arg == "-chunksize") {
                    fetchChunkSize(args[argi++]);
                } else if (arg == "-auth" || arg == "-authserviceurl") {
                    authServiceUrl = new URI(args[argi++]);
                } else if (arg == "-puts" || arg == "-putc") {
                    somethingDone = true;
                    ServerFile serverFile = getServerFile(workspaceId, modelId,
                                                          fileId, true);
                    if (serverFile != null) {
                        if (arg == "-puts") {
                            OutputStream uploadStream = serverFile
                                    .getUploadStream(chunkSize);
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
                                    .getUploadCellWriter(chunkSize);
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
                        LOG.info("Upload to " + fileId
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
                } else if (arg == "-mrc" || arg == "-maxretrycount") {
                    maxRetryCount = fetchMaxRetryCount(args[argi++]);
                } else if (arg == "-rt" || arg == "-retrytimeout") {
                    retryTimeout = fetchRetryTimeout(args[argi++]);
                } else if (arg == "-ct" || arg == "-httptimeout") {
                    httpConnectionTimeout = fetchHttpTimeout(args[argi++]);
                } else if (arg == "-c" || arg == "-certificate") {
                    String certificatePath = args[argi++];
                    setCertificatePath(certificatePath);
                } else if (arg == "-pkey" || arg == "-privatekey") {
                    if (keyStorePath != null) {
                        throw new IllegalArgumentException("expected either the privatekey or the keystore arguments");
                    }
                    String auth = args[argi++];
                    int colonPosition = auth.lastIndexOf(':');
                    if (colonPosition != -1) {
                        setPrivateKeyPath(auth.substring(0, colonPosition));
                        setPassphrase(auth.substring(colonPosition + 1));
                    } else {
                        setUsername(auth);
                        setPassphrase("?");
                    }
                } else if (arg == "-k" || arg == "-keystore") {
                    if (passphrase != null || privateKeyPath != null) {
                        throw new IllegalArgumentException("expected either the privatekey or keystore arguments");
                    }
                    String keyStorePath = args[argi++];
                    setKeyStorePath(keyStorePath);
                } else if (arg == "-ka" || arg == "-keystorealias") {
                    String keyStoreAlias = args[argi++];
                    setKeyStoreAlias(keyStoreAlias);
                } else if (arg == "-kp" || arg == "-keystorepass") {
                    String keyStorePassword = args[argi++];
                    setKeyStorePassword(keyStorePassword);
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
                        if (lastResult != null && lastResult.isSuccessful()) {
                            sourceId = exportId;
                        } else {
                            LOG.error("Export failed - ignoring content");
                            sourceId = null;
                        }
                    } else {
                        sourceId = targetFile.getName();
                    }
                    if (sourceId != null) {
                        ServerFile serverFile = getServerFile(workspaceId, modelId,
                                                              sourceId, false);
                        if (serverFile != null) {
                            serverFile.downLoad(targetFile, true);
                            LOG.info("The server file {} has been downloaded to {}", sourceId, targetFile.getAbsolutePath());
                        }
                    }
                } else if (arg == "-p" || arg == "-put") {
                    somethingDone = true;
                    File sourceFile = new File(args[argi++]);
                    String destId = fileId == null ? sourceFile.getName()
                            : fileId;
                    ServerFile serverFile = getServerFile(workspaceId, modelId,
                                                          destId, true);
                    if (serverFile != null) {
                        serverFile.upLoad(sourceFile, true, chunkSize);
                        LOG.info("The file \"" + sourceFile
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
                    //Removing the usage of loadclass parameter
                    System.err.println("Warning : Loadclass parameter is deprecated starting in Anaplan Connect v1.4.4. Anaplan Connect will automatically load the right driver. This parameter will be removed in a future Anaplan Connect version.");
                } else if (arg.equals("-jdbcproperties")) {
                    String propertiesFilePath = args[argi++];
                    JDBCConfig jdbcConfig = loadJdbcProperties(propertiesFilePath);
                    if (fileId != null) {
                        ServerFile serverFile = getServerFile(workspaceId, modelId,
                                                              fileId, true);
                        CellWriter cellWriter = null;
                        CellReader cellReader = null;
                        try {
                            cellWriter = serverFile.getUploadCellWriter(chunkSize);
                            cellReader = new JDBCCellReader(jdbcConfig)
                                    .connectAndExecute();
                            String[] row = cellReader.getHeaderRow();
                            cellWriter.writeHeaderRow(row);
                            int rowCount = 0;
                            do {
                                if (null != (row = cellReader.readDataRow())) {
                                    cellWriter.writeDataRow(row);
                                    ++rowCount;
                                }
                                somethingDone = true; // TBD
                            } while (null != row);
                            cellWriter.close();
                            cellWriter = null;
                            LOG.info("Transferred {} records to {}", rowCount, fileId);
                        } finally {
                            if (cellReader != null) {
                                cellReader.close();
                            }
                            if (cellWriter != null) {
                                cellWriter.abort();
                            }
                        }
                    } else if (exportId != null) {
                        ServerFile serverFile = getServerFile(workspaceId, modelId,
                                                              exportId, true);
                        if (serverFile != null) {
                            CellWriter cellWriter = null;
                            somethingDone = true;
                            Export export = getExport(workspaceId, modelId, exportId);
                            ExportMetadata emd = export.getExportMetadata();
                            InputStream inputStream = null;
                            int columnCount = emd.getColumnCount();
                            int transferredrows = 0;
                            int[] mapcols = new int[columnCount];
                            String separator = emd.getSeparator();
                            //build map for metadata for exports
                            HashMap<String, Integer> headerName = new HashMap();
                            for (int i = 0; i < emd.getHeaderNames().length; i++) {
                                headerName.put(emd.getHeaderNames()[i], i);
                            }
                            for (int k = 0; k < maxRetryCount; k++) {
                                try {
                                    List<ChunkData> chunkList = serverFile.getChunks();
                                    //jdbc params exists
                                    if (jdbcConfig.getJdbcParams() != null && jdbcConfig.getJdbcParams().length > 0
                                            && !jdbcConfig.getJdbcParams()[0].equals("")) {
                                        mapcols = new int[jdbcConfig.getJdbcParams().length];
                                        //extract matching anaplan columns
                                        for (int i = 0; i < jdbcConfig.getJdbcParams().length; i++) {
                                            String paramName = ((String) jdbcConfig.getJdbcParams()[i]).trim();
                                            if (headerName.containsKey(paramName)) {
                                                mapcols[i] = headerName.get(paramName);
                                            } else {
                                                LOG.debug("{} from JDBC properties file is not a valid column in Anaplan", jdbcConfig.getJdbcParams()[i]);
                                                throw new AnaplanAPIException("Please make sure column names in jdbcproperties file match with the exported columns on Anaplan");
                                            }
                                        }
                                    }
                                    //Retry Fix
                                    cellWriter = new JDBCCellWriter(jdbcConfig);
                                    for (ChunkData chunk : chunkList) {
                                        byte[] chunkContent = serverFile.getChunkContent(chunk.getId());
                                        if (chunkContent == null) {
                                            throw new NoChunkError(chunk.getId());
                                        }
                                        inputStream = new ByteArrayInputStream(chunkContent);
                                        transferredrows = cellWriter.writeDataRow(exportId, maxRetryCount, retryTimeout, inputStream, chunkList.size(), chunk.getId(), mapcols, columnCount, separator);
                                    }
                                    if (transferredrows != 0) {
                                        LOG.info("Transferred {} records to {}", transferredrows, jdbcConfig.getJdbcConnectionUrl());
                                    } else if (transferredrows == 0) {
                                        LOG.info("No records were transferred to {}", jdbcConfig.getJdbcConnectionUrl());
                                    }
                                    k = maxRetryCount;
                                } catch (AnaplanAPIException ape) {
                                    LOG.error(ape.getMessage());
                                    k = maxRetryCount;
                                } catch (Exception e) {
                                    AnaplanJdbcRetryer anaplanJdbcRetryer = new AnaplanJdbcRetryer((long) (retryTimeout * 1000),
                                                                                                   (long) Constants.MAX_RETRY_TIMEOUT_SECS * 1000,
                                                                                                   FeignApiRetryer.DEFAULT_BACKOFF_MULTIPLIER);
                                    Long interval = anaplanJdbcRetryer.nextMaxInterval(k);
                                    try {
                                        LOG.debug("Could not connect to the database! Will retry in {} seconds ", interval / 1000);
                                        // do not retry if we get any other error
                                        Thread.sleep(interval);
                                    } catch (InterruptedException e1) {
                                        // we still want to retry, even though sleep was interrupted
                                        LOG.debug("Sleep was interrupted.");
                                    }
                                } finally {
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }
                                    if (cellWriter != null) {
                                        cellWriter.close();
                                    }
                                }
                            }
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
            //LOG.debug("{}", Throwables.getStackTraceAsString(thrown));
            if (!(thrown instanceof InterruptedException)) {
                // Some brevity for those who don't
                LOG.error(Utils.formatThrowable(thrown));
            }
            // System.exit causes abrupt termination, but the status is useful
            // when run from an automated script.
            closeDown();
            System.exit(1);
        }
    }

    private static void closeDown() {
        if (service != null && Task.getRunningTask() == null) {
            service.close();
            service = null;
        }
    }

    /**
     * Validates the chunk-size value and then converts it to bytes and stores it.
     *
     * @param value
     */
    static int fetchChunkSize(String value) {
        Integer chSize;
        try {
            chSize = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Chunksize needs to be a whole number!", e);
        }
        if (chSize > MAX_CHUNK_SIZE || chSize < MIN_CHUNK_SIZE) {
            throw new IllegalArgumentException("Chunksize [" + chSize
                                                       + "] cannot be less than " + MIN_CHUNK_SIZE
                                                       + "MB or larger than " + MAX_CHUNK_SIZE + "MB");
        }
        chunkSize = 1000 * 1000 * chSize;  // MB to bytes
        return chunkSize;
    }

    private static int fetchMaxRetryCount(String value) {
        Integer maxRetryCount;
        try {
            maxRetryCount = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Provided Max. Retry Count is not a number!");
        }
        if (maxRetryCount > Constants.MAX_RETRY_COUNT || maxRetryCount < Constants.MIN_RETRY_COUNT) {
            throw new IllegalArgumentException("Max-Retry count can only be within the range ["
                                                       + Constants.MIN_RETRY_COUNT + ", " + Constants.MAX_RETRY_COUNT + "]");
        }
        return maxRetryCount;
    }

    private static int fetchHttpTimeout(String value) {
        Integer httpTimeout;
        try {
            httpTimeout = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Provided Http-timeout is not a number!");
        }
        if (httpTimeout > Constants.MAX_HTTP_CONNECTION_TIMEOUT_SECS || httpTimeout < Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS) {
            throw new IllegalArgumentException("Http-Timeout can only be within the range ["
                                                       + Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS + ", " + Constants.MAX_HTTP_CONNECTION_TIMEOUT_SECS + "]");
        }
        return httpTimeout;
    }

    private static int fetchRetryTimeout(String value) {
        Integer retryTimeout;
        try {
            retryTimeout = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Provided Retry-Timeout is not a number");
        }
        if (retryTimeout > Constants.MAX_RETRY_TIMEOUT_SECS || retryTimeout < Constants.MIN_RETRY_TIMEOUT_SECS) {
            throw new IllegalArgumentException("Retry timeout can only be within the range ["
                                                       + Constants.MIN_RETRY_TIMEOUT_SECS + ", " + Constants.MAX_RETRY_TIMEOUT_SECS + "]");
        }
        return retryTimeout;
    }

    /**
     * Retrieve any dump file(s) from the server following an import or process
     * containing 1+ imports. If the result is for a process, then
     * outputLocation will be a directory.
     *
     * @param taskResult     the result of running the task
     * @param outputLocation the location in which to store the data
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
                        LOG.info("Dump file written to \"{}\"", nestedFile);
                    }
                    ++index;
                }

            } else if (taskResult.isFailureDumpAvailable()) {
                ServerFile failureDump = taskResult.getFailureDump();
                failureDump.downLoad(outputLocation, true);
                LOG.info("Dump file written to \"" + outputLocation
                                 + "\"");
            }
        } else {
            LOG.info("No dump file is available.");
            if (outputLocation.exists() && !outputLocation.isDirectory()) {
                outputLocation.delete();
            }
        }
    }

    /**
     * Locate or optionally create a server file on the server. An error message
     * will be produced if the workspace, model or (if create is false) server
     * file cannot be located.
     *
     * @param workspaceId the name or ID of the workspace
     * @param modelId     the name or ID of the model
     * @param fileId      the name or ID of the server file
     * @param create      if true and the file does not exist on the server, create a
     *                    new file
     * @return the server file, or null if the workspace, model or (if create is
     * false) server file could not be located.
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
            LOG.error("A file ID must be provided");
            return null;
        }
        ServerFile serverFile = model.getServerFile(fileId);
        if (serverFile == null) {
            if (create) {
                serverFile = model.createServerFileImportDataSource(fileId,
                                                                    "Anaplan Connect");
            } else {
                LOG.error("File \"" + fileId
                                  + "\" not found in workspace " + workspaceId
                                  + ", model " + modelId);
            }
        }
        // Set proper encoding based on what server sends back
        if (serverFile!=null && serverFile.getData()!=null && serverFile.getData().getEncoding()!=null) {
            System.setProperty("file.encoding",serverFile.getData().getEncoding());
        }
        return serverFile;
    }

    /**
     * Locate a import definition on the server. An error message will be
     * produced if the workspace, model or import definition cannot be located.
     *
     * @param workspaceId the name or ID of the workspace
     * @param modelId     the name or ID of the model
     * @param importId    the name or ID of the import definition
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
            LOG.error("An import ID, code or name must be provided");
        }
        Import serverImport = model.getImport(importId);
        if (serverImport == null) {
            LOG.error("Import \"" + importId
                              + "\" not found in workspace " + workspaceId + ", model "
                              + modelId);
        }
        return serverImport;
    }

    /**
     * Locate a saved export on the server. An error message will be produced if
     * the workspace, model or saved export cannot be located.
     *
     * @param workspaceId the name or ID of the workspace
     * @param modelId     the name or ID of the model
     * @param exportId    the name or ID of the saved export
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
            LOG.error("An export ID, code or name must be provided");
        }
        Export serverExport = model.getExport(exportId);
        if (serverExport == null) {
            LOG.error("Export \"" + exportId
                              + "\" not found in workspace " + workspaceId + ", model "
                              + modelId);
        }
        return serverExport;
    }

    /**
     * Locate a saved action on the server. An error message will be produced if
     * the workspace, model or saved action cannot be located.
     *
     * @param workspaceId the name or ID of the workspace
     * @param modelId     the name or ID of the model
     * @param actionId    the name or ID of the saved action
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
            LOG.error("An action ID, code or name must be provided");
        }
        Action serverAction = model.getAction(actionId);
        if (serverAction == null) {
            LOG.error("Action \"" + actionId
                              + "\" not found in workspace " + workspaceId + ", model "
                              + modelId);
        }
        return serverAction;
    }

    /**
     * Locate a process definition on the server. An error message will be
     * produced if the workspace, model or process definition cannot be located.
     *
     * @param workspaceId the name or ID of the workspace
     * @param modelId     the name or ID of the model
     * @param processId   the name or ID of the process definition
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
            LOG.error("A process ID, code or name must be provided");
        }
        Process serverProcess = model.getProcess(processId);
        if (serverProcess == null) {
            LOG.error("Process \"" + processId
                              + "\" not found in workspace " + workspaceId + ", model "
                              + modelId);
        }
        return serverProcess;
    }

    /**
     * Locate a saved view on the server. An error message will be produced if
     * the workspace, model, module or saved view cannot be located.
     *
     * @param workspaceId the name or ID of the workspace
     * @param modelId     the name or ID of the model
     * @param moduleId    the name or ID of the module
     * @param viewId      the name or ID of the saved view
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
            LOG.error("A view ID must be provided");
            return null;
        }
        View view = module.getView(viewId);
        if (view == null) {
            LOG.error("View \"" + viewId
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
     * @param workspaceId the name or ID of the workspace
     * @param modelId     the name or ID of the model
     * @param moduleId    the name or ID of the module
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
            LOG.error("A module ID must be provided");
            return null;
        }
        Module module = model.getModule(moduleId);
        if (module == null) {
            LOG.error("Module \"" + moduleId
                              + "\" not found in workspace \"" + workspaceId
                              + "\", model \"" + modelId + "\"");
        }
        return module;
    }

    /**
     * Locate a model on the server. An error message will be produced if the
     * workspace or model cannot be located.
     *
     * @param workspaceId the name or ID of the workspace
     * @param modelId     the name or ID of the model
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
            LOG.error("A model ID must be provided");
            return null;
        }
        Model model = workspace.getModel(modelId);
        if (model == null) {
            LOG.error("Model \"" + modelId
                              + "\" not found in workspace \"" + workspaceId + "\"");
        }
        return model;
    }

    /**
     * Locate a workspace on the server. An error message will be produced if
     * the workspace cannot be located.
     *
     * @param workspaceId the name or ID of the workspace
     * @return the workspace, or null if not found
     * @since 1.3
     */
    protected static Workspace getWorkspace(String workspaceId)
            throws AnaplanAPIException {
        if (workspaceId == null || workspaceId.isEmpty()) {
            LOG.error("A workspace ID must be provided");
            return null;
        }
        Workspace result = getService().getWorkspace(workspaceId);
        if (result == null) {
            LOG.error("Workspace \"" + workspaceId
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
            ConnectionProperties props = new ConnectionProperties();
            props.setApiServicesUri(serviceLocation);
            props.setAuthServiceUri(getAuthServiceUri());
            props.setApiCredentials(getServiceCredentials());
            props.setRetryTimeout(retryTimeout);
            props.setMaxRetryCount(maxRetryCount);
            props.setHttpTimeout(httpConnectionTimeout);
            if (proxyLocationSet) {
                props.setProxyLocation(proxyLocation);
                props.setProxyCredentials(getProxyCredentials());
            }
            service = new Service(props);
            service.authenticate();
        }
        return service;
    }

    /**
     * Gathers the Anaplan service credentials.
     *
     * @return the credentials for the Anaplan service, either obtained from
     * getUsername() and getPassPhrase() or from getCertificate(), depending
     * on the authentication method being used
     * @throws AnaplanAPIException
     * @since 1.3.1
     */
    protected static Credentials getServiceCredentials() throws AnaplanAPIException {
        if (userCertificateAuthentication) {
            try {
                return new Credentials(getCertificate(), getPrivateKey());
            } catch (Exception e) {
                throw new AnaplanAPIException("Could not initialise service credentials", e);
            }
        } else {
            return new Credentials(getUsername(), getPassphrase());
        }
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
        if (passphrase == null || passphrase.isEmpty() || passphrase == "?") {
            Console console = System.console();
            if (console != null) {
                passphrase = new String(console.readPassword("Password:"));
            } else {
                throw new UnsupportedOperationException(
                        "Password/Passphrase must be specified");
            }
        }
        return passphrase;
    }

    /**
     * Fetches the Anaplan Auth-service URL. If null, empty or '?' and a system
     * console is available, then the user will be prompted for a value.
     *
     * @return Anaplan Auth-Service URL
     */
    protected static URI getAuthServiceUri() {
        if (authServiceUrl == null || authServiceUrl.toString().isEmpty() || authServiceUrl.toString().equals("?")) {
            Console console = System.console();
            if (console != null) {
                try {
                    authServiceUrl = new URI(console.readLine("Anaplan Auth-Service URL:"));
                } catch (URISyntaxException e) {
                    throw new AnaplanAPIException("Unable to parse Auth-Service URI: ", e);
                }
            } else {
                throw new UnsupportedOperationException("Auth-Service URL must be specified!");
            }
        }
        return authServiceUrl;
    }

    /**
     * Set the service location. This is the production API server by default.
     *
     * @param serviceLocation the URI of the API service endpoint
     * @since 1.3
     */
    protected static void setServiceLocation(URI serviceLocation) {
        Program.serviceLocation = serviceLocation;
    }

    /**
     * Set the proxy location. If null, no proxy will be used, and a direct
     * connection to the internet is assumed. If not set, proxy settings will be automatically detected, based on the platform.
     *
     * @param proxyLocation the URI of the proxy, or null if no proxy is to be used
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
     * @param debugLevel an integer debug level, nominally in the range 0..2
     * @since 1.3
     */
    protected static void setDebugLevel(int debugLevel) {
        Program.debugLevel = debugLevel;
    }

    /**
     * Set the username of the Anaplan account for the service to use.
     *
     * @param username the email address of the Anaplan user
     * @since 1.3
     */
    protected static void setUsername(String username) {
        Program.username = username;
    }

    /**
     * Set the passphrase of the Anaplan account for the service to use.
     *
     * @param passphrase the passphrase of the Anaplan user
     * @since 1.3
     */
    protected static void setPassphrase(String passphrase) {
        Program.passphrase = passphrase;
    }

    /**
     * Set the username for an authenticating proxy
     *
     * @param username the username for the authenticating proxy
     * @since 1.3.1
     */
    protected static void setProxyUsername(String username) {
        Program.proxyUsername = username;
        proxyUsernameSet = true;
    }

    /**
     * Set the passphrase for an authenticating proxy
     *
     * @param passphrase the passphrase for the authenticating proxy
     * @since 1.3.1
     */
    protected static void setProxyPassphrase(String passphrase) {
        Program.proxyPassphrase = passphrase;
    }

    /**
     * Loads the X509 certificate to be used for authentication from file, or from Keystore
     *
     * @return the certificate
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws IOException
     * @since 1.3.2
     */
    protected static X509Certificate getCertificate() throws CertificateException, KeyStoreException, IOException {
        String certificatePath = getCertificatePath();
        String keyStorePath = getKeyStorePath();
        if (certificatePath != null) {
            File certificateFile = new File(certificatePath);
            if (certificateFile.isFile()) {
                // load certificate from file
                return loadCertificateFromFile(certificateFile);
            } else {
                throw new RuntimeException("The specified certificate path '" + certificatePath + "' is invalid");
            }
        } else if (keyStorePath != null) {
            return new KeyStoreManager().getKeyStoreCertificate(keyStorePath, getKeyStorePassword(), getKeyStoreAlias());
        } else {
            // should not happen
            throw new RuntimeException("Could not load a certificate for authentication");
        }
    }

    /**
     * Private key are always stored in keystore files, so fetches that using password and alias.
     *
     * @return
     * @throws GeneralSecurityException
     */
    private static RSAPrivateKey getPrivateKey() throws GeneralSecurityException {
        String privateKeyPath = getPrivateKeyPath();
        String keyStorePath = getKeyStorePath();
        String keyStorePrivateKeyAlias = getKeyStoreAlias();
        if (privateKeyPath != null) {
            File privateKeyFile = new File(privateKeyPath);
            if (privateKeyFile.isFile()) {
                //load privateKey from file
                return loadPrivateKeyFromFile(privateKeyPath, passphrase);
            } else {
                throw new RuntimeException("The specified privateKey path '" + privateKeyPath + "' is invalid");
            }
        } else if (keyStorePath != null && keyStorePrivateKeyAlias != null) {
            return new KeyStoreManager().getKeyStorePrivateKey(keyStorePath, getKeyStorePassword(), keyStorePrivateKeyAlias);
        } else {
            throw new RuntimeException("Could not load the privateKey for authentication. Please check the privateKey parameters in your input.");
        }
    }

    /**
     * Returns the certificate path set using setCertificatePath()
     *
     * @return the certificate path
     * @since 1.3.2
     */
    protected static String getCertificatePath() {
        return certificatePath;
    }

    /**
     * Set the path to the certificate
     *
     * @param certificatePath
     * @since 1.3.2
     */
    protected static void setCertificatePath(String certificatePath) {
        Program.certificatePath = certificatePath;
        Program.userCertificateAuthentication = true;
    }

    /**
     * Returns the privateKey path set using setPrivateKeyPath()
     *
     * @return the privateKeyPath
     */
    public static String getPrivateKeyPath() {
        return privateKeyPath;
    }

    /**
     * Set the path to the privateKey
     *
     * @param privateKeyPath
     */
    public static void setPrivateKeyPath(String privateKeyPath) {
        Program.privateKeyPath = privateKeyPath;
        Program.userCertificateAuthentication = true;
    }

    /**
     * Returns the key store path set using setKeyStorePath()
     *
     * @return the key store path
     * @since 1.3.2
     */
    protected static String getKeyStorePath() {
        return keyStorePath;
    }

    /**
     * Set the path to the key store
     *
     * @param keyStorePath
     * @since 1.3.2
     */
    protected static void setKeyStorePath(String keyStorePath) {
        Program.keyStorePath = keyStorePath;
        Program.userCertificateAuthentication = true;
    }

    /**
     * Returns the key store alias set using setKeyStoreAlias()
     *
     * @return the key store alias
     * @since 1.3.2
     */
    protected static String getKeyStoreAlias() {
        return keyStoreAlias;
    }

    /**
     * Set the alias of the key store entry referring to the public
     * certificate and private key pair used by the client to authenticate
     * with the server
     *
     * @param keyStoreAlias
     * @since 1.3.2
     */
    protected static void setKeyStoreAlias(String keyStoreAlias) {
        Program.keyStoreAlias = keyStoreAlias;
    }

    /**
     * Returns the key store password set using setKeyStorePassword().
     * If not provided, and the password file is not available,
     * then the user will be securely prompted for a value (provided
     * a system console is available)
     *
     * @return the key store password
     * @since 1.3.2
     */
    protected static String getKeyStorePassword() {
        Path userHomeDirectory = null;
        if ("?".equals(keyStorePassword)) {
            promptForKeystorePassword();
        } else if (keyStorePassword == null || keyStorePassword.isEmpty()) {
            // first try the password file
            try {
                userHomeDirectory = Paths.get(System.getProperty("user.home")).toAbsolutePath();
            } catch (InvalidPathException e) {
                throw new BadSystemPropertyError(e);
            }
            File pwFile = new File(userHomeDirectory.toString(), Constants.PW_FILE_PATH_SEGMENT);
            if (pwFile.isFile()) {
                try {
                    String rawPassword = readFileContents(pwFile);
                    return EncodingUtils.decodeAndXor(rawPassword);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Password file could not be read");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Password file could not be read");
                }
            } else {
                promptForKeystorePassword();
            }
        }
        return keyStorePassword;
    }

    /**
     * Set the password of the key store
     *
     * @param keyStorePassword
     * @since 1.3.2
     */
    protected static void setKeyStorePassword(String keyStorePassword) {
        Program.keyStorePassword = keyStorePassword;
    }

    /**
     * Check and if necessary prompt for a value. If propertyValue is null,
     * empty or '?', and the program is associated with a terminal (ie
     * <tt>System.console() != null</tt>), then prompt the user using the value
     * in propertyName, followed by a colon. If password is true, then the value
     * input by the user will not be echoed.
     *
     * @param propertyName  the value to be displayed to the user if prompted
     * @param propertyValue the value supplied
     * @param password      flag to suppress echoing
     * @return the value passed in propertyValue, or the value entered by the
     * user if it was necessary to prompt
     * @throws UnsupportedOperationException if there is no terminal associated with the program and a
     *                                       value was not supplied.
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
                    propertyValue = console.readLine(propertyName + ":");
                }
            } else {
                throw new UnsupportedOperationException("Value for "
                                                                + propertyName + " must be specified");
            }
        }
        return propertyValue;
    }

    /**
     * Reads the contents of a text file
     *
     * @param file
     * @return the file contents
     * @throws FileNotFoundException if the file could not be found
     */
    private static String readFileContents(File file) throws FileNotFoundException {
        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        try {
            while (scanner.hasNext()) {
                fileContents.append(scanner.next());
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }

    /**
     * Loads a {@link X509Certificate} from a file
     *
     * @param certificateFile
     * @return a X509Certificate
     * @throws CertificateException
     * @throws FileNotFoundException
     */
    private static X509Certificate loadCertificateFromFile(File certificateFile) throws
            CertificateException, FileNotFoundException {
        // loading certificate chain
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        InputStream certificateStream = new FileInputStream(certificateFile);

        Collection<? extends Certificate> c = certificateFactory.generateCertificates(certificateStream);
        Certificate[] certs = new Certificate[c.toArray().length];
        if (c.size() == 1) {
            return (X509Certificate) c.iterator().next();
        } else {
            throw new RuntimeException("Certificate file must contain only one certificate (chain length was " + certs.length + ")");
        }
    }

    /**
     * Loads a {privateKey} from a file
     *
     * @param privateKeyPath
     * @param passphrase
     * @return a RSAPrivateKey
     */

    public static RSAPrivateKey loadPrivateKeyFromFile(String privateKeyPath, String passphrase) {
        try {
            if (passphrase.isEmpty()) {
                PemReader pemReader = new PemReader(new FileReader(privateKeyPath));
                PemObject pemObject = pemReader.readPemObject();
                byte[] pemContent = pemObject.getContent();
                pemReader.close();
                PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pemContent);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return (RSAPrivateKey) kf.generatePrivate(encodedKeySpec);
            }
            Security.addProvider(new BouncyCastleProvider());
            PEMParser pemParser = new PEMParser(new FileReader(privateKeyPath));
            PKCS8EncryptedPrivateKeyInfo encryptedKeyPair = (PKCS8EncryptedPrivateKeyInfo) pemParser.readObject();
            InputDecryptorProvider pkcs8Prov = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider("BC").build(passphrase.toCharArray());
            PrivateKeyInfo privateKeyInfo = encryptedKeyPair.decryptPrivateKeyInfo(pkcs8Prov);
            JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();
            return (RSAPrivateKey) jcaPEMKeyConverter.setProvider("BC").getPrivateKey(privateKeyInfo);
        } catch (Exception e) {
            throw new PrivateKeyException(privateKeyPath + ", " + e);
        }
    }

    private static void promptForKeystorePassword() {
        Console console = System.console();
        if (console != null) {
            keyStorePassword = new String(console.readPassword("Key store password:"));
        } else {
            throw new UnsupportedOperationException("Key store password must be specified");
        }
    }

    private static JDBCConfig loadJdbcProperties(String jdbcPropertiesPath) {

        Properties jdbcProps = new Properties();
        try {
            jdbcProps.load(new FileInputStream(jdbcPropertiesPath));
        } catch (IOException e) {
            throw new RuntimeException("Error reading JDBC Properties file", e);
        }

        JDBCConfig jdbcConfig = new JDBCConfig();
        jdbcConfig.setJdbcConnectionUrl(jdbcProps.getProperty("jdbc.connect.url"));
        jdbcConfig.setJdbcUsername(jdbcProps.getProperty("jdbc.username"));
        jdbcConfig.setJdbcPassword(jdbcProps.getProperty("jdbc.password"));
        if (fileId != null) {
            try {
                jdbcConfig.setJdbcFetchSize(Integer.parseInt(jdbcProps.getProperty("jdbc.fetch.size")));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid JDBC Fetch-size provided in properties.");
            }
        }
        jdbcConfig.setStoredProcedure(Boolean.valueOf(jdbcProps.getProperty("jdbc.isStoredProcedure", "false")));
        jdbcConfig.setJdbcQuery(jdbcProps.getProperty("jdbc.query"));
        String paramsCsv = jdbcProps.getProperty("jdbc.params");
        assert paramsCsv != null : "Parameters required!";
        try {
            jdbcConfig.setJdbcParams(new CSVParser().parseLine(paramsCsv));
        } catch (IOException e) {
            throw new RuntimeException("Invalid params, unable to parse.", e);
        }

        return jdbcConfig;
    }

    private static void displayHelp() {
        Path userDirectory = null;
        try {
            userDirectory = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        } catch (InvalidPathException e) {
            throw new BadSystemPropertyError(e);
        }
        File passwordFile = new File(userDirectory.toString(), Constants.PW_FILE_PATH_SEGMENT);

        LOG.error("Options are:\n"
                          + "\n"
                          + "General:\n"
                          + "--------\n"
                          + "(-h|-help): display this help\n"
                          + "(-version): display version information\n"
                          + "(-d|-debug): Show more detailed output\n"
                          + "(-q|-quiet): Show less detailed output\n"
                          + "\n"
                          + "Connection:\n"
                          + "-----------\n"
                          + "(-s|-service) <service URI>: API service endpoint"
                          + " (defaults to https://api.anaplan.com/)\n"
                          + "(-u|-user) <username>[:<password>]"
                          + ": Anaplan user name + (optional) password\n"
                          + "(-auth|-authServiceUrl) <Auth Service URL>: Anaplan SSO server."
                          + "(-c|-certificate) <CA certificate filepath>"
                          + ": Path to user certificate used for authentication (an alternative to using a key store)\n"
                          + "(-pkey|-privatekey) <privatekey path>:<passphrase>"
                          + ": Path to user privatekey used for authentication (an alternative to using a key store) + passphrase\n"
                          + "(-k|-keystore) <keystore path>"
                          + ": Path to local key store containing user certificate(s) for authentication\n"
                          + "(-kp|-keystorepass) <keystore password>"
                          + ": Password for the key store (if not provided, password is read from obfuscated file '"
                          + passwordFile.getAbsolutePath()
                          + "', or prompted for)\n"
                          + "(-ka|-keystorealias) <keystore alias>"
                          + ": Alias of the public certificate in the specified key store\n"
                          + "(-v|-via) <proxy URI>: use specified proxy\n"
                          + "(-vu|-viauser) [<domain>[\\<workstation>]\\]<username>[:<password>]: use proxy credentials\n"
                          + "(-mrc|-maxretrycount): Max retry count for API calls\n"
                          + "(-rt|-retrytimeout): Retry timeout for Http client calls\n"
                          + "(-ct|-httptimeout): Http client connection timeout\n"
                          + "\n"
                          + "Workspace Contents:\n"
                          + "-------------------\n"
                          + "(-w|-workspace) (<id>|<name>): select a workspace by id/name\n"
                          + "(-m|-model) (<id>|<name>): select a model by id/name\n"
                          + "(-F|-files): list available server files in selected model\n"
                          + "(-f|-file) (<id>|<name>): select a server file by id/name\n"
                          + "(-ch|-chunksize): upload chunk-size number, defaults to 1048576.\n"
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
                          + "-emd: Describe layout of an export (metadata)\n"
                          + "\n"
                          + "JDBC:\n"
                          + "-----\n"
                          + "-loadclass <class name>: Load a Java class\n"
                          + "-jdbcproperties: Path to JDBC properties file.\n");
    }

    private static void displayVersion() {
        LOG.debug(Strings.repeat("=", 70));
        LOG.debug("Anaplan Connect {}.{}.{}", Constants.AC_MAJOR, Constants.AC_MINOR, Constants.AC_REVISION);
        LOG.debug("{} ({})/ ({})/", System.getProperty("java.vm.name"), System.getProperty("java.vendor"),
                  System.getProperty("java.vm.version"), System.getProperty("java.version"));
        LOG.debug("({}{})/{}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
        LOG.debug(Strings.repeat("=", 70));
    }
}
