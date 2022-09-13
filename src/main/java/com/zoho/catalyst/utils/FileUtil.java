package com.zoho.catalyst.utils;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtil {
    public static void copy(Path source, Path dest) throws RuntimeException {
        try {
            if(source.toFile().isDirectory()) {
                FileUtils.copyDirectory(source.toFile(), dest.toFile());
                return;
            }
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Unable to copy file: " + source.toString(), e);
        }
    }
}
