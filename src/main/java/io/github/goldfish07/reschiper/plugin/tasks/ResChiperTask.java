package io.github.goldfish07.reschiper.plugin.tasks;

import io.github.goldfish07.reschiper.plugin.command.Command;
import io.github.goldfish07.reschiper.plugin.command.model.DuplicateResMergerCommand;
import io.github.goldfish07.reschiper.plugin.command.model.FileFilterCommand;
import io.github.goldfish07.reschiper.plugin.command.model.ObfuscateBundleCommand;
import io.github.goldfish07.reschiper.plugin.command.model.StringFilterCommand;
import io.github.goldfish07.reschiper.plugin.Extension;
import io.github.goldfish07.reschiper.plugin.ResChiper;
import io.github.goldfish07.reschiper.plugin.model.KeyStore;
import org.gradle.api.file.RegularFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom Gradle task for running ResChiper.
 */
public class ResChiperTask extends DefaultTask {

    private static final Logger logger = Logger.getLogger(ResChiperTask.class.getName());
    private Extension resChiperExtension;
    private String variantName;
    private KeyStore keyStore;
    private Provider<RegularFile> bundleFile;
    private File buildDirectory;
    private String projectName;
    private String agpVersion;
    private String gradleVersion;
    private Path bundlePath;
    private Path obfuscatedBundlePath;

    /**
     * Constructor for the ResChiperTask.
     */
    public ResChiperTask() {
        setDescription("Assemble resource proguard for bundle file");
        setGroup("bundle");
        getOutputs().upToDateWhen(task -> false);
    }

    public void setVariant(String variantName) {
        this.variantName = variantName;
    }

