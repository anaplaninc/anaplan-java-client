//   Copyright 2011, 2013 Anaplan Inc.
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

import com.anaplan.client.CellWriter.DataRow;
import com.anaplan.client.ListImpl.ListAction;
import com.anaplan.client.ListImpl.MetaContent;
import com.anaplan.client.auth.Credentials;
import com.anaplan.client.auth.KeyStoreManager;
import com.anaplan.client.auth.UnknownAuthenticationException;
import com.anaplan.client.dto.ChunkData;
import com.anaplan.client.dto.ExportMetadata;
import com.anaplan.client.dto.FileType;
import com.anaplan.client.dto.ListFailure;
import com.anaplan.client.dto.ListItem;
import com.anaplan.client.dto.ListItemParametersData;
import com.anaplan.client.dto.ListItemResultData;
import com.anaplan.client.dto.ModelData;
import com.anaplan.client.dto.ModuleData;
import com.anaplan.client.dto.ViewData;
import com.anaplan.client.dto.WorkspaceData;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.exceptions.BadSystemPropertyError;
import com.anaplan.client.exceptions.PrivateKeyException;
import com.anaplan.client.exceptions.WorkspaceNotFoundException;
import com.anaplan.client.jdbc.AnaplanJdbcRetryer;
import com.anaplan.client.jdbc.JDBCCellReader;
import com.anaplan.client.jdbc.JDBCCellWriter;
import com.anaplan.client.jdbc.JDBCConfig;
import com.anaplan.client.jdbc.JDBCUtils;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.Paginator;
import com.anaplan.client.transport.retryer.FeignApiRetryer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.opencsv.CSVParser;
import com.opencsv.CSVWriter;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * A command-line interface to the Anaplan Connect API library. Running the program with no arguments will display the
 * available options. This class also contains several static convenience methods that may be useful by other
 * alternative main-method implementations - these should extend this class to gain access to them as they have
 * protected access.
 */

public abstract class Program {

  private static final int MIN_CHUNK_SIZE = 1;
  private static final int MAX_CHUNK_SIZE = MIN_CHUNK_SIZE * 50;
  private static final Logger LOG = LoggerFactory.getLogger(Program.class);
  private static int debugLevel = 0;
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
  private static String fileType = null;
  private static String listId = null;
  private static String importId = null;
  private static String exportId = null;
  private static String actionId = null;
  private static String processId = null;
  private static String itemPropertiesPath = null;
  private static TaskResult lastResult = null;
  private static boolean somethingDone = false;
  private static boolean includeAll;
  private static boolean executeParamPresent;
  private static TaskParameters taskParameters = new TaskParameters();
  private static int chunkSize = fetchChunkSize(String.valueOf(MIN_CHUNK_SIZE));
  private static String[] pagesSplit;
  private static int maxRetryCount = Constants.MIN_RETRY_COUNT;
  private static int retryTimeout = Constants.MIN_RETRY_TIMEOUT_SECS;
  private static int httpConnectionTimeout = Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS;
  private static final String[] CSV_LOG_HEADER =
      new String[] {"Name", "Code", "failureType", "failureMessageDetails"};
  private static final String GET_JSON = "-get:json";
  private static final String OUTPUT = "-output:";
  private static final String DUMP_FILE_WRITTEN = "Dump file written to {}";
  private static final String ITEMS_IGNORED = "{} items ignored";

