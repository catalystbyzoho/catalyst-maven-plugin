package com.zoho.catalyst.utils;

import lombok.extern.java.Log;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log
public class ProcessUtil {
    public static final boolean isWindows = SystemUtils.IS_OS_WINDOWS;
    public static final boolean isMac = SystemUtils.IS_OS_MAC;
    public static List<Process> processList = new ArrayList<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                processList.forEach(process -> {
                    try {
                        process.destroy();
                    } catch (Exception e) {
                        process.destroyForcibly();
                    }
                });
            }
        });
    }

    public static Process executeCommand(@NotNull List<String> command, String dir, Map<String, String> env) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        if (dir != null) {
            builder.directory(new File(dir));
        }
        if (env != null) {
            Map<String, String> envMap = builder.environment();
            envMap.putAll(env);
        }
        Process currentProcess = builder.inheritIO().start();
        processList.add(currentProcess);
        return currentProcess;
    }
}
