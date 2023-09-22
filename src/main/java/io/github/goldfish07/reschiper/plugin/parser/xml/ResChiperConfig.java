package io.github.goldfish07.reschiper.plugin.parser.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * The `ResChiperConfig` class represents the configuration for the Resource Chiper tool.
 * It includes settings related to file filtering, string filtering, and a whitelist of rules.
 */
public class ResChiperConfig {
    private final Set<String> whiteList = new HashSet<>();
    private FileFilterConfig fileFilter;
    private StringFilterConfig stringFilterConfig;
    private boolean useWhiteList;

    /**
     * Gets the file filter configuration for the Resource Chiper tool.
     *
     * @return The file filter configuration.
     */
    public FileFilterConfig getFileFilter() {
        return fileFilter;
    }

    /**
     * Sets the file filter configuration for the Resource Chiper tool.
     *
     * @param fileFilter The file filter configuration to set.
     */
    public void setFileFilter(FileFilterConfig fileFilter) {
        this.fileFilter = fileFilter;
    }

    /**
     * Checks if the whitelist is active for the Resource Chiper tool.
     *
     * @return `true` if the whitelist is active, `false` otherwise.
     */
    public boolean isUseWhiteList() {
        return useWhiteList;
    }

    /**
     * Sets the whitelist activity status for the Resource Chiper tool.
     *
     * @param useWhiteList `true` to activate the whitelist, `false` to deactivate it.
     */
    public void setUseWhiteList(boolean useWhiteList) {
        this.useWhiteList = useWhiteList;
    }

    /**
     * Gets the whitelist of rules used for resource filtering.
     *
     * @return The set of whitelist rules.
     */
    public Set<String> getWhiteList() {
        return whiteList;
    }

    /**
     * Adds a rule to the whitelist for resource filtering.
     *
     * @param whiteRule The rule to add to the whitelist.
     */
    public void addWhiteList(String whiteRule) {
        this.whiteList.add(whiteRule);
    }

    /**
     * Gets the string filter configuration for the Resource Chiper tool.
     *
     * @return The string filter configuration.
     */
    public StringFilterConfig getStringFilterConfig() {
        return stringFilterConfig;
    }

    /**
     * Sets the string filter configuration for the Resource Chiper tool.
     *
     * @param stringFilterConfig The string filter configuration to set.
     */
    public void setStringFilterConfig(StringFilterConfig stringFilterConfig) {
        this.stringFilterConfig = stringFilterConfig;
    }
}