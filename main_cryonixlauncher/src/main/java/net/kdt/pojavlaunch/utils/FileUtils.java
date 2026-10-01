package net.kdt.pojavlaunch.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

/** Small Java-I/O helpers used by the launcher without an Apache Commons dependency. */
public class FileUtils {
    private static final int BUFFER_SIZE = 8192;

    public static boolean exists(String filePath) {
        return new File(filePath).exists();
    }

    public static String getFileName(String pathOrUrl) {
        int lastSlashIndex = pathOrUrl.lastIndexOf('/');
        if (lastSlashIndex == -1) return null;
        return pathOrUrl.substring(lastSlashIndex + 1);
    }

    public static String removeExtension(String pathOrUrl) {
        int lastSlash = Math.max(pathOrUrl.lastIndexOf('/'), pathOrUrl.lastIndexOf('\\'));
        int lastDotIndex = pathOrUrl.lastIndexOf('.');
        if (lastDotIndex <= lastSlash) return pathOrUrl;
        return pathOrUrl.substring(0, lastDotIndex);
    }

    public static boolean ensureDirectorySilently(File targetFile) {
        if (targetFile.isFile()) return false;
        if (targetFile.exists()) return targetFile.canWrite();
        return targetFile.mkdirs();
    }

    public static boolean ensureParentDirectorySilently(File targetFile) {
        File parentFile = targetFile.getParentFile();
        return parentFile != null && ensureDirectorySilently(parentFile);
    }

    public static void ensureDirectory(File targetFile) throws IOException {
        if (targetFile.isFile()) throw new IOException("Target directory is a file");
        if (targetFile.exists()) {
            if (!targetFile.canWrite()) throw new IOException("Target directory is not writable");
        } else if (!targetFile.mkdirs()) {
            throw new IOException("Unable to create target directory");
        }
    }

    public static void ensureParentDirectory(File targetFile) throws IOException {
        File parentFile = targetFile.getParentFile();
        if (parentFile == null) throw new IOException("targetFile does not have a parent");
        ensureDirectory(parentFile);
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, new byte[BUFFER_SIZE]);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long total = 0;
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
            total += count;
        }
        return total;
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static String toString(InputStream input, Charset charset) throws IOException {
        return new String(toByteArray(input), charset);
    }

    public static void write(String content, OutputStream output) throws IOException {
        output.write(content.getBytes(Charset.defaultCharset()));
    }

    public static void copyFile(File source, File destination, boolean preserveDate) throws IOException {
        ensureParentDirectory(destination);
        try (InputStream input = new FileInputStream(source);
             OutputStream output = new FileOutputStream(destination)) {
            copy(input, output);
        }
        if (preserveDate) destination.setLastModified(source.lastModified());
    }

    public static void copyInputStreamToFile(InputStream input, File destination) throws IOException {
        ensureParentDirectory(destination);
        try (InputStream source = input; OutputStream output = new FileOutputStream(destination)) {
            copy(source, output);
        }
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) return;
        if (Files.isSymbolicLink(directory.toPath()) || !directory.isDirectory()) {
            if (!directory.delete()) throw new IOException("Unable to delete " + directory);
            return;
        }
        File[] children = directory.listFiles();
        if (children == null) throw new IOException("Unable to list " + directory);
        for (File child : children) deleteDirectory(child);
        if (!directory.delete()) throw new IOException("Unable to delete " + directory);
    }

    public static Collection<File> listFiles(File directory, String[] extensions, boolean recursive) {
        ArrayList<File> files = new ArrayList<>();
        collectFiles(directory, extensions, recursive, files);
        return files;
    }

    private static void collectFiles(File directory, String[] extensions, boolean recursive, Collection<File> result) {
        File[] children = directory.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) {
                if (recursive) collectFiles(child, extensions, true, result);
            } else if (hasExtension(child, extensions)) {
                result.add(child);
            }
        }
    }

    private static boolean hasExtension(File file, String[] extensions) {
        if (extensions == null) return true;
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? "" : name.substring(dot + 1);
        for (String candidate : extensions) {
            if (candidate != null && candidate.equalsIgnoreCase(extension)) return true;
        }
        return false;
    }
}
