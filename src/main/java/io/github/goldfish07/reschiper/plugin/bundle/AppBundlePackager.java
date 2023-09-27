package io.github.goldfish07.reschiper.plugin.bundle;

import com.android.tools.build.bundletool.io.AppBundleSerializer;
import com.android.tools.build.bundletool.model.AppBundle;
import io.github.goldfish07.reschiper.plugin.utils.TimeClock;

import java.io.IOException;
import java.nio.file.Path;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileDoesNotExist;

/**
 * Utility class for packaging an Android App Bundle (AAB) and writing it to an output file.
 */
public class AppBundlePackager {
    private final Path output;
    private final AppBundle appBundle;

    /**
     * Constructs an AppBundlePackager with the provided AppBundle and output path.
     *
     * @param appBundle The Android App Bundle (AAB) to package and write.
     * @param output    The path to the output file where the packaged AAB will be written.
     * @throws IllegalArgumentException If the output file already exists.
     */
    public AppBundlePackager(AppBundle appBundle, Path output) {
        this.output = output;
        this.appBundle = appBundle;
        checkFileDoesNotExist(output);
    }

    /**
     * Executes the packaging of the Android App Bundle (AAB) and writes it to the output file.
     *
     * @throws IOException If an I/O error occurs during packaging or writing.
     */
    public void execute() throws IOException {
        System.out.println(
                """
                ----------------------------------------
                 Resource Packaging:
                ----------------------------------------
                - Packaging the bundle...""");
        TimeClock timeClock = new TimeClock();
        AppBundleSerializer appBundleSerializer = new AppBundleSerializer();
        appBundleSerializer.writeToDisk(appBundle, output);
        System.out.printf("- Packaging completed in: %s%n\n", timeClock.getElapsedTime());
    }
}