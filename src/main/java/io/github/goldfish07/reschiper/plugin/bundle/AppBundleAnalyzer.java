package io.github.goldfish07.reschiper.plugin.bundle;

import com.android.tools.build.bundletool.model.AppBundle;
import io.github.goldfish07.reschiper.plugin.utils.TimeClock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * Utility class for analyzing Android App Bundles (AABs).
 */
public class AppBundleAnalyzer {

    private final Path bundlePath;

    /**
     * Constructs an AppBundleAnalyzer with the provided AAB file path.
     *
     * @param bundlePath The path to the Android App Bundle (AAB) file.
     * @throws IllegalArgumentException If the provided file path does not exist or is not readable.
     */
    public AppBundleAnalyzer(Path bundlePath) {
        checkFileExistsAndReadable(bundlePath);
        this.bundlePath = bundlePath;
    }

    /**
     * Analyzes the Android App Bundle (AAB) file and returns the parsed AppBundle.
     *
     * @return The parsed AppBundle.
     * @throws IOException If an I/O error occurs while analyzing the AAB file.
     */
    public AppBundle analyze() throws IOException {
        TimeClock timeClock = new TimeClock();
        ZipFile bundleZip = new ZipFile(bundlePath.toFile());
        AppBundle appBundle = AppBundle.buildFromZip(bundleZip);
        System.out.printf("Analysis of the bundle file completed, took %s%n", timeClock.getElapsedTime());
        return appBundle;
    }
}