package io.github.goldfish07.reschiper.plugin.bundle;

import com.android.tools.build.bundletool.model.BundleModule;
import com.android.tools.build.bundletool.model.ModuleEntry;
import com.android.tools.build.bundletool.model.ResourceTableEntry;
import com.android.tools.build.bundletool.model.ZipPath;
import com.android.tools.build.bundletool.model.utils.ZipUtils;
import io.github.goldfish07.reschiper.plugin.operations.FileOperation;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility methods for working with Android App Bundles (AABs).
 */
public class AppBundleUtils {

    /**
     * Get the size of a specific entry within the AAB.
     *
     * @param bundleZipFile The AAB as a ZipFile.
     * @param entry         The ModuleEntry for the entry.
     * @param bundleModule  The BundleModule containing the entry.
     * @return The size of the entry in bytes.
     */
    public static long getZipEntrySize(@NotNull ZipFile bundleZipFile, @NotNull ModuleEntry entry, @NotNull BundleModule bundleModule) {
        String path = String.format("%s/%s", bundleModule.getName().getName(), entry.getPath().toString());
        ZipEntry bundleConfigEntry = bundleZipFile.getEntry(path);
        return FileOperation.getZipPathFileSize(bundleZipFile, bundleConfigEntry);
    }

    /**
     * Get the size of a specific entry within the AAB.
     *
     * @param bundleZipFile The AAB as a ZipFile.
     * @param zipPath       The ZipPath of the entry.
     * @return The size of the entry in bytes.
     */
    public static long getZipEntrySize(@NotNull ZipFile bundleZipFile, @NotNull ZipPath zipPath) {
        String path = zipPath.toString();
        ZipEntry bundleConfigEntry = bundleZipFile.getEntry(path);
        return FileOperation.getZipPathFileSize(bundleZipFile, bundleConfigEntry);
    }

    /**
     * Get the MD5 hash of a specific entry within the AAB.
     *
     * @param bundleZipFile The AAB as a ZipFile.
     * @param entry         The ModuleEntry for the entry.
     * @param bundleModule  The BundleModule containing the entry.
     * @return The MD5 hash as a hexadecimal string.
     */
    public static @NotNull String getEntryMd5(@NotNull ZipFile bundleZipFile, @NotNull ModuleEntry entry, @NotNull BundleModule bundleModule) {
        String path = String.format("%s/%s", bundleModule.getName().getName(), entry.getPath().toString());
        ZipEntry bundleConfigEntry = bundleZipFile.getEntry(path);
        try {
            byte[] bs = ZipUtils.asByteSource(bundleZipFile, bundleConfigEntry).read();
            return bytesToHexString(DigestUtils.md5(bs));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the content of a specific entry within the AAB as bytes.
     *
     * @param bundleZipFile The AAB as a ZipFile.
     * @param entry         The ModuleEntry for the entry.
     * @param bundleModule  The BundleModule containing the entry.
     * @return The content of the entry as bytes.
     * @throws IOException If an I/O error occurs.
     */
    public static byte[] readByte(@NotNull ZipFile bundleZipFile, @NotNull ModuleEntry entry, @NotNull BundleModule bundleModule) throws IOException {
        String path = String.format("%s/%s", bundleModule.getName().getName(), entry.getPath().toString());
        ZipEntry bundleConfigEntry = bundleZipFile.getEntry(path);
        return ZipUtils.asByteSource(bundleZipFile, bundleConfigEntry).read();
    }

    /**
     * Convert a byte array to a hexadecimal string.
     *
     * @param src The byte array to convert.
     * @return The hexadecimal string.
     */
    public static @NotNull String bytesToHexString(byte @NotNull [] src) {
        if (src.length == 0)
            return "";
        StringBuilder stringBuilder = new StringBuilder(src.length);
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2)
                stringBuilder.append(0);
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Get the entry name from a resource name in the format "package.type.entry".
     *
     * @param resourceName The resource name.
     * @return The entry name.
     */
    public static String getEntryNameByResourceName(@NotNull String resourceName) {
        int index = resourceName.indexOf(".R.");
        String value = resourceName.substring(index + 3);
        String[] values = value.replace(".", "/").split("/");
        if (values.length != 2)
            throw new RuntimeException("Invalid resource format, it should be package.type.entry, yours: " + resourceName);
        return values[values.length - 1];
    }

    /**
     * Get the type name from a resource name in the format "package.type.entry".
     *
     * @param resourceName The resource name.
     * @return The type name.
     */
    public static String getTypeNameByResourceName(@NotNull String resourceName) {
        int index = resourceName.indexOf(".R.");
        String value = resourceName.substring(index + 3);
        String[] values = value.replace(".", "/").split("/");
        if (values.length != 2)
            throw new RuntimeException("Invalid resource format, it should be package.type.entry, yours: " + resourceName);
        return values[0];
    }

    /**
     * Get the full resource name from a ResourceTableEntry.
     *
     * @param entry The ResourceTableEntry.
     * @return The full resource name.
     */
    public static String getResourceFullName(@NotNull ResourceTableEntry entry) {
        return getResourceFullName(entry.getPackage().getPackageName(), entry.getType().getName(), entry.getEntry().getName());
    }

    /**
     * Get the full resource name from individual components.
     *
     * @param packageName The package name.
     * @param typeName    The type name.
     * @param entryName   The entry name.
     * @return The full resource name.
     */
    public static String getResourceFullName(String packageName, String typeName, String entryName) {
        return String.format("%s.R.%s.%s", packageName, typeName, entryName);
    }
}