  /**
   * Parse and process the command line. The process will exit with status 1 if a serious error occurs; the exit status
   * will be zero otherwise.
   *
   * @param args the list of command-line arguments
   */
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
        JDBCConfig jdbcConfig = new JDBCConfig();
        if (Objects.equals(arg, "-h") || Objects.equals(arg, "-help")) {
          displayHelp();
          somethingDone = true;
        } else if (Objects.equals(arg, "-version")) {
          displayVersion();
          somethingDone = true;
        } else if (Objects.equals(arg, "-d") || Objects.equals(arg, "-debug")) {
          if (debugLevel++ == 0) {
            LogDebugUtils.enableDebugLogging();
            displayVersion();
          }
        } else if (Objects.equals(arg, "-MO") || Objects.equals(arg, "-modules")) {
          somethingDone = true;
          logModules();
        } else if (Objects.equals(arg, "-V") || Objects.equals(arg, "-views")) {
          somethingDone = true;
          Stream<ModelData> modelsStream =
              StreamSupport.stream(getService().getModels().spliterator(), false);
          if (modelId != null) {
            modelsStream = modelsStream
                .filter(model -> modelId.equals(model.getId()) || modelId.equals(model.getName()));
          }
          if (workspaceId != null) {
            modelsStream = modelsStream.filter(model -> {
              String modelWorkspaceId = model.getCurrentWorkspaceId();
              Workspace modelWorkspace = service.getWorkspace(modelWorkspaceId);
              if (modelWorkspace == null) {
                return false;
              }
              return workspaceId.equals(modelWorkspace.getId()) ||
                  workspaceId.equals(modelWorkspace.getName());
            });
          }
          modelsStream = modelsStream.sorted(Comparator.comparing(ModelData::getCurrentWorkspaceId)
              .thenComparing(ModelData::getId));

          List<ModelData> models = modelsStream.collect(Collectors.toList());
          // If the current user is a visitor user, they may not get data about the used workspace/model so we'll
          // just try with the provided input
          if (models.isEmpty()) {
            Model model = getModel(workspaceId, modelId);
            if (model != null) {
              models = Collections.singletonList(model.getData());
            }
          }
          for (ModelData model : models) {
            logModuleViews(model);
          }
        } else if (Objects.equals(arg, "-W") || Objects.equals(arg, "-workspaces")) {
          somethingDone = true;
          Iterable<Workspace> workspaces = getService().getWorkspaces();
          String log = Utils.formatTSV("WS_ID", "WS_NAME", "WS_ALLOCATED_SIZE", "WS_SIZE");
          LOG.info(log);
          for (Workspace workspace : workspaces) {
            log = Utils
                .formatTSV(workspace.getId(), workspace.getName(), workspace.getSizeAllowance(),
                    workspace.getCurrentSize());
            LOG.info(log);
          }
        } else if (Objects.equals(arg, "-M") || Objects.equals(arg, "-models")) {
          somethingDone = true;
          Map<String, String> workspaceNames = StreamSupport
              .stream(getService().getWorkspaces().spliterator(), false)
              .collect(Collectors.toMap(Workspace::getId, Workspace::getName));
          Iterable<ModelData> models = getService().getModels();
          String log = Utils.formatTSV("WS_ID", "WS_NAME", "MODEL_ID", "MODEL_NAME", "MODEL_SIZE");
          LOG.info(log);
          for (ModelData model : models) {
            String workspaceId = model.getCurrentWorkspaceId();
            String workspaceName = Optional.ofNullable(workspaceNames.get(workspaceId))
                .orElse("");
            log = Utils.formatTSV(workspaceId, workspaceName, model.getId(), model.getName(),
                model.getMemoryUsage());
            LOG.info(log);
          }
        } else if (Objects.equals(arg, "-F") || Objects.equals(arg, "-files")) {
          somethingDone = true;
          Model model = getModel(workspaceId, modelId);
          if (model != null) {
            String log;
            for (ServerFile serverFile : model.getServerFiles()) {
              log = Utils.formatTSV(
                  serverFile.getId(),
                  serverFile.getCode(),
                  serverFile.getName());
              LOG.info(log);
            }
          }
        } else if (Objects.equals(arg, "-I") || Objects.equals(arg, "-imports")) {
          somethingDone = true;
          Model model = getModel(workspaceId, modelId);
          if (model != null) {
            String log;
            for (Import serverImport : model.getImports()) {
              log = Utils.formatTSV(
                  serverImport.getId(),
                  serverImport.getCode(),
                  serverImport.getName(),
                  serverImport.getImportType(),
                  serverImport.getSourceFileId());
              LOG.info(log);
            }
          }
        } else if (Objects.equals(arg, "-A") || Objects.equals(arg, "-actions")) {
          somethingDone = true;
          Model model = getModel(workspaceId, modelId);
          if (model != null) {
            String log;
            for (Action serverAction : model.getActions()) {
              log = Utils.formatTSV(
                  serverAction.getId(),
                  serverAction.getCode(),
                  serverAction.getName());
              LOG.info(log);
            }
          }
        } else if (Objects.equals(arg, "-E") || Objects.equals(arg, "-exports")) {
          somethingDone = true;
          Model model = getModel(workspaceId, modelId);
          if (model != null) {
            String log;
            for (Export serverExport : model.getExports()) {
              log = Utils.formatTSV(
                  serverExport.getId(),
                  serverExport.getCode(),
                  serverExport.getName());
              LOG.info(log);
            }
          }
        } else if (Objects.equals(arg, "-P") || Objects.equals(arg, "-processes")) {
          somethingDone = true;
          Model model = getModel(workspaceId, modelId);
          if (model != null) {
            String log;
            for (Process serverProcess : model.getProcesses()) {
              log = Utils.formatTSV(
                  serverProcess.getId(),
                  serverProcess.getCode(),
                  serverProcess.getName());
              LOG.info(log);
            }
          }
        } else if (Objects.equals(arg, "-L") || "-lists".equals(arg)) {
          if (argi == args.length) {
            somethingDone = true;
            Service service = getService();
            Model model = getModel(workspaceId, modelId);
            if (service != null && model != null) {
              service
                  .exportListNames(fileType, fileId, model.getCurrentWorkspaceId(), model.getId());
            }
          }
        } else if (Objects.equals(arg, "-l") || Objects.equals(arg, "-list")) {
          listId = args[argi++];
          if (argi >= args.length) {
            somethingDone = true;
            Service service = getService();
            Model model = getModel(workspaceId, modelId);
            if (service != null) {
              service.exportListMetadata(fileType, fileId, model.getCurrentWorkspaceId(),
                  model.getId(), listId);
            }
          }
        } else if (Objects.equals(arg, GET_JSON) || Objects.equals(arg, "-get:csv") || Objects
            .equals(arg, "-get:csv_sc") ||
            Objects.equals(arg, "-get:csv_mc")) {
          boolean supportedListTypes = Objects.equals(arg, GET_JSON) || Objects
              .equals(arg, "-get:csv");
          boolean supportedModuleTypes =
              Objects.equals(arg, GET_JSON) || Objects.equals(arg, "-get:csv_sc") || Objects
                  .equals(arg, "-get:csv_mc");
          fileType = arg.substring("-get:".length());
          if (argi < args.length) {
            fileId = args[argi];
          }
          argi++;
          somethingDone = true;
          Model model = getModel(workspaceId, modelId);
          if (argi >= args.length - 1) {
            if (executeParamPresent && moduleId != null && supportedModuleTypes) {
              Module module = getModule(workspaceId, modelId, moduleId);
              if (module != null) {
                module
                    .exportViewData(fileType, fileId, model.getCurrentWorkspaceId(), model.getId(),
                        viewId, pagesSplit);
              }
            } else if (supportedListTypes) {
              Service service = getService();
              if (service != null) {
                if (executeParamPresent) {
                  service.exportListItems(fileType, fileId, model.getCurrentWorkspaceId(),
                      model.getId(), listId, includeAll);
                } else if (supportedListTypes) {
                  if (listId == null) {
                    service.exportListNames(fileType, fileId, model.getCurrentWorkspaceId(),
                        model.getId());
                  } else {
                    service.exportListMetadata(fileType, fileId, model.getCurrentWorkspaceId(),
                        model.getId(), listId);
                  }
                }
              }
            }
          }
        } else if (Objects.equals(arg, "-emd")) {
          somethingDone = true;
          Export export = getExport(workspaceId, modelId, exportId);
          ExportMetadata emd = export.getExportMetadata();
          String delimiter = emd.getDelimiter();
          if ("\t".equals(delimiter)) {
            delimiter = "\\t";
          }
          String exportName = export.getName();
          int columnCount = emd.getColumnCount();
          int rowCount = emd.getRowCount();
          String exportFormat = emd.getExportFormat();
          String encoding = emd.getEncoding();
          String separator = emd.getSeparator();
          LOG.info(
              "Export: {}\ncolumns: {}\nrows: {}\nformat: {}\ndelimiter: {}\nencoding: {}\nseparator: {}"
              , exportName, columnCount, rowCount, exportFormat, delimiter, encoding, separator);

          String[] headerNames = emd.getHeaderNames();
          DataType[] dataTypes = emd.getDataTypes();
          String[] listNames = emd.getListNames();

          String dataType;
          for (int i = 0; i < headerNames.length; i++) {
            dataType = dataTypes[i].toString();
            LOG.info(" col {}:\n  name: {}\n  type: {}\n  list: {}", i, headerNames[i], dataType, listNames[i]);
          }
        } else if (Objects.equals(arg, "-x:all") || Objects.equals(arg, "-execute:all")) {
          somethingDone = true;
          executeParamPresent = true;
          includeAll = true;
        } else if (Objects.equals(arg, "-x") || Objects.equals(arg, "-execute")) {
          executeParamPresent = true;
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
          } else if (listId != null) {
            // Performing list operations, like retrieving a list items
          } else if (moduleId != null) {
            // Performing module operations, like retrieving module view data
          } else {
            LOG.error("An import, export, action or "
                + "process must be specified before {}", arg);
          }

        } else if (Objects.equals(arg, "-gets") || Objects.equals(arg, "-getc")) {
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
              if (Objects.equals(arg, "-gets")) {
                InputStream inputStream = serverFile
                    .getDownloadStream();
                byte[] buffer = new byte[4096];
                int read;
                String stringBuilder = "";
                do {
                  if (0 < (read = inputStream.read(buffer))) {
                    stringBuilder = stringBuilder.concat(new String(buffer));
                  }
                } while (-1 != read);
                LOG.info(stringBuilder);
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
                  String log = line.toString();
                  LOG.info(log);
                  row = cellReader.readDataRow();
                } while (null != row);
              }
            }
          }

        } else if (Objects.equals(arg, "-ch") || Objects.equals(arg, "-chunksize")) {
          fetchChunkSize(args[argi++]);
        } else if (Objects.equals(arg, "-pages")) {
          String delim = ",";
          String regex = "(?<!\\\\)" + Pattern.quote(delim);
          String pages = args[argi++];
          pagesSplit = pages.split(regex);
        } else if (Objects.equals(arg, "-auth") || Objects.equals(arg, "-authserviceurl")) {
          authServiceUrl = new URI(args[argi++]);
        } else if (Objects.equals(arg, "-puts") || Objects.equals(arg, "-putc")) {
          somethingDone = true;
          ServerFile serverFile = getServerFile(workspaceId, modelId,
              fileId, true);
          if (serverFile != null) {
            if (Objects.equals(arg, "-puts")) {
              OutputStream uploadStream = serverFile.getUploadStream(chunkSize);
              byte[] buf = new byte[4096];
              int read;
              do {
                if (0 < (read = System.in.read(buf))) {
                  uploadStream.write(buf, 0, read);
                }
              } while (-1 != read);
              uploadStream.close();
            } else {
              CellWriter cellWriter = serverFile.getUploadCellWriter(chunkSize);
              LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
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
            LOG.info("Upload to {} completed.", fileId);
          }
          // Now check the additional parameter is present before
          // processing consuming options
        } else if (argi >= args.length) {
          break;
        } else if (Objects.equals(arg, "-s") || Objects.equals(arg, "-service")) {
          serviceLocation = new URI(args[argi++]);
        } else if (Objects.equals(arg, "-u") || Objects.equals(arg, "-user")) {
          String auth = args[argi++];
          int colonPosition = auth.indexOf(':');
          if (colonPosition != -1) {
            setUsername(auth.substring(0, colonPosition));
            setPassphrase(auth.substring(colonPosition + 1));
          } else {
            setUsername(auth);
            setPassphrase("?");
          }
        } else if (Objects.equals(arg, "-v") || Objects.equals(arg, "-via")) {
          URI uri = new URI(args[argi++]);
          setProxyLocation(
              new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), null, null, null));
        } else if (Objects.equals(arg, "-vu") || Objects.equals(arg, "-viauser")) {
          String auth = args[argi++];
          int colonPosition = auth.indexOf(':');
          if (colonPosition != -1) {
            setProxyUsername(auth.substring(0, colonPosition));
            setProxyPassphrase(auth.substring(colonPosition + 1));
          } else {
            setProxyUsername(auth);
            setProxyPassphrase("?");
          }
        } else if (Objects.equals(arg, "-mrc") || Objects.equals(arg, "-maxretrycount")) {
          maxRetryCount = fetchMaxRetryCount(args[argi++]);
        } else if (Objects.equals(arg, "-rt") || Objects.equals(arg, "-retrytimeout")) {
          retryTimeout = fetchRetryTimeout(args[argi++]);
        } else if (Objects.equals(arg, "-ct") || Objects.equals(arg, "-httptimeout")) {
          httpConnectionTimeout = fetchHttpTimeout(args[argi++]);
        } else if (Objects.equals(arg, "-c") || Objects.equals(arg, "-certificate")) {
          String certificatePath = args[argi++];
          setCertificatePath(certificatePath);
        } else if (Objects.equals(arg, "-pkey") || Objects.equals(arg, "-privatekey")) {
          if (keyStorePath != null) {
            throw new IllegalArgumentException(
                "expected either the privatekey or the keystore arguments");
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
        } else if (Objects.equals(arg, "-k") || Objects.equals(arg, "-keystore")) {
          if (passphrase != null || privateKeyPath != null) {
            throw new IllegalArgumentException(
                "expected either the privatekey or keystore arguments");
          }
          String keyStorePath = args[argi++];
          setKeyStorePath(keyStorePath);
        } else if (Objects.equals(arg, "-ka") || Objects.equals(arg, "-keystorealias")) {
          String keyStoreAlias = args[argi++];
          setKeyStoreAlias(keyStoreAlias);
        } else if (Objects.equals(arg, "-kp") || Objects.equals(arg, "-keystorepass")) {
          String keyStorePassword = args[argi++];
          setKeyStorePassword(keyStorePassword);
        } else if (Objects.equals(arg, "-w") || Objects.equals(arg, "-workspace")) {
          workspaceId = args[argi++];
        } else if (Objects.equals(arg, "-m") || Objects.equals(arg, "-model")) {
          modelId = args[argi++];
        } else if (Objects.equals(arg, "-mo") || Objects.equals(arg, "-module")) {
          moduleId = args[argi++];
        } else if (Objects.equals(arg, "-vi") || Objects.equals(arg, "-view")) {
          viewId = args[argi++];
        } else if (Objects.equals(arg, "-f") || Objects.equals(arg, "-file")) {
          fileId = args[argi++];
        } else if (Objects.equals(arg, "-g") || Objects.equals(arg, "-get")) {
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
            ServerFile serverFile = getServerFile(workspaceId, modelId, sourceId, false);
            if (serverFile != null) {
              serverFile.downLoad(targetFile, true);
              LOG.info("The server file {} has been downloaded to {}", sourceId,
                  targetFile.getAbsolutePath());
            }
          }
        } else if (Objects.equals(arg, "-putItems:json") || Objects.equals(arg, "-putItems:csv")
            || Objects.equals(arg, "-putItems:jdbc") ||
            Objects.equals(arg, "-upsertItems:jdbc") || Objects.equals(arg, "-upsertItems:json") || Objects
            .equals(arg, "-upsertItems:csv")) {
          somethingDone = true;
          boolean upsert = arg.startsWith("-upsertItems:");
          String type = arg.startsWith("-putItems") ? arg.substring("-putItems:".length()) :
              arg.substring("-upsertItems:".length());
          final Path outputPath = getOutput(args, argi);
          String outputType = null;
          if (outputPath != null) {
            outputType = args[argi + 1].substring(OUTPUT.length());
          }
          ListItemResultData result = new ListItemResultData();
          result.setFailures(new ArrayList<>(0));
          ListImpl listImpl;
          final Path itemMapFile = ("".equals(itemPropertiesPath) || itemPropertiesPath == null) ?
              null : new File(itemPropertiesPath).toPath();
          if ("jdbc".equalsIgnoreCase(type)) {
            final Map<String, String> headerMap = getHeader(jdbcConfig, itemMapFile, args[argi++]);
            listImpl = new ListImpl(getService(), workspaceId, modelId, listId);
            result = JDBCUtils.doActionsItemsFromJDBC(jdbcConfig, listImpl, headerMap,
                ListAction.ADD, (itemPropertiesPath != null));
          } else {
            final File sourceFile = new File(args[argi++]);
            listImpl = new ListImpl(getService(), workspaceId, modelId, listId);

            result = listImpl.doActionToItems(sourceFile.toPath(), itemMapFile, FileType
                .valueOf(type.toUpperCase()), ListAction.ADD);
          }

          if (result != null) {
            LOG.info("{} items added to the list", result.getAdded());
            manageItemLog(upsert, outputPath, outputType, result, listImpl);
          }
          if (outputPath != null) {
            String log = outputPath.toString();
            LOG.info(DUMP_FILE_WRITTEN, log);
            argi += 2;
          }

        } else if (Objects.equals(arg, "-updateItems:json") || Objects
            .equals(arg, "-updateItems:csv") ||
            Objects.equals(arg, "-updateItems:jdbc")) {
          somethingDone = true;
          String type = arg.substring("-updateItems:".length());
          final Path outputPath = getOutput(args, argi);
          String outputType = null;
          if (outputPath != null) {
            outputType = args[argi + 1].substring(OUTPUT.length());
          }
          final ListImpl listImpl = new ListImpl(getService(), workspaceId, modelId, listId);
          ListItemResultData result = new ListItemResultData();
          result.setFailures(new ArrayList<>(0));
          final Path itemMapFile = ("".equals(itemPropertiesPath) || itemPropertiesPath == null)
              ? null : new File(itemPropertiesPath).toPath();
          if ("jdbc".equalsIgnoreCase(type)) {
            final Map<String, String> headerMap = getHeader(jdbcConfig, itemMapFile, args[argi++]);
            result = JDBCUtils
                .doActionsItemsFromJDBC(jdbcConfig, listImpl, headerMap, ListAction.UPDATE,
                    (itemPropertiesPath != null));
          } else {
            final File sourceFile = new File(args[argi++]);

            result = listImpl
                .doActionToItems(sourceFile.toPath(), itemMapFile,
                    FileType.valueOf(type.toUpperCase()), ListAction.UPDATE);
          }
          if (result != null) {
            String log = String.format("%d items updated in the list", result.getUpdated());
            LOG.info(log);
            if (result.getIgnored() > 0) {
              int ignored = result.getIgnored();
              LOG.info(ITEMS_IGNORED, ignored);
            }
          }
          if (outputPath != null) {
            addLogItemToOutput(result, outputPath, outputType, listImpl.getContent());
            argi += 2;
            String log = outputPath.toString();
            LOG.info(DUMP_FILE_WRITTEN, log);
          }
        } else if (Objects.equals(arg, "-deleteItems:json") || Objects.equals(arg, "-deleteItems:csv") ||
            Objects.equals(arg, "-deleteItems:jdbc")) {
          somethingDone = true;
          String type = arg.substring("-deleteItems:".length());
          final Path outputPath = getOutput(args, argi);
          String outputType = null;
          if (outputPath != null) {
            outputType = args[argi + 1].substring(OUTPUT.length());
          }
          final Path itemMapFile = ("".equals(itemPropertiesPath) || itemPropertiesPath == null) ?
              null : new File(itemPropertiesPath).toPath();
          ListItemResultData result = new ListItemResultData();
          result.setFailures(new ArrayList<>());
          if ("jdbc".equalsIgnoreCase(type)) {
            final Map<String, String> headerMap = getHeader(jdbcConfig, itemMapFile, args[argi++]);
            CellReader cellReader = null;
            try {
              final ListImpl listImpl = new ListImpl(getService(), workspaceId, modelId, listId);
              result = JDBCUtils.doActionsItemsFromJDBC(jdbcConfig, listImpl, headerMap,
                  ListAction.DELETE, (itemPropertiesPath != null));
              argi = handleDeleteLog(result, outputPath, outputType, listImpl.getContent(), argi);
            } finally {
              if (cellReader != null) {
                cellReader.close();
              }
            }
          } else {
            final File sourceFile = new File(args[argi++]);
            final ListImpl listImpl = new ListImpl(getService(), workspaceId, modelId, listId);
            result = listImpl.doActionToItems(sourceFile.toPath(), itemMapFile, FileType
                .valueOf(type.toUpperCase()), ListAction.DELETE);
            argi = handleDeleteLog(result, outputPath, outputType, listImpl.getContent(), argi);
          }
        } else if (Objects.equals(arg, "-p") || Objects.equals(arg, "-put")) {
          somethingDone = true;
          File sourceFile = new File(args[argi++]);
          String destId = fileId == null ? sourceFile.getName() : fileId;
          ServerFile serverFile = getServerFile(workspaceId, modelId,
              destId, true);
          if (serverFile != null) {
            serverFile.upLoad(sourceFile, true, chunkSize);
            LOG.info("The file \"{}\" has been uploaded as {}.", sourceFile, destId);
          }
        } else if (Objects.equals(arg, "-i") || Objects.equals(arg, "-import")) {
          importId = args[argi++];
          exportId = null;
          actionId = null;
          processId = null;
        } else if (Objects.equals(arg, "-e") || Objects.equals(arg, "-export")) {
          importId = null;
          exportId = args[argi++];
          actionId = null;
          processId = null;
        } else if (Objects.equals(arg, "-a") || Objects.equals(arg, "-action")) {
          importId = null;
          exportId = null;
          actionId = args[argi++];
          processId = null;
        } else if (Objects.equals(arg, "-pr") || Objects.equals(arg, "-process")) {
          importId = null;
          exportId = null;
          actionId = null;
          processId = args[argi++];
        } else if (Objects.equals(arg, "-xl") || Objects.equals(arg, "-locale")) {
          String[] localeName = args[argi++].split("_");
          taskParameters.setLocale(localeName[0], localeName.length > 0 ? localeName[1] : null);
        } else if (Objects.equals(arg, "-xc") || Objects.equals(arg, "-connectorproperty")) {
          String[] propEntry = args[argi++].split(":", 2);
          if (propEntry.length != 2) {
            throw new IllegalArgumentException(
                "expected " + arg + " [(<source>|<type>)/]property:(value|?)");
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
        } else if (Objects.equals(arg, "-im") || Objects.equals(arg, "-itemmappingproperty")) {
          itemPropertiesPath = Optional.ofNullable(args[argi++]).orElse("");
        } else if (Objects.equals(arg, "-xm") || Objects.equals(arg, "-mappingproperty")) {
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
        } else if (Objects.equals(arg, "-o") || Objects.equals(arg, "-output")) {
          File outputFile = new File(args[argi++]);
          retrieveOutput(lastResult, outputFile);
        } else if (Objects.equals(arg, "-loadclass")) {
          argi++;
          //Removing the usage of loadclass parameter
          LOG.error(
              "Warning : Loadclass parameter is deprecated starting in Anaplan Connect v1.4.4. Anaplan Connect will automatically load the right driver. This parameter will be removed in a future Anaplan Connect version.");
        } else if (arg.equals("-jdbcproperties")) {
          String propertiesFilePath = args[argi++];
          jdbcConfig = loadJdbcProperties(propertiesFilePath);
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
              } while (null != row && row.length > 0);
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
              if (export == null) {
                continue;
              }
              ExportMetadata emd = export.getExportMetadata();
              int columnCount = emd.getColumnCount();
              String separator = emd.getSeparator();
              //build map for metadata for exports
              HashMap<String, Integer> headerName = new HashMap<>();
              for (int i = 0; i < emd.getHeaderNames().length; i++) {
                headerName.put(emd.getHeaderNames()[i], i);
              }
              doTransfer(serverFile, jdbcConfig, cellWriter, headerName, separator, columnCount);
            }
          }
        } else {
          break;
        }
      }
      if (!somethingDone) {
        displayHelp();
      }
      closeDown();
    } catch (Exception thrown) {
      if (!(thrown instanceof InterruptedException)) {
        // Some brevity for those who don't
        LOG.error(Utils.formatThrowable(thrown));
      }
      // System.exit causes abrupt termination, but the status is useful
      // when run from an automated script.
      closeDown();
      Thread.currentThread().interrupt();
      System.exit(1);
    }
  }

  private static void doTransfer(final ServerFile serverFile, final JDBCConfig jdbcConfig,
      CellWriter cellWriter, final Map<String, Integer> headerName, final String separator,
      int columnCount)
      throws IOException {
    int transferredrows;
    int[] mapcols = new int[0];
    for (int k = 0; k < maxRetryCount; k++) {
      List<ChunkData> chunkList = serverFile.getChunks();
      try {
        //jdbc params exists
        if (jdbcConfig.getJdbcParams() != null && jdbcConfig.getJdbcParams().length > 0
            && !jdbcConfig.getJdbcParams()[0].equals("")) {
          mapcols = checkJDBCParams(jdbcConfig, headerName, jdbcConfig.getJdbcParams().length);
        }
        //Retry Fix
        cellWriter = new JDBCCellWriter(jdbcConfig);
        transferredrows = transferRow(chunkList, cellWriter, serverFile, separator, columnCount, mapcols);
        if (transferredrows != 0) {
          LOG.info("Transferred {} records to {}", transferredrows,
              jdbcConfig.getJdbcConnectionUrl());
        } else {
          LOG.info("No records were transferred to {}",
              jdbcConfig.getJdbcConnectionUrl());
        }
        break;
      } catch (AnaplanAPIException ape) {
        LOG.error(ape.getMessage());
        break;
      } catch (Exception e) {
        AnaplanJdbcRetryer anaplanJdbcRetryer = new AnaplanJdbcRetryer(
            (long) (retryTimeout * 1000),
            (long) Constants.MAX_RETRY_TIMEOUT_SECS * 1000,
            FeignApiRetryer.DEFAULT_BACKOFF_MULTIPLIER);
        long interval = anaplanJdbcRetryer.nextMaxInterval(k);
        waitFor(interval);
      } finally {
        if (cellWriter != null) {
          cellWriter.close();
        }
      }
    }
  }

  private static int[] checkJDBCParams(final JDBCConfig jdbcConfig, Map<String, Integer> headerName, int len) {
    int[] mapcols = new int[len];
    for (int i = 0; i < jdbcConfig.getJdbcParams().length; i++) {
      String paramName = ((String) jdbcConfig.getJdbcParams()[i]).trim();
      if (headerName.containsKey(paramName)) {
        mapcols[i] = headerName.get(paramName);
      } else {
        LOG.debug("{} from JDBC properties file is not a valid column in Anaplan",
            jdbcConfig.getJdbcParams()[i]);
        throw new AnaplanAPIException(
            "Please make sure column names in jdbcproperties file match with the exported columns on Anaplan");
      }
    }
    return mapcols;
  }

  private static void waitFor(long interval) {
    try {
      LOG.debug("Could not connect to the database! Will retry in {} seconds ",
          interval / 1000);
      // do not retry if we get any other error
      Thread.sleep(interval);
    } catch (InterruptedException e1) {
      // we still want to retry, even though sleep was interrupted
      LOG.debug("Sleep was interrupted.");
      Thread.currentThread().interrupt();
    }
  }

  private static int transferRow(final List<ChunkData> chunkList, final CellWriter cellWriter,
      final ServerFile serverFile, final String separator, final int columnCount,
      final int[] mapcols)
      throws IOException, SQLException {
    int transferredrows = 0;
    for (ChunkData chunk : chunkList) {
      byte[] chunkContent = serverFile.getChunkContent(chunk.getId());
      try (InputStream inputStream = new ByteArrayInputStream(chunkContent);) {
        final DataRow dataRow = new DataRow();
        dataRow.setInputStream(inputStream);
        dataRow.setChunkId(chunk.getId());
        dataRow.setSeparator(separator);
        dataRow.setColumnCount(columnCount);
        dataRow.setChunks(chunkList.size());
        dataRow.setRetryTimeout(retryTimeout);
        dataRow.setMaxRetryCount(maxRetryCount);
        dataRow.setExportId(exportId);
        dataRow.setMapcols(mapcols);
        transferredrows = cellWriter
            .writeDataRow(dataRow);
      }
    }
    return transferredrows;
  }

  private static Map<String, String> getHeader(final JDBCConfig jdbcConfig, final Path itemMapFile,
                                               final String propertiesFilePath)
      throws IOException {
    try (FileReader fileReader = new FileReader(propertiesFilePath)) {
      final Properties properties = new Properties();
      properties.load(fileReader);
      final JDBCConfig config = getJDBCConfig(properties);
      jdbcConfig.setJdbcQuery(config.getJdbcQuery());
      jdbcConfig.setJdbcPassword(config.getJdbcPassword());
      jdbcConfig.setJdbcUsername(config.getJdbcUsername());
      jdbcConfig.setJdbcFetchSize(config.getJdbcFetchSize());
      jdbcConfig.setJdbcParams(config.getJdbcParams());
      jdbcConfig.setStoredProcedure(config.isStoredProcedure());
      jdbcConfig.setJdbcConnectionUrl(config.getJdbcConnectionUrl());
      properties.clear();
      Map<String, String> mapValues = new HashMap<>();
      if (itemMapFile != null) {
        mapValues = Utils
            .getPropertyFile(new FileInputStream(itemMapFile.toFile()));
      }
      properties.putAll(mapValues);
      return getHeaderMap(properties);
    }
  }

  private static int handleDeleteLog(final ListItemResultData result, final Path outputPath,
                                     final String outputType, final MetaContent metaContent,
                                     int argi)
      throws IOException {
    if (result != null) {
      LOG.info("{} items deleted from the list", result.getDeleted());
      if (result.getFailures() != null && !result.getFailures().isEmpty()) {
        LOG.info(ITEMS_IGNORED, result.getFailures().size());
      }
      if (outputPath != null) {
        addLogItemToOutput(result, outputPath, outputType, metaContent);
        argi += 2;
        String log = outputPath.toString();
        LOG.info(DUMP_FILE_WRITTEN, log);
      }
    }
    return argi;
  }

  private static void manageItemLog(boolean upsert, final Path outputPath, final String outputType,
                                    final ListItemResultData result, final ListImpl listImpl)
      throws IOException {
    if (!upsert && result.getIgnored() > 0) {
      LOG.info(ITEMS_IGNORED, result.getIgnored());
    }
    if (upsert) {
      updateFailureItemResult(result, listImpl, outputPath, outputType);
    } else {
      if (outputPath != null) {
        addLogItemToOutput(result, outputPath, outputType, listImpl.getContent());
      }
    }
  }

  public static void logModules() throws UnknownAuthenticationException {
    Model model = getModel(workspaceId, modelId);
    if (model == null) {
      return;
    }
    workspaceId = model.getCurrentWorkspaceId();
    modelId = model.getId();

    if (workspaceId != null && modelId != null) {
      Iterable<ModuleData> moduleIterator = getService()
          .getModules(workspaceId, modelId);
      if ((((Paginator<ModuleData>) moduleIterator).getPageInfo().getTotalSize()).equals(0)) {
        LOG.info("Model - {} has no modules.", modelId);
      } else {
        String log = Utils.formatTSV("Module_ID", "Module_Name");
        LOG.info(log);
        StreamSupport.stream(moduleIterator.spliterator(), false)
            .forEach(module -> {
              String logInfo = Utils.formatTSV(module.getId(), module.getName());
              LOG.info(logInfo);
            });
      }
    }
  }

  private static void logModuleViews(ModelData model) throws UnknownAuthenticationException {
    String currentWorkspaceId = model.getCurrentWorkspaceId();
    String currentModelId = model.getId();
    Iterable<ModuleData> moduleIterator = getService()
        .getModules(currentWorkspaceId, currentModelId);
    Stream<ModuleData> moduleDataStream = StreamSupport
        .stream(moduleIterator.spliterator(), false);
    if (moduleId != null) {
      moduleDataStream = moduleDataStream.filter(module -> moduleId.equals(module.getId())
          || moduleId.equals(module.getName())
          || moduleId.equals(module.getCode()));
    }
    String log = Utils.formatTSV("Module_ID", "Module_Name", "View_ID", "View_Name");
    LOG.info(log);
    moduleDataStream
        .forEach(module -> {
          Iterable<ViewData> viewIterator = null;
          try {
            viewIterator = getService()
                .getViews(currentModelId, module.getId());
          } catch (UnknownAuthenticationException e) {
            return;
          }
          StreamSupport.stream(viewIterator.spliterator(), false)
              .forEach(view -> LOG.info(Utils.formatTSV(
                  module.getId(), module.getName(), view.getId(), view.getName())));
        });
  }

  private static void closeDown() {
    if (service != null && Task.getRunningTask() == null) {
      service.close();
      service = null;
    }
  }

  /**
   * Validates the chunk-size value and then converts it to bytes and stores it.
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
    int maxRetryCount;
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
    int httpTimeout;
    try {
      httpTimeout = Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Provided Http-timeout is not a number!");
    }
    if (httpTimeout > Constants.MAX_HTTP_CONNECTION_TIMEOUT_SECS
        || httpTimeout < Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS) {
      throw new IllegalArgumentException("Http-Timeout can only be within the range ["
          + Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS + ", "
          + Constants.MAX_HTTP_CONNECTION_TIMEOUT_SECS + "]");
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
    if (retryTimeout > Constants.MAX_RETRY_TIMEOUT_SECS
        || retryTimeout < Constants.MIN_RETRY_TIMEOUT_SECS) {
      throw new IllegalArgumentException("Retry timeout can only be within the range ["
          + Constants.MIN_RETRY_TIMEOUT_SECS + ", " + Constants.MAX_RETRY_TIMEOUT_SECS + "]");
    }
    return retryTimeout;
  }

  /**
   * Retrieve any dump file(s) from the server following an import or process containing 1+ imports. If the result is
   * for a process, then outputLocation will be a directory.
   *
   * @param taskResult     the result of running the task
   * @param outputLocation the location in which to store the data
   * @since 1.3
   */
  protected static void retrieveOutput(TaskResult taskResult,
                                       File outputLocation)
      throws AnaplanAPIException, IOException {
    if (taskResult == null) {
      LOG.info("No dump file is available.");
      if (outputLocation.exists() && !outputLocation.isDirectory()) {
        Files.delete(outputLocation.toPath());
      }
      return;
    }
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
      parseNestedFile(taskResult, outputLocation);
    } else if (taskResult.isFailureDumpAvailable()) {
      ServerFile failureDump = taskResult.getFailureDump();
      failureDump.downLoad(outputLocation, true);
      LOG.info(DUMP_FILE_WRITTEN, outputLocation);
    }
  }

  private static void parseNestedFile(TaskResult taskResult, File outputLocation) throws IOException {
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
        LOG.info(DUMP_FILE_WRITTEN, nestedFile);
      }
      ++index;
    }
  }

  /**
   * Locate or optionally create a server file on the server. An error message will be produced if the workspace, model
   * or (if create is false) server file cannot be located.
   *
   * @param workspaceId the name or ID of the workspace
   * @param modelId     the name or ID of the model
   * @param fileId      the name or ID of the server file
   * @param create      if true and the file does not exist on the server, create a new file
   * @return the server file, or null if the workspace, model or (if create is false) server file could not be located.
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
        LOG.error("File \"{}\" not found in workspace {}, model {}", fileId, workspaceId, modelId);
      }
    }
    // Set proper encoding based on what server sends back
    if (serverFile != null && serverFile.getData() != null
        && serverFile.getData().getEncoding() != null) {
      System.setProperty("file.encoding", serverFile.getData().getEncoding());
    }
    return serverFile;
  }

  /**
   * Locate a import definition on the server. An error message will be produced if the workspace, model or import
   * definition cannot be located.
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
      LOG.error("Import \"{}\" not found in workspace {}, model {}", importId, workspaceId, modelId);
    }
    return serverImport;
  }

  /**
   * Locate a saved export on the server. An error message will be produced if the workspace, model or saved export
   * cannot be located.
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
      LOG.error("Export \"{}\" not found in workspace {}, model {}", exportId, workspaceId, modelId);
    }
    return serverExport;
  }

  /**
   * Locate a saved action on the server. An error message will be produced if the workspace, model or saved action
   * cannot be located.
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
      LOG.error("Action \"{}\" not found in workspace {}, model {}", actionId, workspaceId, modelId);
    }
    return serverAction;
  }

  /**
   * Locate a process definition on the server. An error message will be produced if the workspace, model or process
   * definition cannot be located.
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
      LOG.error("Process \"{}\" not found in workspace {}, model {}",
          processId, workspaceId, modelId);
    }
    return serverProcess;
  }

  /**
   * Locate a saved view on the server. An error message will be produced if the workspace, model, module or saved view
   * cannot be located.
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
      LOG.error("View \"{}\" not found in workspace \"{}\", model \"{}\", module \"{}\"",
          viewId, workspaceId, modelId, moduleId);
    }
    return view;
  }

  /**
   * Locate a module on the server. An error message will be produced if the workspace, model or module cannot be
   * located.
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
      LOG.error("Module \"{}\" not found in workspace \"{}\", model \"{}\"", moduleId, workspaceId, modelId);
    }
    return module;
  }

  /**
   * Locate a model on the server. An error message will be produced if the workspace or model cannot be located.
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
    Model model = null;
    for (Model m : workspace.getModels()) {
      if (modelId.equals(m.getId()) || modelId.equalsIgnoreCase(m.getName())) {
        model = m;
        break;
      }
    }
    if (model == null) {
      ModelData data = new ModelData(modelId, "");
      model = new Model(workspace, data);
      model.setCurrentWorkspaceId(workspaceId);
    }
    return model;
  }

  /**
   * Locate a workspace on the server. An error message will be produced if the workspace cannot be located.
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
    Workspace result;
    try {
      result = getService().getWorkspace(workspaceId);
    } catch(WorkspaceNotFoundException | UnknownAuthenticationException wnfe) {
      WorkspaceData data = new WorkspaceData();
      data.setId(workspaceId);
      result = new Workspace(service, data);
    }
    return result;
  }

  /**
   * Returns the client service instance. If this is the first invocation of this method, then a new instance will be
   * created, using the current service credentials, location, proxy and debug settings.
   *
   * @return the service instance
   * @since 1.3
   */
  protected static Service getService() throws AnaplanAPIException, UnknownAuthenticationException {
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
      service = DefaultServiceProvider.getService(props);
      service.authenticate();
    }
    return service;
  }

  /**
   * Gathers the Anaplan service credentials.
   *
   * @return the credentials for the Anaplan service, either obtained from getUsername() and getPassPhrase() or from
   * getCertificate(), depending on the authentication method being used
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
   * Gathers the proxy credentials if set. If the user name is of the form "domain\u005cuser" or
   * "domain\u005cworkstation\u005cuser" it is interpreted accordingly.
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
   * Returns the username set using setUsername(). If null, empty or '?' and a system console is available, then the
   * user will be prompted for a value.
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
   * Set the username of the Anaplan account for the service to use.
   *
   * @param username the email address of the Anaplan user
   * @since 1.3
   */
  protected static void setUsername(String username) {
    Program.username = username;
  }

  /**
   * Returns the password set using setPassword(). If null, empty or '?' and a system console is available, then the
   * user will be securely prompted for a value.
   *
   * @return the password
   * @since 1.3
   */
  protected static String getPassphrase() {
    if (passphrase == null || passphrase.isEmpty() || Objects.equals(passphrase, "?")) {
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
   * Set the passphrase of the Anaplan account for the service to use.
   *
   * @param passphrase the passphrase of the Anaplan user
   * @since 1.3
   */
  protected static void setPassphrase(String passphrase) {
    Program.passphrase = passphrase;
  }

  /**
   * Fetches the Anaplan Auth-service URL. If null, empty or '?' and a system console is available, then the user will
   * be prompted for a value.
   *
   * @return Anaplan Auth-Service URL
   */
  protected static URI getAuthServiceUri() {
    if (authServiceUrl == null || authServiceUrl.toString().isEmpty() || authServiceUrl.toString()
        .equals("?")) {
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
   * Set the proxy location. If null, no proxy will be used, and a direct connection to the internet is assumed. If not
   * set, proxy settings will be automatically detected, based on the platform.
   *
   * @param proxyLocation the URI of the proxy, or null if no proxy is to be used
   * @since 1.3
   */
  protected static void setProxyLocation(URI proxyLocation) {
    Program.proxyLocation = proxyLocation;
    Program.proxyLocationSet = true;
  }

  /**
   * Returns the proxy username set using setProxyUsername(). If it was set to '?' and a system console is available,
   * then the user will be prompted for a value.
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
   * Returns the proxy password set using setProxyPassword(). If it was set to '?' and a system console is available,
   * then the user will be securely prompted for a value.
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
   * Set the passphrase for an authenticating proxy
   *
   * @param passphrase the passphrase for the authenticating proxy
   * @since 1.3.1
   */
  protected static void setProxyPassphrase(String passphrase) {
    Program.proxyPassphrase = passphrase;
  }

  /**
   * Set the debug level. If this is increased then more verbose output will be produced. The default setting is zero.
   *
   * @param debugLevel an integer debug level, nominally in the range 0..2
   * @since 1.3
   */
  protected static void setDebugLevel(int debugLevel) {
    Program.debugLevel = debugLevel;
  }

  /**
   * Loads the X509 certificate to be used for authentication from file, or from Keystore
   *
   * @return the certificate
   * @since 1.3.2
   */
  protected static X509Certificate getCertificate()
      throws CertificateException, KeyStoreException, IOException {
    String certificatePath = getCertificatePath();
    String keyStorePath = getKeyStorePath();
    if (certificatePath != null) {
      File certificateFile = new File(certificatePath);
      if (certificateFile.isFile()) {
        // load certificate from file
        return loadCertificateFromFile(certificateFile);
      } else {
        throw new IllegalArgumentException(
            "The specified certificate path '" + certificatePath + "' is invalid");
      }
    } else if (keyStorePath != null) {
      return new KeyStoreManager()
          .getKeyStoreCertificate(keyStorePath, getKeyStorePassword(), getKeyStoreAlias());
    } else {
      // should not happen
      throw new IllegalArgumentException("Could not load a certificate for authentication");
    }
  }

  /**
   * Private key are always stored in keystore files, so fetches that using password and alias.
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
        throw new IllegalArgumentException(
            "The specified privateKey path '" + privateKeyPath + "' is invalid");
      }
    } else if (keyStorePath != null && keyStorePrivateKeyAlias != null) {
      return new KeyStoreManager()
          .getKeyStorePrivateKey(keyStorePath, getKeyStorePassword(), keyStorePrivateKeyAlias);
    } else {
      throw new IllegalArgumentException(
          "Could not load the privateKey for authentication. Please check the privateKey parameters in your input.");
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
   * Set the alias of the key store entry referring to the public certificate and private key pair used by the client to
   * authenticate with the server
   *
   * @since 1.3.2
   */
  protected static void setKeyStoreAlias(String keyStoreAlias) {
    Program.keyStoreAlias = keyStoreAlias;
  }

  /**
   * Returns the key store password set using setKeyStorePassword(). If not provided, and the password file is not
   * available, then the user will be securely prompted for a value (provided a system console is available)
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
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
          throw new IllegalArgumentException("Password file could not be read");
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
   * @since 1.3.2
   */
  protected static void setKeyStorePassword(String keyStorePassword) {
    Program.keyStorePassword = keyStorePassword;
  }

  /**
   * Check and if necessary prompt for a value. If propertyValue is null, empty or '?', and the program is associated
   * with a terminal (ie
   * <tt>System.console() != null</tt>), then prompt the user using the value
   * in propertyName, followed by a colon. If password is true, then the value input by the user will not be echoed.
   *
   * @param propertyName  the value to be displayed to the user if prompted
   * @param propertyValue the value supplied
   * @param password      flag to suppress echoing
   * @return the value passed in propertyValue, or the value entered by the user if it was necessary to prompt
   * @throws UnsupportedOperationException if there is no terminal associated with the program and a value was not
   *                                       supplied.
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
   * @return the file contents
   * @throws FileNotFoundException if the file could not be found
   */
  private static String readFileContents(File file) throws FileNotFoundException {
    StringBuilder fileContents = new StringBuilder((int) file.length());

    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNext()) {
        fileContents.append(scanner.next());
      }
      return fileContents.toString();
    }
  }

  /**
   * Loads a {@link X509Certificate} from a file
   *
   * @return a X509Certificate
   */
  private static X509Certificate loadCertificateFromFile(File certificateFile) throws
      CertificateException, FileNotFoundException {
    // loading certificate chain
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    InputStream certificateStream = new FileInputStream(certificateFile);

    Collection<? extends Certificate> c = certificateFactory
        .generateCertificates(certificateStream);
    Certificate[] certs = new Certificate[c.toArray().length];
    if (c.size() == 1) {
      return (X509Certificate) c.iterator().next();
    } else {
      throw new IllegalArgumentException(
          "Certificate file must contain only one certificate (chain length was " + certs.length
              + ")");
    }
  }

  /**
   * Loads a {privateKey} from a file
   *
   * @return a RSAPrivateKey
   */

  private static RSAPrivateKey loadPrivateKeyFromFile(String privateKeyPath, String passphrase) {
    try(FileReader fileReader = new FileReader(privateKeyPath)) {
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
      PEMParser pemParser = new PEMParser(fileReader);
      PKCS8EncryptedPrivateKeyInfo encryptedKeyPair = (PKCS8EncryptedPrivateKeyInfo) pemParser
          .readObject();
      InputDecryptorProvider pkcs8Prov = new JceOpenSSLPKCS8DecryptorProviderBuilder()
          .setProvider("BC")
          .build(passphrase.toCharArray());
      PrivateKeyInfo privateKeyInfo = encryptedKeyPair.decryptPrivateKeyInfo(pkcs8Prov);
      JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();
      return (RSAPrivateKey) jcaPEMKeyConverter.setProvider("BC").getPrivateKey(privateKeyInfo);
    } catch (final NullPointerException e) {
      throw new IllegalArgumentException(String
          .format("Either private key path <%s> or passphrase is null. Please check the value(s).",
              privateKeyPath));
    } catch (final FileNotFoundException e) {
      throw new IllegalArgumentException(String
          .format("Private key file doesn't exist on path: %s. Please check the path.",
              privateKeyPath));
    } catch (final InvalidKeySpecException e) {
      if (e.getCause().getMessage().contains("DER input, Integer tag error")) {
        throw new IllegalArgumentException(
            "Empty passphrase provided for private key. Please check the passphrase.");
      } else {
        throw new IllegalArgumentException(
            "Invalid private key / passphrase combination. Please check.");
      }
    } catch (final PKCSException e) {
      if (null != e.getCause().getCause()
          && e.getCause().getCause().getMessage().contains("pad block corrupted")) {
        throw new IllegalArgumentException(
            "Incorrect passphrase provided for private key. Please check the passphrase.");
      } else {
        throw new IllegalArgumentException(
            "Invalid private key / passphrase combination. Please check.");
      }
    } catch (final Exception e) {
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
    try (FileInputStream fileInputStream = new FileInputStream(jdbcPropertiesPath)) {
      jdbcProps.load(fileInputStream);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading JDBC Properties file", e);
    }

    return getJDBCConfig(jdbcProps);
  }

  private static JDBCConfig getJDBCConfig(final Properties jdbcProps) {
    JDBCConfig jdbcConfig = new JDBCConfig();
    jdbcConfig.setJdbcConnectionUrl(jdbcProps.getProperty("jdbc.connect.url"));
    jdbcConfig.setJdbcUsername(jdbcProps.getProperty("jdbc.username"));
    jdbcConfig.setJdbcPassword(jdbcProps.getProperty("jdbc.password") == null ? new char[0]
        : jdbcProps.getProperty("jdbc.password").toCharArray());
    if (fileId != null) {
      try {
        jdbcConfig.setJdbcFetchSize(Integer.parseInt(jdbcProps.getProperty("jdbc.fetch.size")));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid JDBC Fetch-size provided in properties.");
      }
    }
    jdbcConfig.setStoredProcedure(
        Boolean.valueOf(jdbcProps.getProperty("jdbc.isStoredProcedure", "false")));
    jdbcConfig.setJdbcQuery(jdbcProps.getProperty("jdbc.query"));
    String paramsCsv = jdbcProps.getProperty("jdbc.params");
    assert paramsCsv != null : "Parameters required!";
    try {
      jdbcConfig.setJdbcParams(new CSVParser().parseLine(paramsCsv));
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid params, unable to parse.", e);
    }

    return jdbcConfig;
  }

  private static Map<String, String> getHeaderMap(final Properties properties) {
    final Map<String, String> headerMap = new HashMap<>();
    for (final Object key : properties.keySet()) {
      headerMap.put(key.toString(), properties.getProperty(key.toString()));
    }
    return headerMap;
  }

  private static void displayHelp() {
    Path userDirectory;
    try {
      userDirectory = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    } catch (InvalidPathException e) {
      throw new BadSystemPropertyError(e);
    }
    File passwordFile = new File(userDirectory.toString(), Constants.PW_FILE_PATH_SEGMENT);

    String error = "Options are:\n"
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
        +
        ": Path to user certificate used for authentication (an alternative to using a key store)\n"
        + "(-pkey|-privatekey) <privatekey path>:<passphrase>"
        +
        ": Path to user privatekey used for authentication (an alternative to using a key store) + passphrase\n"
        + "(-k|-keystore) <keystore path>"
        + ": Path to local key store containing user certificate(s) for authentication\n"
        + "(-kp|-keystorepass) <keystore password>"
        + ": Password for the key store (if not provided, password is read from obfuscated file '"
        + passwordFile.getAbsolutePath()
        + "', or prompted for)\n"
        + "(-ka|-keystorealias) <keystore alias>"
        + ": Alias of the public certificate in the specified key store\n"
        + "(-v|-via) <proxy URI>: use specified proxy\n"
        +
        "(-vu|-viauser) [<domain>[\\<workstation>]\\]<username>[:<password>]: use proxy credentials\n"
        + "(-mrc|-maxretrycount): Max retry count for API calls\n"
        + "(-rt|-retrytimeout): Retry timeout for Http client calls\n"
        + "(-ct|-httptimeout): Http client connection timeout\n"
        + "\n"
        + "Workspace Contents:\n"
        + "-------------------\n"
        + "(-W|-workspaces): list available workspaces\n"
        + "(-M|-models): list available models\n"
        + "(-w|-workspace) (<id>|<name>): select a workspace by id/name\n"
        + "(-m|-model) (<id>|<name>): select a model by id/name\n"
        + "(-V|-views): list available views\n"
        + "(-vi|-view) (<id>|<name>): select a view by id/name\n"
        + "(-MO|-modules): list available modules\n"
        + "(-mo|-module): (<id>|<name>): select a module by id/name\n"
        + "(-L|-lists): list available lists in selected model\n"
        + "(-l|-list): (<id>|<name>): select a list by id/name\n"
        + "(-F|-files): list available server files in selected model\n"
        + "(-f|-file) (<id>|<name>): select a server file by id/name\n"
        + "(-ch|-chunksize): upload chunk-size number, defaults to 1048576.\n"
        +
        "(-pages): Comma separated list of <page dimension id>:<dimension member id> The page selector values that identify the page to retrieve\n"
        + "\n"
        + "Data Transfer:\n"
        + "--------------\n"
        + "(-g|-get) <local path>: Download specified server file to local file\n"
        +
        "(-get:csv|-get:csv_sc|-get:csv_mc|-get:json) [local path]: Export to local file of type csv or json\n"
        + "-gets Write specified server file to standard output\n"
        + "-getc Write tab-separated server file to standard output\n"
        + "(-p|-put) <local path>: Upload to specified server file from local file\n"
        + "-puts Upload to specified server file from standard input\n"
        + "-putc Upload to specified server file from tab-separated standard input\n"
        + "-file Export data to a local file\n"
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
        + "(-xl|-locale) <locale> Specify locale (eg en_US) to perform server operation\n"
        + "(-xc|-connectorproperty) [(<source>|<type>)/]property:(value|?):\n"
        + "    specify import data source connection property\n"
        + "(-xm|-mappingproperty) [(<import id>|<import name>)/]dimension:(value|?):\n"
        + "    specify prompt-at-runtime import mapping value"
        +
        "(-x[:all]|-execute[:all]): Run the selected import/export/action/process/listItems/views  - [:all] option for list items include all details\n"
        + "\n"
        + "Action Information:\n"
        + "-------------------\n"
        + "(-o|-output) <local path>: Retrieve dump file(s) for completed import/process\n"
        + "-emd: Describe layout of an export (metadata)\n"
        + "\n"
        + "JDBC:\n"
        + "-----\n"
        + "-loadclass <class name>: This parameter is not required since AC 1.4.4.\n"
        + "    It will be removed in next version of AC.\n"
        + "-jdbcproperties: Path to JDBC properties file.\n"
        + "\n"
        + "Actionless Imports and Exports:\n"
        + "-------------------------------\n"
        +
        "-putItems:(csv|json|jdbc) <local path>: Add items in file in <local path> to list defined by parameter \"-list\"\n"
        +
        "-updateItems:(csv|json|jdbc) <local path>: Update items in file in <local path> in list defined by parameter \"-list\"\n"
        +
        "-upsertItems:(csv|json|jdbc) <local path>: Upsert (add + update) items in file in <local path> in list defined by parameter \"-list\"\n"
        +
        "-deleteItems:(csv|json|jdbc) <local path>: Delete items in file in <local path> from list defined by parameter \"-list\"\n"
        +
        "(-im|-itemmappingproperty) <local path>: Path to file mapping file for putItems/updateItems/upsertItems/deleteItems actions\n"
        +
        "-output:(csv|json) <local path>: Write potential errors to file in <local path> in given format";
    LOG.error(error);
  }

  private static void displayVersion() {
    String log = Strings.repeat("=", 70);
    LOG.debug(log);
    LOG.debug("Anaplan Connect {}.{}.{}", Constants.AC_MAJOR, Constants.AC_MINOR,
        Constants.AC_REVISION);
    LOG.debug("{} ({})/ ({})/ {}", System.getProperty("java.vm.name"),
        System.getProperty("java.vendor"),
        System.getProperty("java.vm.version"), System.getProperty("java.version"));
    LOG.debug("({}{})/{}", System.getProperty("os.name"), System.getProperty("os.arch"),
        System.getProperty("os.version"));
    log = Strings.repeat("=", 70);
    LOG.debug(log);
  }

  private static void updateFailureItemResult(final ListItemResultData result,
                                              final ListImpl listImpl,
                                              final Path outputPath, final String outputType)
      throws IOException {
    final List<ListFailure> failures = result.getFailures();
    if ((result.getFailures() == null || failures.isEmpty()) && outputPath != null) {
      addLogItemToOutput(result, outputPath, outputType, listImpl.getContent());
    }

    final List<ListItem> listItem = new ArrayList<>();
    final Iterator<ListFailure> failuresIterator = failures.iterator();
    int extraFailure = 0;
    while (failuresIterator.hasNext()) {
      final ListFailure failure = failuresIterator.next();
      if ("DUPLICATE".equalsIgnoreCase(failure.getFailureType())) {
        listItem.add(failure.getListItem());
        failuresIterator.remove();
        result.setIgnored(result.getIgnored() - 1);
      } else {
        extraFailure++;
      }
    }
    addItems(listItem, listImpl, result, outputPath, outputType, extraFailure);
  }

  private static void addItems(final List<ListItem> listItem, final ListImpl listImpl,
      final ListItemResultData result, final Path outputPath, final String outputType, int extraFailure)
      throws IOException {
    final ListItemParametersData listItemParametersDataUpdate = new ListItemParametersData();
    listItemParametersDataUpdate.setItems(new ArrayList<>(0));
    if (!listItem.isEmpty()) {
      listItemParametersDataUpdate.setItems(listItem);
      final ListItemResultData updateResult =
          listImpl.updateItemsList(listItemParametersDataUpdate);
      if (updateResult.getFailures() != null) {
        for (final ListFailure upResult : updateResult.getFailures()) {
          upResult.setListItem(listItem.get(upResult.getRequestIndex()));
          result.getFailures().add(upResult);
        }
      }
      if (outputPath != null) {
        addLogItemToOutput(result, outputPath, outputType, listImpl.getContent());
      }
      String log = String.format("%d items updated", updateResult.getTotal() - updateResult.getIgnored());
      LOG.info(log);
      if (updateResult.getIgnored() > 0 || extraFailure > 0) {
        LOG.info(ITEMS_IGNORED, (updateResult.getIgnored() + extraFailure));
      }
    } else {
      if (outputPath != null && result.getFailures() != null) {
        addLogItemToOutput(result, outputPath, outputType, listImpl.getContent());
        LOG.info(ITEMS_IGNORED, result.getIgnored());
      }
    }
  }

  private static Path getOutput(final String[] args, final int index) {
    if (args.length > (index + 1) && args[index + 1].startsWith("-output")) {
      return Paths.get(args[index + 2]);
    }
    return null;
  }

  private static void addLogItemToOutput(final ListItemResultData result, final Path outputPath,
                                         final String type, final MetaContent metaContent)
      throws IOException {
    if ("CSV".equalsIgnoreCase(type)) {
      addLogItemToOutputCSV(result, outputPath, metaContent);
    } else if ("JSON".equalsIgnoreCase(type)) {
      addLogItemToOutputJSON(result, outputPath);
    }
  }

  private static void addLogItemToOutputCSV(final ListItemResultData result, final Path outputPath,
                                            final MetaContent metaContent)
      throws IOException {

    final File output = getOutputFile(outputPath);
    final String[] header = getLogHeaderCSV(result, metaContent);
    if (output == null) {
      return;
    } else if (result.getFailures() == null || result.getFailures().isEmpty()) {
      try (final CSVWriter csvWriter = new CSVWriter(new FileWriter(output))) {
        csvWriter.writeNext(header);
      }
      return;
    }
    try (final CSVWriter csvWriter = new CSVWriter(new FileWriter(output))) {
      csvWriter.writeNext(header);
      for (final ListFailure listFailure : result.getFailures()) {
        final ListItem listItem = listFailure.getListItem();
        final List<String> itemsCSV = new ArrayList<>();
        itemsCSV.add(listItem.getName());
        itemsCSV.add(listItem.getCode());
        if (listItem.getProperties() != null) {
          for (final String key : listItem.getProperties().keySet()) {
            itemsCSV.add(listItem.getProperties().get(key));
          }
        }
        if (listItem.getSubsets() != null) {
          for (final String key : listItem.getSubsets().keySet()) {
            itemsCSV.add(listItem.getSubsets().get(key).toString());
          }
        }
        itemsCSV.add(listFailure.getFailureType());
        itemsCSV.add(listFailure.getFailureMessageDetails());
        csvWriter.writeNext(itemsCSV.toArray(new String[0]));
      }
      csvWriter.flush();
    }
  }

  private static ObjectNode createItemLogJSON(final ObjectMapper jsonObject,
                                              final ListItem listItem, final String failureType,
                                              final String failureMessage) {
    final ObjectNode node = jsonObject.createObjectNode();
    node.put(CSV_LOG_HEADER[0], listItem.getName());
    node.put(CSV_LOG_HEADER[1], listItem.getCode());
    if (listItem.getProperties() != null) {
      for (final String key : listItem.getProperties().keySet()) {
        node.put(key, listItem.getProperties().get(key));
      }
    }
    if (listItem.getSubsets() != null) {
      for (final String key : listItem.getSubsets().keySet()) {
        node.put(key, listItem.getSubsets().get(key));
      }
    }
    node.put(CSV_LOG_HEADER[2], failureType);
    node.put(CSV_LOG_HEADER[3], failureMessage);
    return node;
  }

  private static String[] getLogHeaderCSV(final ListItemResultData result,
                                          final MetaContent metaContent) {
    final List<String> header = new ArrayList<>();
    header.add(CSV_LOG_HEADER[0]);
    header.add(CSV_LOG_HEADER[1]);
    if (result.getFailures() != null && !result.getFailures().isEmpty()) {
      final Map<String, String> properties = result
          .getFailures().get(0).getListItem().getProperties();
      if (properties != null && properties.size() > 0) {
        header.addAll(properties.keySet());
      }
      final Map<String, Boolean> subsets = result
          .getFailures().get(0).getListItem().getSubsets();
      if (subsets != null && subsets.size() > 0) {
        header.addAll(subsets.keySet());
      }
    } else {
      if (metaContent.getPropNames() != null) {
        header.addAll(metaContent.getPropNames());
      }
      if (metaContent.getSubsets() != null) {
        header.addAll(metaContent.getSubsets());
      }
    }
    header.add(CSV_LOG_HEADER[2]);
    header.add(CSV_LOG_HEADER[3]);
    return header.toArray(new String[0]);

  }

  private static void addLogItemToOutputJSON(final ListItemResultData result, final Path outputPath)
      throws IOException {
    final File output = getOutputFile(outputPath);
    final ObjectMapper jsonObject = new ObjectMapper();
    final ArrayNode arrayNode = jsonObject.createArrayNode();

    if (output == null) {
      return;
    } else if (result.getFailures() == null || result.getFailures().isEmpty()) {
      Files.write(output.toPath(),
          jsonObject.writerWithDefaultPrettyPrinter().writeValueAsBytes(arrayNode));
      return;
    }
    for (final ListFailure listFailure : result.getFailures()) {
      final ListItem listItem = listFailure.getListItem();
      final ObjectNode node = createItemLogJSON(jsonObject, listItem, listFailure.getFailureType(),
          listFailure.getFailureMessageDetails());
      arrayNode.add(node);
    }
    Files.write(output.toPath(),
        jsonObject.writerWithDefaultPrettyPrinter().writeValueAsBytes(arrayNode));
  }

  private static File getOutputFile(final Path outputPath) throws IOException {
    if (outputPath == null) {
      return null;
    }
    final File output = outputPath.toFile();

    if (!output.exists() && !output.createNewFile()) {
      throw new IllegalArgumentException(
          "Log path " + output.getPath() + " not exists or cannot be created \"");
    }
    return output;
  }
}
