package io.github.goldfish07.reschiper.plugin.android;

import com.android.tools.build.bundletool.flags.Flag;
import com.android.tools.build.bundletool.model.exceptions.CommandExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * This class provides functionality for signing JAR files in an Android context.
 */
public class JarSigner {

    /**
     * Signs the specified file using the provided signature information.
     *
     * @param toBeSigned The file to be signed.
     * @param signature  The signature information.
     * @throws IOException            If an I/O error occurs while signing the file.
     * @throws InterruptedException   If the signing process is interrupted.
     */
    public void sign(File toBeSigned, Signature signature) throws IOException, InterruptedException {
        new OpenJDKJarSigner().sign(toBeSigned, signature);
    }

    /**
     * Represents the signature information for signing JAR files.
     */
    public record Signature(Path storeFile, String storePassword, String keyAlias, String keyPassword) {
        /**
         * A constant representing the default debug signature configuration.
         */
        public static final Signature DEBUG_SIGNATURE = AndroidDebugKeyStoreHelper.debugSigningConfig();

        /**
         * Constructs a Signature object with the provided information.
         *
         * @param storeFile    The path to the keystore file.
         * @param storePassword The password for the keystore.
         * @param keyAlias     The alias for the key.
         * @param keyPassword  The password for the key.
         */
        public Signature(Path storeFile, String storePassword, String keyAlias, String keyPassword) {
            this.storeFile = storeFile;
            this.storePassword = storePassword;
            this.keyAlias = keyAlias;
            this.keyPassword = keyPassword;
            checkFileExistsAndReadable(storeFile);
            checkStringIsEmpty(storePassword, "storePassword");
            checkStringIsEmpty(keyAlias, "keyAlias");
            checkStringIsEmpty(keyPassword, "keyPassword");
        }
    }

    /**
     * Checks if the specified flag is present (not null) and throws an exception if it's not.
     *
     * @param object The object to check for presence.
     * @param flag   The flag being checked.
     * @throws CommandExecutionException If the flag is not present.
     */
    public static void checkFlagPresent(Object object, Flag<String> flag) {
        Optional.ofNullable(object)
                .orElseThrow(() -> CommandExecutionException.builder()
                        .withInternalMessage("Wrong properties: %s can not be empty", flag)
                        .build());
    }

    /**
     * Checks if the specified string value is empty (null or consists only of whitespace) and throws an exception if it is.
     *
     * @param value The string value to check.
     * @param name  The name of the property being checked.
     * @throws IllegalArgumentException If the string value is empty.
     */
    public static void checkStringIsEmpty(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("Wrong properties: %s can not be empty", name));
        }
    }
}
