package io.github.goldfish07.reschiper.plugin.android;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for managing the Android debug keystore and its configurations.
 */
public class AndroidDebugKeyStoreHelper {

    public static final String DEFAULT_PASSWORD = "android";
    public static final String DEFAULT_ALIAS = "AndroidDebugKey";
    private static final Logger logger = Logger.getLogger(AndroidDebugKeyStoreHelper.class.getName());

    /**
     * Retrieves the debug signing configuration for Android.
     *
     * @return The debug signing configuration as a {@link JarSigner.Signature} object, or null if the keystore is not found.
     */
    public static @Nullable JarSigner.Signature debugSigningConfig() {
        String debugKeystoreLocation = defaultDebugKeystoreLocation();
        if (debugKeystoreLocation == null || !new File(debugKeystoreLocation).exists()) {
            return null;
        }
        return new JarSigner.Signature(
                Path.of(debugKeystoreLocation),
                DEFAULT_PASSWORD,
                DEFAULT_ALIAS,
                DEFAULT_PASSWORD
        );
    }

    /**
     * Returns the location of the default debug keystore.
     *
     * @return The location of the default debug keystore, or null if an error occurs.
     */
    private static @Nullable String defaultDebugKeystoreLocation() {
        try {
            String folder = AndroidLocation.getFolder();
            return folder + "debug.keystore";
        } catch (AndroidLocation.AndroidLocationException e) {
            logger.log(Level.SEVERE, "Error getting keystore folder", e);
            return null;
        }
    }
}
