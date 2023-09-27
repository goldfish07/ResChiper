package io.github.goldfish07.reschiper.plugin.bundle;

import io.github.goldfish07.reschiper.plugin.android.JarSigner;
import io.github.goldfish07.reschiper.plugin.utils.TimeClock;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility class for signing an Android App Bundle (AAB) using a JarSigner.
 */
public class AppBundleSigner {

    private final Path bundleFile;
    private JarSigner.Signature bundleSignature = JarSigner.Signature.DEBUG_SIGNATURE;

    /**
     * Constructs an AppBundleSigner with the provided AAB file path and signature.
     *
     * @param bundleFile The path to the Android App Bundle (AAB) file to sign.
     * @param signature  The signature to use for signing the AAB.
     */
    public AppBundleSigner(Path bundleFile, JarSigner.Signature signature) {
        this.bundleFile = bundleFile;
        this.bundleSignature = signature;
    }

    /**
     * Constructs an AppBundleSigner with the provided AAB file path.
     *
     * @param bundleFile The path to the Android App Bundle (AAB) file to sign.
     */
    public AppBundleSigner(Path bundleFile) {
        this.bundleFile = bundleFile;
    }

    /**
     * Sets the signature to use for signing the AAB.
     *
     * @param bundleSignature The signature to use for signing the AAB.
     */
    public void setBundleSignature(JarSigner.Signature bundleSignature) {
        this.bundleSignature = bundleSignature;
    }

    /**
     * Executes the signing of the Android App Bundle (AAB) using the specified JarSigner signature.
     *
     * @throws IOException          If an I/O error occurs during the signing process.
     * @throws InterruptedException If the signing process is interrupted.
     */
    public void execute() throws IOException, InterruptedException {
        if (bundleSignature == null)
            return;
        System.out.println(
                """
                ----------------------------------------
                 Signing:
                ----------------------------------------
                - Signing the bundle...""");
        TimeClock timeClock = new TimeClock();
        JarSigner.Signature signature = new JarSigner.Signature(
                bundleSignature.storeFile(),
                bundleSignature.storePassword(),
                bundleSignature.keyAlias(),
                bundleSignature.keyPassword()
        );
        new JarSigner().sign(bundleFile.toFile(), signature);
        System.out.printf("- Signing completed in %s%n\n", timeClock.getElapsedTime());
    }
}