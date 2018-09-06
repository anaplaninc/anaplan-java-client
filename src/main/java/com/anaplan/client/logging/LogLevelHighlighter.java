package com.anaplan.client.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

/**
 * Sets Logback level colors
 */
public class LogLevelHighlighter extends ForegroundCompositeConverterBase<ILoggingEvent> {
    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        switch (level.toInt()) {
            case Level.DEBUG_INT:
                return ANSIConstants.YELLOW_FG;
            case Level.ERROR_INT:
                return ANSIConstants.BOLD + ANSIConstants.RED_FG; // same as default color scheme
            case Level.WARN_INT:
                return ANSIConstants.RED_FG;// same as default color scheme
            case Level.INFO_INT:
                return ANSIConstants.GREEN_FG;
            default:
                return ANSIConstants.DEFAULT_FG;
        }
    }
}
