package io.github.goldfish07.reschiper.plugin.model;

import java.io.File;

/**
 * Represents a keystore containing a cryptographic key pair for signing purposes.
 */
public record KeyStore(File storeFile, String storePassword, String keyAlias, String keyPassword) {
    /**
     * Constructs a new KeyStore with the provided parameters.
     *
     * @param storeFile    The keystore file.
     * @param storePassword The password for the keystore.
     * @param keyAlias     The alias for the key within the keystore.
     * @param keyPassword  The password for the key.
     */
    public KeyStore(File storeFile, String storePassword, String keyAlias, String keyPassword) {
        this.storeFile = storeFile;
        this.storePassword = storePassword;
        this.keyAlias = keyAlias;
        this.keyPassword = keyPassword;
    }
}
