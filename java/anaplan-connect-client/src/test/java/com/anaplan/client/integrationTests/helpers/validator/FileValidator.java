package com.anaplan.client.integrationTests.helpers.validator;

import com.anaplan.client.integrationTests.helpers.dto.AnaplanTest;
import com.anaplan.client.integrationTests.helpers.dto.ExportFile;
import com.anaplan.client.integrationTests.helpers.dto.Script;
import com.anaplan.client.integrationTests.helpers.ex.FileComparisonException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * checks file size for exports, and compares file contents.
 */
public class FileValidator {

  private AnaplanTest anaplanTest;

  public FileValidator(AnaplanTest anaplanTest) {
    this.anaplanTest = anaplanTest;
  }

  private final static Logger LOG = LoggerFactory.getLogger(FileValidator.class);

  public void checkFileSize(String folderPath, Script script) {
    if (script.getExportFile().getFileSize() != null) {
      File exportedFile = new File(folderPath + "/" + script.getExportFile().getExportedFile());
      if (!exportedFile.exists()) {
        LOG.error("File " + exportedFile.getName() + " does not exist");
        Assert.fail(new StringBuilder("Exported file does not exist.")
            .append("\n")
            .append("Test-Name - ")
            .append(this.anaplanTest.getTestName())
            .append("\n")
            .append("Script-Name - ")
            .append(script.getScriptName())
            .append("\n")
            .append("Exported File Path - ")
            .append(exportedFile.getAbsolutePath())
            .toString());
      }
      long expectedSize = script.getExportFile().getFileSize();
      long actualSize = exportedFile.length();
      LOG.debug("Checking file size. File - {}", exportedFile);
      if (expectedSize != actualSize) {
        Assert.assertEquals(new StringBuilder("File-size differs from expected size.")
                .append("\n")
                .append("Test-Name - ")
                .append(this.anaplanTest.getTestName())
                .append("\n")
                .append("Script-Name - ")
                .append(script.getScriptName())
                .append("\n")
                .append("Exported File Path - ")
                .append(exportedFile.getAbsolutePath())
                .toString(),
            expectedSize, actualSize);
      }
      LOG.debug("\u2713 File size of exported file is as expected.");
    }

  }

  public void compareFiles(String folderPath, Script script) {
    if (script.getExportFile().getExportedFile() != null) {
      Boolean areEqual = true;
      ExportFile exportFileInfo = script.getExportFile();
      String exportFile = new StringBuilder(folderPath).append("/").append(exportFileInfo.getExportedFile()).toString();
      String orgFile = new StringBuilder(folderPath).append("/").append(exportFileInfo.getOrgFile()).toString();
      LOG.debug("Comparing files - \nOriginal File - {}\nExported File - {}", orgFile, exportFile);

      try (BufferedReader exportedFile = new BufferedReader(new FileReader(exportFile));
          BufferedReader originalFile = new BufferedReader(new FileReader(orgFile));) {
        String exportedFileLine = exportedFile.readLine();
        String orgFileLine = originalFile.readLine();
        int lineNumber = 1;
        while (exportedFileLine != null || orgFileLine != null) {
          if (exportedFileLine == null || orgFileLine == null) {
            areEqual = false;
            break;
          } else if (!exportedFileLine.equalsIgnoreCase(orgFileLine)) {
            if (!(exportedFileLine.contains("CreationDate") ||
                orgFileLine.contains("dc:date") ||
                exportedFileLine.contains("MetadataDate") ||
                orgFileLine.contains("CreateDate") ||
                exportedFileLine.contains("ID [<"))) {
              areEqual = false;
              break;
            }
          }
          exportedFileLine = exportedFile.readLine();
          orgFileLine = originalFile.readLine();
          lineNumber++;
        }

        if (!areEqual) {
          Assert.fail(new StringBuilder("Files have different content.")
              .append("\n")
              .append("Test-Name - ")
              .append(this.anaplanTest.getTestName())
              .append("\n")
              .append("Script-Name - ")
              .append(script.getScriptName())
              .append("\n")
              .append("Difference at line-number - ")
              .append(lineNumber)
              .toString());
          LOG.error("Files have different content. They differ at line {}", lineNumber);
        } else {
          LOG.debug("\u2713 Files are identical.");
        }

      } catch (Exception e) {
        throw new FileComparisonException("Exception while comparing exported file content.", e);
      }
    }
  }
}
