package com.zoho.catalyst.utils;

import org.apache.maven.plugin.logging.Log;

import java.util.logging.*;

public class LoggerUtil implements Log {

    private static final Logger[] PINNED_LOGGERS;
    private static Log PLUGIN_LOGGER = null;

    static {
        //Assuming the default config file.
        PINNED_LOGGERS = new Logger[]{
                Logger.getLogger(""),
                Logger.getLogger(LoggerUtil.class.getName())
        };


        Handler logHandler = new CatalystLogHandler();
        logHandler.setFormatter(new CatalystFormatter());

        // remove all existing handlers and add our log handler
        for (Logger l : PINNED_LOGGERS) {
            for (Handler h : l.getHandlers()) {
                l.removeHandler(h);
            }
            l.addHandler(logHandler);
        }
    }

    private LoggerUtil(Log pluginLogger) {
        PLUGIN_LOGGER = pluginLogger;
    }

    public static Log init(Log l) {
        return new LoggerUtil(l);
    }

    @Override
    public boolean isDebugEnabled() {
        return PLUGIN_LOGGER.isDebugEnabled();
    }

    @Override
    public void debug(CharSequence charSequence) {
        PLUGIN_LOGGER.debug(charSequence);
    }

    @Override
    public void debug(CharSequence charSequence, Throwable throwable) {
        PLUGIN_LOGGER.debug(charSequence, throwable);
    }

    @Override
    public void debug(Throwable throwable) {
        PLUGIN_LOGGER.debug(throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return PLUGIN_LOGGER.isInfoEnabled();
    }

    @Override
    public void info(CharSequence charSequence) {
        PLUGIN_LOGGER.info(charSequence);
    }

    @Override
    public void info(CharSequence charSequence, Throwable throwable) {
        PLUGIN_LOGGER.info(charSequence, throwable);
    }

    @Override
    public void info(Throwable throwable) {
        PLUGIN_LOGGER.info(throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return PLUGIN_LOGGER.isWarnEnabled();
    }

    @Override
    public void warn(CharSequence charSequence) {
        PLUGIN_LOGGER.warn(charSequence);
    }

    @Override
    public void warn(CharSequence charSequence, Throwable throwable) {
        PLUGIN_LOGGER.warn(charSequence, throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        PLUGIN_LOGGER.warn(throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return PLUGIN_LOGGER.isErrorEnabled();
    }

    @Override
    public void error(CharSequence charSequence) {
        PLUGIN_LOGGER.error(charSequence);
    }

    @Override
    public void error(CharSequence charSequence, Throwable throwable) {
        PLUGIN_LOGGER.error(charSequence, throwable);
    }

    @Override
    public void error(Throwable throwable) {
        PLUGIN_LOGGER.error(throwable);
    }

    private static class CatalystLogHandler extends Handler {
        @Override
        public void publish(LogRecord rec) {
            Level recLevel = rec.getLevel();
            CharSequence msg = getFormatter().format(rec);
            Throwable throwable = rec.getThrown();
            if (throwable != null && recLevel.intValue() < Level.FINE.intValue()) {
                recLevel = Level.FINE;
            }
            if (recLevel.equals(Level.INFO)) {
                PLUGIN_LOGGER.info(msg);
            } else if (recLevel.equals(Level.WARNING)) {
                PLUGIN_LOGGER.warn(msg);
            } else if (recLevel.equals(Level.SEVERE)) {
                PLUGIN_LOGGER.error(msg);
            } else if (recLevel.equals(Level.FINE)) {
                PLUGIN_LOGGER.debug(msg, throwable);
            } else {
                PLUGIN_LOGGER.info(msg);
            }
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }

    private static class CatalystFormatter extends Formatter {

        @Override
        public String format(LogRecord rec) {
            String formattedMsg = formatMessage(rec);
            if (formattedMsg.isEmpty()) {
                return System.lineSeparator();
            }
            return "[CATALYST] " + formatMessage(rec) + System.lineSeparator();
        }
    }
}
