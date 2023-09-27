package io.github.goldfish07.reschiper.plugin.operations;

import com.android.tools.build.bundletool.model.ZipPath;
import com.android.tools.build.bundletool.model.utils.ZipUtils;
import com.android.tools.build.bundletool.model.utils.files.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * Utility class for various file operations.
 */
public class FileOperation {
    private static final Logger logger = Logger.getLogger(FileOperation.class.getName());
    private static final int BUFFER = 8192;

    /**
     * Recursively deletes a directory and its contents.
     *
     * @param file The directory to delete.
     * @return true if the directory was successfully deleted, false otherwise.
     */
    public static boolean deleteDir(File file) {
        if (file == null || (!file.exists())) {
            return false;
        }
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File value : files) {
                    deleteDir(value);
                }
            }
        }
        file.delete();
        return true;
    }

    /**
     * Uncompressed a ZIP file to a target directory.
     *
     * @param uncompressedFile The ZIP file to uncompress.
     * @param targetDir        The target directory to extract the contents.
     * @throws IOException If an I/O error occurs during the uncompressed.
     */
    public static void uncompress(Path uncompressedFile, Path targetDir) throws IOException {
        checkFileExistsAndReadable(uncompressedFile);
        if (Files.exists(targetDir)) {
            targetDir.toFile().delete();
        } else {
            FileUtils.createDirectories(targetDir);
        }
        ZipFile zipFile = new ZipFile(uncompressedFile.toFile());
        try (zipFile) {
            Enumeration<? extends ZipEntry> emu = zipFile.entries();
            while (emu.hasMoreElements()) {
                ZipEntry entry = emu.nextElement();
                if (entry.isDirectory()) {
                    FileUtils.createDirectories(new File(targetDir.toFile(), entry.getName()).toPath());
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                File file = new File(targetDir.toFile() + File.separator + entry.getName());
                File parent = file.getParentFile();
                if (parent != null && (!parent.exists())) {
                    FileUtils.createDirectories(parent.toPath());
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);
                byte[] buf = new byte[BUFFER];
                int len;
                while ((len = bis.read(buf, 0, BUFFER)) != -1) {
                    fos.write(buf, 0, len);
                }
                bos.flush();
                bos.close();
                bis.close();
            }
        }
    }

    /**
     * Gets a human-readable file size description from a file size in bytes.
     *
     * @param size The file size in bytes.
     * @return A string representing the file size with appropriate units (B, KB, MB, GB).
     */
    public static @NotNull String getNetFileSizeDescription(long size) {
        StringBuilder bytes = new StringBuilder();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    /**
     * Gets the size of a file in bytes.
     *
     * @param f The file to get the size of.
     * @return The file size in bytes.
     */
    public static long getFileSizes(@NotNull File f) {
        long size = 0;
        if (f.exists() && f.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                size = fis.available();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Unable to get FileSize", e);
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Unable to get file size", e);
                }
            }
        }
        return size;
    }

    /**
     * Gets the size of a file within a ZIP archive.
     *
     * @param zipFile   The ZIP file.
     * @param zipEntry  The ZIP entry representing the file.
     * @return The size of the file in bytes.
     */
    public static long getZipPathFileSize(ZipFile zipFile, ZipEntry zipEntry) {
        long size = 0;
        //todo changed
        try {
            InputStream is = ZipUtils.asByteSource(zipFile, zipEntry).openStream();
            size = is.available();
            is.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to get ZipPath file size", e);
        }
        return size;
    }

    /**
     * Copies a file from a source location to a destination location using streams.
     *
     * @param source The source file to copy.
     * @param dest   The destination file.
     * @throws IOException If an I/O error occurs during the copying process.
     */
    public static void copyFileUsingStream(File source, @NotNull File dest) throws IOException {
        FileInputStream is = null;
        FileOutputStream os = null;
        File parent = dest.getParentFile();
        if (parent != null && (!parent.exists())) {
            parent.mkdirs();
        }
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest, false);

            byte[] buffer = new byte[BUFFER];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    /**
     * Gets the simple name of a file from a ZipPath.
     *
     * @param zipPath The ZipPath representing the file.
     * @return The simple name of the file.
     */
    public static @NotNull String getFileSimpleName(@NotNull ZipPath zipPath) {
        return zipPath.getFileName().toString();
    }

    /**
     * Gets the file suffix (extension) from a ZipPath.
     *
     * @param zipPath The ZipPath representing the file.
     * @return The file suffix (extension).
     */
    public static @NotNull String getFileSuffix(@NotNull ZipPath zipPath) {
        String fileName = zipPath.getName(zipPath.getNameCount() - 1).toString();
        if (!fileName.contains(".")) {
            return fileName;
        }
        String[] values = fileName.replace(".", "/").split("/");
        return fileName.substring(values[0].length());
    }

    /**
     * Gets the parent directory path from a Zip file path.
     *
     * @param zipPath The Zip file path.
     * @return The parent directory path.
     */
    public static @NotNull String getParentFromZipFilePath(@NotNull String zipPath) {
        if (!zipPath.contains("/")) {
            throw new IllegalArgumentException("invalid zipPath: " + zipPath);
        }
        String[] values = zipPath.split("/");
        return zipPath.substring(0, zipPath.indexOf(values[values.length - 1]) - 1);
    }

    /**
     * Gets the name of a file from a Zip file path.
     *
     * @param zipPath The Zip file path.
     * @return The file name.
     */
    public static String getNameFromZipFilePath(@NotNull String zipPath) {
        if (!zipPath.contains("/")) {
            throw new IllegalArgumentException("invalid zipPath: " + zipPath);
        }
        String[] values = zipPath.split("/");
        return values[values.length - 1];
    }

    /**
     * Gets the file prefix (name without extension) from a file name.
     *
     * @param fileName The file name.
     * @return The file prefix.
     */
    public static String getFilePrefixByFileName(@NotNull String fileName) {
        if (!fileName.contains(".")) {
            throw new IllegalArgumentException("invalid file name: " + fileName);
        }
        String[] values = fileName.replace(".", "/").split("/");
        return values[0];
    }
}