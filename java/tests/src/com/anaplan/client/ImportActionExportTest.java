// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import java.io.File;
import java.io.IOException;

public class ImportActionExportTest extends BaseTest {
    public void testImportExport() throws AnaplanAPIException, IOException {
        Model model = getTestModel();
        ServerFile serverFile0 = model.getServerFile("File 0");
        File sourceFile0 = getTestDataFile("source.txt");
        serverFile0.upLoad(sourceFile0, true);

        Import import0 = model.getImport("List 0 from File 0");
        TaskResult taskResult = runTask(import0, null);
        assertNotNull(taskResult);
        assertTrue(taskResult.isSuccessful());
        assertTrue(taskResult.isFailureDumpAvailable());
        ServerFile dumpFile = taskResult.getFailureDump();
        File downloadedDumpFile = File.createTempFile("listdump", ".txt");
        dumpFile.downLoad(downloadedDumpFile, true);
        assertEquals(getTestDataFile("listdump.txt"), downloadedDumpFile);
        downloadedDumpFile.delete();

        Action action0 = model.getAction("Action 0");
        taskResult = runTask(action0, null);
        assertNotNull(taskResult);
        assertTrue(taskResult.isSuccessful());

        Action action1 = model.getAction("Action 1");
        taskResult = runTask(action1, null);
        assertNotNull(taskResult);
        assertTrue(taskResult.isSuccessful());

        Import import1 = model.getImport("Module 0 from File 0");
        TaskParameters taskParameters = new TaskParameters();
        taskParameters.addMappingParameter("Line Item", "Line Item 0");
        taskResult = runTask(import1, taskParameters);
        assertNotNull(taskResult);
        assertTrue(taskResult.isSuccessful());
        assertTrue(taskResult.isFailureDumpAvailable());
        dumpFile = taskResult.getFailureDump();
        downloadedDumpFile = File.createTempFile("moduledump", ".txt");
        dumpFile.downLoad(downloadedDumpFile, true);
        assertEquals(getTestDataFile("moduledump.txt"), downloadedDumpFile);
        downloadedDumpFile.delete();

        Export export0 = model.getExport("Export 0");
        taskResult = runTask(export0, null);
        assertNotNull(taskResult);
        assertTrue(taskResult.isSuccessful());
        ServerFile exportServerFile = model.getServerFile("Export 0");
        File downloadedExportFile = File.createTempFile("export", ".txt");
        exportServerFile.downLoad(downloadedExportFile, true);
        assertEquals(getTestDataFile("expected.txt"), downloadedExportFile);
        downloadedExportFile.delete();
    }
        
}
