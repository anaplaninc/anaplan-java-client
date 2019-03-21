package com.anaplan.client.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.anaplan.client.Program;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {

    private static final Logger LOG = LoggerFactory.getLogger(LogUtils.class);
    private static final String consoleAppenderName = "CONSOLE_STDOUT";
    private static final String debugPatternName = "DEBUG_STDOUT";

    /**
     * Enables debug logging in Logback.
     */
    public static void enableDebugLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        String packageName = Program.class.getPackage().getName();
        context.getLogger(packageName).setLevel(Level.DEBUG);

        ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) context.getLogger(packageName)
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
