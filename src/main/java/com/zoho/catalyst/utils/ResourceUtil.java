package com.zoho.catalyst.utils;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceUtil {
    // get a file from the resources folder
    public InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    // Get all paths from a folder that inside the JAR file
    public List<Path> getPathsFromJAR(String folder) throws IOException, URISyntaxException {
        List<Path> result;
        // get path of the current running JAR
        String jarPath = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
        // file walks JAR
        URI uri = URI.create("jar:file:" + jarPath);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            try(Stream<Path> files = Files.walk(fs.getPath(folder))) {
                result = files.filter(Files::isRegularFile).collect(Collectors.toList());
            }
        }
        return result;
    }

    public void transferResourceFromJar(String inputFolder, Path outputDir) throws IOException, URISyntaxException {
        List<Path> serverFilesInsideJar = getPathsFromJAR(inputFolder);
        for (Path serverFilePth : serverFilesInsideJar) {
            String serverFile = serverFilePth.toString();
            if(serverFile.startsWith("/")) {
                serverFile = serverFile.substring(1, serverFile.length());
            }
            try(InputStream resStream = getFileFromResourceAsStream(serverFile)) {
                FileUtils.copyInputStreamToFile(resStream, outputDir.resolve(Paths.get(inputFolder).relativize(Paths.get(serverFile))).toFile());
            }
        }
    }
}
