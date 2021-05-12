package com.anaplan.client.logging;

import com.anaplan.client.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {

  private static final Logger LOG = LoggerFactory.getLogger(LogUtils.class);

  public static void logSeparatorRunAction() {
    LOG.info(Strings.repeat("= ", 30));
  }

  public static void logSeparatorUpload() {
    LOG.info(Strings.repeat("↑ ", 30));
  }

  public static void logSeparatorDownload() {
    LOG.info(Strings.repeat("↓ ", 30));
  }

  public static void logSeparatorOperationResponses() {
    LOG.info(Strings.repeat("-", 41));
  }

  public static void logSeparatorOperationStatus() {
    LOG.info(Strings.repeat("- ", 21));
  }
}
