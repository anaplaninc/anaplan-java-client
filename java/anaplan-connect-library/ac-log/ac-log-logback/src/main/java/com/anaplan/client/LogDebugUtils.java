package com.anaplan.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;

public class LogDebugUtils {

  private static final String consoleAppenderName = "CONSOLE_STDOUT";
  private static final String debugPatternName = "DEBUG_STDOUT";

  /**
   * Enables debug logging in Logback.
   */
  public static void enableDebugLogging() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    String packageName = LogDebugUtils.class.getPackage().getName();
    context.getLogger(packageName).setLevel(Level.DEBUG);

    ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) context
        .getLogger(packageName)
        .getAppender(consoleAppenderName);
    if (consoleAppender != null) {
      consoleAppender.stop();

      PatternLayout debugLayout = new PatternLayout();
      debugLayout.setPattern(context.getProperty(debugPatternName));
      debugLayout.setContext(context);
      debugLayout.start();

      consoleAppender.setLayout(debugLayout);
      consoleAppender.setContext(context);
      consoleAppender.start();
    }
  }

}
