// Copyright 2012 Anaplan Limited
package com.anaplan.client.integrationTests;
/**
 * This is a test that runs via a yaml file with dataprovider Basically runs unix commands and shell scripts to mimic
 * Jmeter suite for anaplan connect.  IP.
 */

import static com.anaplan.client.integrationTests.helpers.constants.IntegrationConstants.DUMP_FOLDER;
import static com.anaplan.client.integrationTests.helpers.constants.IntegrationConstants.EXPORT_FOLDER;
import static com.anaplan.client.integrationTests.helpers.constants.IntegrationConstants.SCRIPTS_FOLDER;

import com.anaplan.client.integrationTests.helpers.dto.AnaplanTest;
import com.anaplan.client.integrationTests.helpers.dto.Root;
import com.anaplan.client.integrationTests.helpers.dto.Script;
import com.anaplan.client.integrationTests.helpers.validator.FileValidator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.codehaus.plexus.util.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BaseIntegrationTest {

  private static final Logger LOG = LoggerFactory.getLogger(BaseIntegrationTest.class);

  private AnaplanTest anaplanTest;

  public BaseIntegrationTest(AnaplanTest anaplanTest) {
    this.anaplanTest = anaplanTest;
  }

  @BeforeClass
  public static void setup() {
    try {
      LOG.info("Running setup tasks...");
      createFolder(EXPORT_FOLDER);
      createFolder(DUMP_FOLDER);
      copyAnaplanClientSh();
      copyAnaplanConnectJAR();
    } catch (IOException e) {
      LOG.error("Error while setup.", e);
    }
  }

  private static void createFolder(String folderPath) {
    try {
      File folder = getRelativeFileOrDirectory(folderPath);
      if (folder == null) {
        Files.createDirectories(Paths.get(getRelativeFileOrDirectory(SCRIPTS_FOLDER).getAbsolutePath() + folderPath));
      }
    } catch (IOException e) {
      LOG.error("Error while setup.", e);
    }
  }

  @AfterClass
  public static void cleanUp() {
    LOG.info("Running clean-up tasks...");
    try {
      deleteFolder(EXPORT_FOLDER);
      deleteFolder(DUMP_FOLDER);
    } catch (IOException e) {
      LOG.error("Error while cleanUp.", e);
    }
  }

  private static void deleteFolder(String folderPath) throws IOException {
    File folder = getRelativeFileOrDirectory(SCRIPTS_FOLDER + folderPath);
    if (folder != null) {
      FileUtils.deleteDirectory(folder);
    }
  }

  /**
   * Copy Anaplan-Connect JAR to scripts folder.
   *
   * @throws IOException
   */
  public static void copyAnaplanConnectJAR() throws IOException {
    String userDir = System.getProperty("user.dir");
    File targetDirectory = new File(userDir + "/target");
    FilenameFilter filenameFilter = new WildcardFileFilter("anaplan-connect-*-jar-with-dependencies.jar");
    File[] jarFiles = targetDirectory.listFiles(filenameFilter);
    if (jarFiles != null && jarFiles.length > 0) {
      File destDir = getRelativeFileOrDirectory(SCRIPTS_FOLDER);
      FileUtils.copyFileToDirectory(jarFiles[0], destDir);
    } else {
      Assert.fail("anaplan-connect JAR not found.");
    }
  }

  /**
   * Copy AnaplanClient.sh to scripts folder.
   *
   * @throws IOException
   */
  public static void copyAnaplanClientSh() throws IOException {
    File srcDir = new File(System.getProperty("user.dir")); // Project root directory.
    FilenameFilter filenameFilter = new WildcardFileFilter("AnaplanClient.sh");
    File[] jarFiles = srcDir.listFiles(filenameFilter);
    if (jarFiles != null && jarFiles.length > 0) {
      File destDir = getRelativeFileOrDirectory(SCRIPTS_FOLDER);
      File file = jarFiles[0];
      try {
        FileUtils.forceDelete(new File(destDir + "/" + file.getName()));
      } catch (FileNotFoundException e) {
        // Do nothing.
      }
      FileUtils.copyFileToDirectory(file, destDir);

      Set<PosixFilePermission> perms = new HashSet<>();
      perms.add(PosixFilePermission.OWNER_READ);
      perms.add(PosixFilePermission.OWNER_EXECUTE);
      Files.setPosixFilePermissions(Paths.get(destDir.getAbsolutePath() + "/" + file.getName()), perms);
    } else {
      Assert.fail("AnaplanClient.sh not found");
    }
  }

  /**
   * @param inputStream The input stream to convert to list of lines.
   * @return List of lines read from the input stream.
   */
  private static List<String> readOutput(InputStream inputStream) {
    List<String> output = new ArrayList<>();
    new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(output::add);
    output.stream().forEach(System.out::println);
    return output;
  }

  /**
   * Get the a file from resources folder. Uses classloader.
   *
   * @param path The relative path.
   * @return File or directory at the relative path.
   */
  static File getRelativeFileOrDirectory(String path) {
    URL resource = BaseIntegrationTest.class.getClassLoader().getResource(path);
    if (resource != null && resource.getFile() != null) {
      return new File(resource.getFile());
    }
    return null;
  }

  /**
   * Parses a list of YAML config files and returns list of AnaplanTest object.
   *
   * @param yamlFileNames The list of YAML files to parse.
   * @return The list of AnaplanTests from all the parsed YAML files.
   */
  static List<AnaplanTest> getTestsFromYAMLFiles(List<String> yamlFileNames) {
    // Initialize object-mapper
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    mapper.findAndRegisterModules();

    // Parse all yaml files and collect tests.
    List<AnaplanTest> anaplanTests = new ArrayList<>();
    for (String fileName : yamlFileNames) {
      File yamlFile = getRelativeFileOrDirectory("integration-tests/runner/" + fileName);
      Assert.assertTrue(fileName + " does not exist.", yamlFile.exists());
      LOG.debug("Parsing file - {}.", fileName);
      Root root = null;
      try {
        root = mapper.readValue(yamlFile, Root.class);
      } catch (IOException e) {
        LOG.error("Error reading the YAML file - " + fileName, e);
        Assert.fail("Error in YAML file - " + fileName);
      }
      anaplanTests.addAll(root.getTests());
    }
    return anaplanTests;
  }

  /**
   * @param script
   */
  private void checkDumpFile(Script script) {
    if (script.getFailInfo() != null) {
      String dumpFileName = script.getFailInfo().getDumpFile();
      if (StringUtils.isNotBlank(dumpFileName)) {
        File scriptFolder = getRelativeFileOrDirectory(SCRIPTS_FOLDER);
        File dumpFile = new File(scriptFolder.getAbsolutePath().concat(File.separator).concat(dumpFileName));
        if (!dumpFile.exists()) {
          LOG.debug("Dump file not found at - {}", dumpFile.getAbsolutePath());
          Assert.fail(new StringBuilder("Dump file not found.")
              .append("\n")
              .append("Test-Name - ")
              .append(this.anaplanTest.getTestName())
              .append("\n")
              .append("Script-Name - ")
              .append(script.getScriptName())
              .append("\n")
              .append("Dump File Path -  - ")
              .append(dumpFile.getAbsolutePath())
              .toString());
        } else {
          LOG.debug("\u2713 Dump file exists at {}", dumpFile.getAbsoluteFile());
        }
      }
    }
  }

  /**
   * Run a particular integration-test.
   *
   * @throws IOException
   * @throws InterruptedException
   */
  public void runIntegrationTest() throws IOException, InterruptedException {
    LOG.info("Running test - {}", anaplanTest.getTestName());
    for (Script script : anaplanTest.getScripts()) {
      List<String> scriptOutput = runShellScript(script);
      // Check if dump file was downloaded.
      checkDumpFile(script);
      // Check if desired messages are present in script output.
      checkScreenMessages(script, scriptOutput);
      // Check if exported file is as expected.
      checkExportedFile(script);
    }
    LOG.info("\u2713 Test successfully executed - {}", anaplanTest.getTestName());
  }

  private void checkExportedFile(Script script) {
    if (script.getExportFile() != null) {
      FileValidator fileValidator = new FileValidator(this.anaplanTest);
      String scriptsFolderPath = getRelativeFileOrDirectory(SCRIPTS_FOLDER).getAbsolutePath();
      // Check for expected file-size.
      fileValidator.checkFileSize(scriptsFolderPath, script);
      // Check for expected file-content.
      fileValidator.compareFiles(scriptsFolderPath, script);
    }
  }

  /**
   * Check for presence of desired messages in script output.
   *
   * @param script       - The script object being executed.
   * @param scriptOutput - Lines of script output.
   */
  private void checkScreenMessages(Script script, List<String> scriptOutput) {
    if (script.getReport() != null && CollectionUtils.isNotEmpty(script.getReport().getMessageList())) {
      List<String> messagesToCheck = script.getReport().getMessageList();
      messagesToCheck.stream().forEach(message -> {
        boolean isPresent = scriptOutput.stream().anyMatch(s -> s != null && s.contains(message));
        Assert.assertTrue(new StringBuilder("Script doesn't contain the desired message in output.")
            .append("\n")
            .append("Test-Name - ")
            .append(this.anaplanTest.getTestName())
            .append("\n")
            .append("Script-Name - ")
            .append(script.getScriptName())
            .append("\n")
            .append("Message - ")
            .append(message)
            .toString(), isPresent);
      });
      LOG.debug("\u2713 All expected messages are present in the script-output.");
    }
  }

  /**
   * Run a particular script-file from scripts folder and return output.
   *
   * @param script The script object.
   * @return List of output from the script.
   * @throws InterruptedException
   * @throws IOException
   */
  List<String> runShellScript(Script script) throws InterruptedException, IOException {
    String scriptName = script.getScriptName();
    LOG.info("Executing shell script - {}", scriptName);
    boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    ProcessBuilder builder = new ProcessBuilder();
    if (isWindows) {
      Assert.fail("Not setup for windows yet.");
    } else {
      // Take care of special character '&' in file-name.
      scriptName = scriptName.replaceAll(Matcher.quoteReplacement("&"), Matcher.quoteReplacement("\\&"));
      builder.command("sh", "-c", "./" + scriptName);
    }
    builder.directory(getRelativeFileOrDirectory(SCRIPTS_FOLDER));
    builder.redirectErrorStream(true);
    Process process = builder.start();
    int exitCode = process.waitFor();
    List<String> scriptOutput = readOutput(process.getInputStream());
    int expectedExitCode = script.getFailInfo() != null ? script.getFailInfo().getExitCode() : 0;
    Assert.assertEquals(
        new StringBuilder("Expected exit-code doesn't match.")
            .append("\n")
            .append("Test-Name - ")
            .append(this.anaplanTest.getTestName())
            .append("\n")
            .append("Script-Name - ")
            .append(scriptName)
            .toString(),
        expectedExitCode, exitCode);
    LOG.debug("\u2713 Exit code is as expected.");
    return scriptOutput;
  }

}
