package org.tsuyoi.edgecomp.utilities;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RollingFileAppender {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private Path logFolder;

    public RollingFileAppender(Path logFolder) throws IllegalArgumentException, IOException {
        if (logFolder == null)
            throw new IllegalArgumentException("logFolder cannot be null");
        if (!Files.exists(logFolder))
            Files.createDirectories(logFolder);
        this.logFolder = logFolder;
    }

    public void append(String message) throws IOException {
        if (!message.endsWith("\n"))
            message = message + "\n";
        Path logFile = getLogFile();
        FileUtils.writeStringToFile(logFile.toFile(), message, true);
    }

    private Path getLogFile() throws IOException {
        compressOldLogs();
        return logFolder.resolve(String.format("%s.log", FORMAT.format(new Date())));
    }

    private void compressOldLogs() throws IOException {
        DirectoryStream<Path> logFiles = Files.newDirectoryStream(logFolder, "*.log");
        for (Path logFile : logFiles) {
            if (logFile.endsWith(String.format("%s.log", FORMAT.format(new Date()))))
                continue;
            compressOldLogFile(logFile);
        }
    }

    private void compressOldLogFile(Path logFile) throws IOException {
        Path compressedLogFile = logFolder.resolve(logFile.getFileName() + ".gz");
        try (GzipCompressorOutputStream o = new GzipCompressorOutputStream(Files.newOutputStream(compressedLogFile))) {
            FileUtils.copyFile(logFile.toFile(), o);
        }
        Files.deleteIfExists(logFile);
    }
}
