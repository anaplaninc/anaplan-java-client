package com.anaplan.client.logging;

import com.anaplan.client.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {

  private LogUtils(){}

  private static final Logger LOG = LoggerFactory.getLogger(LogUtils.class);

  public static void logSeparatorRunAction() {
    final String logText = Strings.repeat("= ", 30);
    LOG.info(logText);
  }

  public static void logSeparatorUpload() {
    final String logText = Strings.repeat("↑ ", 30);
    LOG.info(logText);
  }

  public static void logSeparatorDownload() {
    final String logText = Strings.repeat("↓ ", 30);
    LOG.info(logText);
  }

  public static void logSeparatorOperationResponses() {
    final String logText = Strings.repeat("-", 30);
    LOG.info(logText);
  }

  public static void logSeparatorOperationStatus() {
    final String logText = Strings.repeat("- ", 21);
    LOG.info(logText);
  }
}
