package io.github.goldfish07.reschiper.plugin.parser.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * The `StringFilterConfig` class represents the configuration for filtering strings in resources.
 * It allows specifying whether the string filter is active, a custom path, and a whitelist of languages.
 */
public class StringFilterConfig {
    private final Set<String> languageWhiteList = new HashSet<>();
    private boolean isActive;
    private String path = "";

    /**
     * Checks if the string filter is active.
     *
     * @return `true` if the filter is active, `false` otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the activity status of the string filter.
     *
     * @param active `true` to activate the filter, `false` to deactivate it.
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Gets the custom path used for filtering strings.
     *
     * @return The custom path for string filtering.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the custom path for filtering strings.
     *
     * @param path The custom path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the whitelist of languages used for string filtering.
     *
     * @return The set of whitelisted languages for string filtering.
     */
    public Set<String> getLanguageWhiteList() {
        return languageWhiteList;
    }

    /**
     * Returns a string representation of the `StringFilterConfig` object.
     *
     * @return A string representation of the object's properties.
     */
    @Override
    public String toString() {
        return "StringFilterConfig{" +
               "isActive=" + isActive +
               ", path='" + path + '\'' +
               ", languageWhiteList=" + languageWhiteList +
               '}';
    }
}