package io.github.goldfish07.reschiper.plugin.operations;

import com.android.bundle.Files;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for working with Native Libraries in Android App Bundles.
 */
public class NativeLibrariesOperation {

    /**
     * Removes a directory from Native Libraries if it exists.
     *
     * @param nativeLibraries The NativeLibraries object to modify.
     * @param zipPath         The path of the directory to remove.
     * @return The modified NativeLibraries object.
     */
    public static Files.NativeLibraries removeDirectory(Files.@NotNull NativeLibraries nativeLibraries, String zipPath) {
        int index = -1;
        for (int i = 0; i < nativeLibraries.getDirectoryList().size(); i++) {
            Files.TargetedNativeDirectory directory = nativeLibraries.getDirectoryList().get(i);
            if (directory.getPath().equals(zipPath)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return nativeLibraries;
        }
        return nativeLibraries.toBuilder().removeDirectory(index).build();
    }
}