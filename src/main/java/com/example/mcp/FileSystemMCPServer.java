package com.example.mcp;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MCP Server for filesystem access
 * Provides secure, controlled access to local files
 */
@Service
public class FileSystemMCPServer {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemMCPServer.class);

    @Value("${mcp.filesystem.enabled:true}")
    private boolean enabled;

    @Value("${mcp.filesystem.root-path:/Users/mukundkumar/Documents}")
    private String rootPath;

    @Value("${mcp.filesystem.allowed-extensions:pdf,txt,md,docx,java,py,json,xml,html,css,js}")
    private String allowedExtensions;

    private Tika tika;
    private Set<String> allowedExtensionsSet;
    private Path rootDirectory;

    @PostConstruct
    public void initialize() {
        if (!enabled) {
            logger.warn("Filesystem MCP server is disabled");
            return;
        }

        tika = new Tika();
        allowedExtensionsSet = Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        rootDirectory = Paths.get(rootPath);

        if (!Files.exists(rootDirectory)) {
            logger.warn("Root path does not exist: {}. Creating it...", rootPath);
            try {
                Files.createDirectories(rootDirectory);
            } catch (IOException e) {
                logger.error("Failed to create root directory", e);
            }
        }

        logger.info("Filesystem MCP Server initialized. Root: {}, Allowed extensions: {}",
                rootPath, allowedExtensions);
    }

    /**
     * List files in a directory with optional pattern filtering
     */
    public List<FileInfo> listFiles(String relativePath, String pattern) throws IOException {
        if (!enabled) {
            throw new IllegalStateException("Filesystem MCP server is disabled");
        }

        Path targetPath = resolvePath(relativePath);
        validatePath(targetPath);

        if (!Files.isDirectory(targetPath)) {
            throw new IllegalArgumentException("Path is not a directory: " + relativePath);
        }

        List<FileInfo> files = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(targetPath, 1)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isAllowedExtension)
                    .filter(p -> pattern == null || p.getFileName().toString().matches(pattern))
                    .forEach(p -> {
                        try {
                            files.add(createFileInfo(p));
                        } catch (IOException e) {
                            logger.warn("Error reading file info: {}", p, e);
                        }
                    });
        }

        logger.debug("Listed {} files from {}", files.size(), relativePath);
        return files;
    }

    /**
     * Read file content with automatic text extraction
     */
    public String readFile(String relativePath) throws IOException {
        if (!enabled) {
            throw new IllegalStateException("Filesystem MCP server is disabled");
        }

        Path filePath = resolvePath(relativePath);
        validatePath(filePath);

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist: " + relativePath);
        }

        if (!isAllowedExtension(filePath)) {
            throw new IllegalArgumentException("File extension not allowed: " + relativePath);
        }

        // Use Apache Tika to extract text from various file formats
        try {
            String content = tika.parseToString(filePath);
            logger.debug("Read file: {} ({} characters)", relativePath, content.length());
            return content;
        } catch (org.apache.tika.exception.TikaException e) {
            throw new IOException("Failed to parse file: " + relativePath, e);
        }
    }

    /**
     * Search for files containing a query string
     */
    public List<FileInfo> searchFiles(String query, String relativePath) throws IOException {
        if (!enabled) {
            throw new IllegalStateException("Filesystem MCP server is disabled");
        }

        Path searchPath = resolvePath(relativePath);
        validatePath(searchPath);

        List<FileInfo> matchingFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(searchPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isAllowedExtension)
                    .forEach(p -> {
                        try {
                            String content = tika.parseToString(p);
                            if (content.toLowerCase().contains(query.toLowerCase())) {
                                matchingFiles.add(createFileInfo(p));
                            }
                        } catch (Exception e) {
                            logger.debug("Error searching file: {}", p, e);
                        }
                    });
        }

        logger.info("Found {} files matching query '{}' in {}", matchingFiles.size(), query, relativePath);
        return matchingFiles;
    }

    /**
     * Get file metadata
     */
    public FileInfo getFileMetadata(String relativePath) throws IOException {
        if (!enabled) {
            throw new IllegalStateException("Filesystem MCP server is disabled");
        }

        Path filePath = resolvePath(relativePath);
        validatePath(filePath);

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist: " + relativePath);
        }

        return createFileInfo(filePath);
    }

    /**
     * Resolve relative path to absolute path within root directory
     */
    private Path resolvePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty() || relativePath.equals(".")) {
            return rootDirectory;
        }
        return rootDirectory.resolve(relativePath).normalize();
    }

    /**
     * Validate that path is within allowed root directory
     */
    private void validatePath(Path path) throws IOException {
        if (!path.normalize().startsWith(rootDirectory.normalize())) {
            throw new SecurityException("Access denied: path outside root directory");
        }
    }

    /**
     * Check if file extension is allowed
     */
    private boolean isAllowedExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');

        if (lastDot == -1) {
            return false;
        }

        String extension = fileName.substring(lastDot + 1).toLowerCase();
        return allowedExtensionsSet.contains(extension);
    }

    /**
     * Create FileInfo object from Path
     */
    private FileInfo createFileInfo(Path path) throws IOException {
        FileInfo info = new FileInfo();
        info.setPath(rootDirectory.relativize(path).toString());
        info.setAbsolutePath(path.toString());
        info.setName(path.getFileName().toString());
        info.setSize(Files.size(path));
        info.setLastModified(Files.getLastModifiedTime(path).toMillis());
        info.setDirectory(Files.isDirectory(path));

        // Get MIME type
        try {
            info.setMimeType(tika.detect(path));
        } catch (Exception e) {
            info.setMimeType("application/octet-stream");
        }

        return info;
    }

    /**
     * File information model
     */
    public static class FileInfo {
        private String path;
        private String absolutePath;
        private String name;
        private long size;
        private long lastModified;
        private boolean isDirectory;
        private String mimeType;

        // Getters and Setters
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        public void setAbsolutePath(String absolutePath) {
            this.absolutePath = absolutePath;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        public boolean isDirectory() {
            return isDirectory;
        }

        public void setDirectory(boolean directory) {
            isDirectory = directory;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        public String toString() {
            return "FileInfo{path='" + path + "', size=" + size + ", mimeType='" + mimeType + "'}";
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