    public void setResChiperExtension(Extension resChiperExtension) {
        this.resChiperExtension = resChiperExtension;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public void setBundleFile(Provider<RegularFile> bundleFile) {
        this.bundleFile = bundleFile;
    }

    public void setBuildDirectory(File buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    public void setBuildConfiguration(String projectName, String agpVersion, String gradleVersion) {
        this.projectName = projectName;
        this.agpVersion = agpVersion;
        this.gradleVersion = gradleVersion;
    }

    /**
     * Executes the ResChiperTask.
     *
     * @throws Exception If an error occurs during execution.
     */
    @TaskAction
    public void execute() throws Exception {
        printResChiperBuildConfiguration();
        printProjectBuildConfiguration();
        logger.log(Level.INFO, resChiperExtension.toString());
        bundlePath = bundleFile.get().getAsFile().toPath();
        obfuscatedBundlePath = new File(bundlePath.toFile().getParentFile(), resChiperExtension.getObfuscatedBundleName()).toPath();
        printSignConfiguration();
        printOutputFileLocation();
        prepareUnusedFile();
        Command.Builder builder = Command.builder();
        builder.setBundlePath(bundlePath);
        builder.setOutputPath(obfuscatedBundlePath);

        ObfuscateBundleCommand.Builder obfuscateBuilder = ObfuscateBundleCommand.builder()
                .setEnableObfuscate(resChiperExtension.getEnableObfuscation())
                .setObfuscationMode(resChiperExtension.getObfuscationMode())
                .setMergeDuplicatedResources(resChiperExtension.getMergeDuplicateResources())
                .setWhiteList(resChiperExtension.getWhiteList())
                .setFilterFile(resChiperExtension.getEnableFileFiltering())
                .setFileFilterRules(resChiperExtension.getFileFilterList())
                .setRemoveStr(resChiperExtension.getEnableFilterStrings())
                .setUnusedStrPath(resChiperExtension.getUnusedStringFile())
                .setLanguageWhiteList(resChiperExtension.getLocaleWhiteList());
        if (resChiperExtension.getMappingFile() != null)
            obfuscateBuilder.setMappingPath(resChiperExtension.getMappingFile());
        if (resChiperExtension.getObfuscationSeed() != null)
            obfuscateBuilder.setObfuscationSeed(resChiperExtension.getObfuscationSeed());

        if (keyStore.storeFile() != null && keyStore.storeFile().exists())
            builder.setStoreFile(keyStore.storeFile().toPath())
                    .setKeyAlias(keyStore.keyAlias())
                    .setKeyPassword(keyStore.keyPassword())
                    .setStorePassword(keyStore.storePassword());

        builder.setObfuscateBundleBuilder(obfuscateBuilder.build());

        FileFilterCommand.Builder fileFilterBuilder = FileFilterCommand.builder();
        fileFilterBuilder.setFileFilterRules(resChiperExtension.getFileFilterList());
        builder.setFileFilterBuilder(fileFilterBuilder.build());

        StringFilterCommand.Builder stringFilterBuilder = StringFilterCommand.builder();
        builder.setStringFilterBuilder(stringFilterBuilder.build());

        DuplicateResMergerCommand.Builder duplicateResMergeBuilder = DuplicateResMergerCommand.builder();
        builder.setDuplicateResMergeBuilder(duplicateResMergeBuilder.build());

        Command command = builder.build(builder.build(), Command.TYPE.OBFUSCATE_BUNDLE);
        command.execute(Command.TYPE.OBFUSCATE_BUNDLE);
    }

    /**
     * Prints the ResChiper build configuration information.
     */
    private void printResChiperBuildConfiguration() {
        System.out.println("----------------------------------------");
        System.out.println(" ResChiper Plugin Configuration:");
        System.out.println("----------------------------------------");
        System.out.println("- ResChiper version:\t" + ResChiper.VERSION);
        System.out.println("- BundleTool version:\t" + ResChiper.BT_VERSION);
        System.out.println("- AGP version:\t\t" + ResChiper.AGP_VERSION);
        System.out.println("- Gradle Wrapper:\t" + ResChiper.GRADLE_WRAPPER_VERSION);
    }

    /**
     * Prints the project's build information.
     */
    private void printProjectBuildConfiguration() {
        System.out.println("----------------------------------------");
        System.out.println(" App Build Information:");
        System.out.println("----------------------------------------");
        System.out.println("- Project name:\t\t\t" + projectName);
        System.out.println("- AGP version:\t\t\t" + agpVersion);
        System.out.println("- Running Gradle version:\t" + gradleVersion);
    }

    /**
     * Prepares the unused file for filtering.
     */
    private void prepareUnusedFile() {
        String simpleName = variantName.replace("Release", "");
        String name = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        String resourcePath = buildDirectory + "/outputs/mapping/" + name + "/release/unused_strings.txt";
        File usedFile = new File(resourcePath);

        if (usedFile.exists()) {
            System.out.println("find unused_strings.txt: " + usedFile.getAbsolutePath());
            if (resChiperExtension.getEnableFilterStrings())
                if (resChiperExtension.getUnusedStringFile() == null || resChiperExtension.getUnusedStringFile().isBlank()) {
                    resChiperExtension.setUnusedStringFile(usedFile.getAbsolutePath());
                    logger.log(Level.SEVERE, "replace unused_strings.txt!");
                }
        } else
            logger.log(Level.SEVERE, "not exists unused_strings.txt: " + usedFile.getAbsolutePath()
                    + "\nuse default path: " + resChiperExtension.getUnusedStringFile());
    }

    /**
     * Prints the signing configuration.
     */
    private void printSignConfiguration() {
        System.out.println("----------------------------------------");
        System.out.println(" Signing Configuration");
        System.out.println("----------------------------------------");
        System.out.println("\tKeyStoreFile:\t\t" + keyStore.storeFile());
        System.out.println("\tKeyPassword:\t" + encrypt(keyStore.keyPassword()));
        System.out.println("\tAlias:\t\t\t" + encrypt(keyStore.keyAlias()));
        System.out.println("\tStorePassword:\t" + encrypt(keyStore.storePassword()));
    }

    /**
     * Prints the output file location.
     */
    private void printOutputFileLocation() {
        System.out.println("----------------------------------------");
        System.out.println(" Output configuration");
        System.out.println("----------------------------------------");
        System.out.println("\tFolder:\t\t" + obfuscatedBundlePath.getParent());
        System.out.println("\tFile:\t\t" + obfuscatedBundlePath.getFileName());
        System.out.println("----------------------------------------");
    }

    /**
     * Encrypts a value for printing (partially).
     *
     * @param value The value to encrypt.
     * @return The encrypted value.
     */
    private @NotNull String encrypt(String value) {
        if (value == null)
            return "/";
        if (value.length() > 2)
            return value.substring(0, value.length() / 2) + "****";
        return "****";
    }
}
