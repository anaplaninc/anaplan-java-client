// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import java.io.File;
import java.io.IOException;

public class ProcessTest extends BaseTest {
    public void testProcess() throws AnaplanAPIException, IOException {
        Model model = getTestModel();

        ServerFile serverFile0 = model.getServerFile("File 0");
        File sourceFile0 = getTestDataFile("source.txt");
        serverFile0.upLoad(sourceFile0, true);

        Process process0 = model.getProcess("Process 0");
        TaskParameters taskParameters = new TaskParameters();
        taskParameters.addMappingParameter("Line Item", "Line Item 0");
        TaskResult taskResult = runTask(process0, taskParameters);
        assertTrue(taskResult.isSuccessful());
        assertEquals(5, taskResult.getNestedResults().size());

        TaskResult subtaskResult = taskResult.getNestedResults().get(0);
        assertTrue(subtaskResult.isSuccessful());
        assertTrue(subtaskResult.isFailureDumpAvailable());
        assertEquals("Failures for Import 0", subtaskResult.getObjectName());
        ServerFile dumpFile = subtaskResult.getFailureDump();
        File downloadedDumpFile = File.createTempFile("listdump", ".txt");
        dumpFile.downLoad(downloadedDumpFile, true);
        assertEquals(getTestDataFile("listdump.txt"), downloadedDumpFile);
        downloadedDumpFile.delete();

        subtaskResult = taskResult.getNestedResults().get(1);
        assertTrue(subtaskResult.isSuccessful());
        assertFalse(subtaskResult.isFailureDumpAvailable());

        subtaskResult = taskResult.getNestedResults().get(2);
        assertTrue(subtaskResult.isSuccessful());
        assertFalse(subtaskResult.isFailureDumpAvailable());

        subtaskResult = taskResult.getNestedResults().get(3);
        assertTrue(subtaskResult.isSuccessful());
        assertTrue(subtaskResult.isFailureDumpAvailable());
        assertEquals("Failures for Import 1", subtaskResult.getObjectName());
        dumpFile = subtaskResult.getFailureDump();
        downloadedDumpFile = File.createTempFile("moduledump", ".txt");
        dumpFile.downLoad(downloadedDumpFile, true);
        assertEquals(getTestDataFile("moduledump.txt"), downloadedDumpFile);
        downloadedDumpFile.delete();

        subtaskResult = taskResult.getNestedResults().get(4);
        assertTrue(subtaskResult.isSuccessful());
        assertFalse(subtaskResult.isFailureDumpAvailable());
        ServerFile exportServerFile = model.getServerFile("Export 0");
        File downloadedExportFile = File.createTempFile("export", ".txt");
        exportServerFile.downLoad(downloadedExportFile, true);
        assertEquals(getTestDataFile("expected.txt"), downloadedExportFile);
        downloadedExportFile.delete();
    }
}
