package com.zoho.catalyst.utils;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ProcessUtil {
    public static final boolean isWindows = SystemUtils.IS_OS_WINDOWS;
    public static final boolean isMac = SystemUtils.IS_OS_MAC;

    public static final char classPathSep = isWindows ? ';' : ':';

    public static Process executeCommand(@NotNull List<String> command, String dir, Map<String, String> env) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        if(dir != null) {
            builder.directory(new File(dir));
        }
        if(env != null) {
            Map<String, String> envMap = builder.environment();
            envMap.putAll(env);
        }
        return builder.inheritIO().start();
    }
}
