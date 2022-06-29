package com.zoho.catalyst.utils;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthUtil {
    public static Path getOSConfigDir(String appName) {
        // Ref: https://github.com/sindresorhus/env-paths#pathsconfig
        String userHome = SystemUtils.getUserHome().getAbsolutePath();
        if(SystemUtils.IS_OS_WINDOWS) {
            // OS is windows
            return Paths.get(userHome, "AppData", "Roaming", appName, "Config");
        }
        if(SystemUtils.IS_OS_MAC) {
            // OS is mac
            return Paths.get(userHome, "Library", "Preferences", appName);
        }
        // OS is linux
        return Paths.get(userHome, ".config", appName);
    }
}
