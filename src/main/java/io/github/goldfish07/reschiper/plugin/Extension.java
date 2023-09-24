package io.github.goldfish07.reschiper.plugin;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class for customizing the behavior of the ResChiper tool or plugin.
 */
public class Extension {
    private boolean enableObfuscation = true;
    private String obfuscationMode = "default";
    private boolean enableFileFiltering = false;
    private boolean enableFilterStrings = false;
    private boolean mergeDuplicateResources = false;
    private Path mappingFile = null;
    private String obfuscatedBundleName;
    private String unusedStringFile = "";
    private Set<String> fileFilterList = new HashSet<>();
    private Set<String> whiteList = new HashSet<>();
    private Set<String> localeWhiteList = new HashSet<>();

    /**
     * Gets whether obfuscation is enabled.
     *
     * @return {@code true} if obfuscation is enabled; otherwise, {@code false}.
     */
    public boolean getEnableObfuscation() {
        return enableObfuscation;
    }

    /**
     * Sets whether obfuscation should be enabled.
     *
     * @param enableObfuscation {@code true} to enable obfuscation; {@code false} to disable it.
     */
    public void setEnableObfuscation(boolean enableObfuscation) {
        this.enableObfuscation = enableObfuscation;
    }

    /**
     * Gets the current resource obfuscation mode.
     *
     * @return The resource obfuscation mode as a string.
     */
    public String getObfuscationMode() {
        return obfuscationMode;
    }

    /**
     * Sets the resource obfuscation mode.
     *
     * @param obfuscationMode The resource obfuscation mode to set as a string.
     */
    public void setObfuscationMode(String obfuscationMode) {
        this.obfuscationMode = obfuscationMode;
    }

    /**
     * Gets whether file filtering is enabled.
     *
     * @return {@code true} if file filtering is enabled; otherwise, {@code false}.
     */
    public boolean getEnableFileFiltering() {
        return enableFileFiltering;
    }

    /**
     * Sets whether file filtering should be enabled.
     *
     * @param enableFileFiltering {@code true} to enable file filtering; {@code false} to disable it.
     */
    public void setEnableFileFiltering(boolean enableFileFiltering) {
        this.enableFileFiltering = enableFileFiltering;
    }

    /**
     * Gets whether string filtering is enabled.
     *
     * @return {@code true} if string filtering is enabled; otherwise, {@code false}.
     */
    public boolean getEnableFilterStrings() {
        return enableFilterStrings;
    }

    /**
     * Sets whether string filtering should be enabled.
     *
     * @param enableFilterStrings {@code true} to enable string filtering; {@code false} to disable it.
     */
    public void setEnableFilterStrings(boolean enableFilterStrings) {
        this.enableFilterStrings = enableFilterStrings;
    }

    /**
     * Gets whether duplicated resources should be merged.
     *
     * @return {@code true} if duplicated resources should be merged; otherwise, {@code false}.
     */
    public boolean getMergeDuplicateResources() {
        return mergeDuplicateResources;
    }

    /**
     * Sets whether duplicated resources should be merged.
     *
     * @param mergeDuplicateResources {@code true} to merge duplicated resources; {@code false} to keep them separate.
     */
    public void setMergeDuplicateResources(boolean mergeDuplicateResources) {
        this.mergeDuplicateResources = mergeDuplicateResources;
    }

    /**
     * Gets the path to the mapping file used for obfuscation.
     *
     * @return The path to the mapping file.
     */
    public Path getMappingFile() {
        return mappingFile;
    }

    /**
     * Sets the path to the mapping file used for obfuscation.
     *
     * @param mappingFile The path to the mapping file.
     */
    public void setMappingFile(Path mappingFile) {
        this.mappingFile = mappingFile;
    }

    /**
     * Gets the name of the obfuscated bundle file.
     *
     * @return The name of the obfuscated bundle file.
     */
    public String getObfuscatedBundleName() {
        return obfuscatedBundleName;
    }

    /**
     * Sets the name of the obfuscated bundle file.
     *
     * @param obfuscatedBundleName The name of the obfuscated bundle file.
     */
    public void setObfuscatedBundleName(String obfuscatedBundleName) {
        this.obfuscatedBundleName = obfuscatedBundleName;
    }

    /**
     * Gets the path to the file containing unused strings.
     *
     * @return The path to the unused string file.
     */
    public String getUnusedStringFile() {
        return unusedStringFile;
    }

    /**
     * Sets the path to the file containing unused strings.
     *
     * @param unusedStringFile The path to the unused string file.
     */
    public void setUnusedStringFile(String unusedStringFile) {
        this.unusedStringFile = unusedStringFile;
    }

    /**
     * Gets the list of filters for files.
     *
     * @return A set of file filters.
     */
    public Set<String> getFileFilterList() {
        return fileFilterList;
    }

    /**
     * Sets the list of filters for files.
     *
     * @param fileFilterList A set of file filters to apply.
     */
    public void setFileFilterList(Set<String> fileFilterList) {
        this.fileFilterList = fileFilterList;
    }

    /**
     * Gets the whitelist of locales.
     *
     * @return A set of locale identifiers that are whitelisted.
     */
    public Set<String> getLocaleWhiteList() {
        return localeWhiteList;
    }

    /**
     * Sets the whitelist of locales.
     *
     * @param localeWhiteList A set of locale identifiers to whitelist.
     */
    public void setLocaleWhiteList(Set<String> localeWhiteList) {
        this.localeWhiteList = localeWhiteList;
    }

    /**
     * Gets the whitelist of resources.
     *
     * @return A set of resource names that are whitelisted.
     */
    public Set<String> getWhiteList() {
        return whiteList;
    }

    /**
     * Sets the whitelist of resources.
     *
     * @param whiteList A set of resource names to whitelist.
     */
    public void setWhiteList(Set<String> whiteList) {
        this.whiteList = whiteList;
    }

    /**
     * Provides a formatted string representation of the configuration options.
     *
     * @return A formatted string containing the configuration details.
     */
    @Override
    public String toString() {
        return "-------------- Extension --------------\n" +
                "\tenableObfuscation=" + enableObfuscation + "\n" +
                "\tobfuscationMode=" + obfuscationMode + "\n" +
                "\tenableFileFiltering=" + enableFileFiltering + "\n" +
                "\tenableFilterStrings=" + enableFilterStrings + "\n" +
                "\tmergeDuplicateResources=" + mergeDuplicateResources + "\n" +
                "\tmappingFile=" + mappingFile + "\n" +
                "\tobfuscatedBundleName=" + obfuscatedBundleName + "\n" +
                "\tunusedStringFile=" + unusedStringFile + "\n" +
                "\tfileFilterList=" + fileFilterList + "\n" +
                "\tlocaleWhiteList=" + localeWhiteList + "\n" +
                "\twhiteList=" + whiteList + "\n";
    }
}
