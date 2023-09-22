package io.github.goldfish07.reschiper.plugin.parser.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * The `FileFilterConfig` class represents a configuration for filtering files based on a set of rules.
 * It allows specifying whether the filter is active and maintains a set of rules for file filtering.
 */
public class FileFilterConfig {
    private final Set<String> rules = new HashSet<>();
    private boolean isActive;

    /**
     * Checks if the file filter is active.
     *
     * @return `true` if the filter is active, `false` otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the activity status of the file filter.
     *
     * @param active `true` to activate the filter, `false` to deactivate it.
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Gets the set of rules used for file filtering.
     *
     * @return The set of rules for file filtering.
     */
    public Set<String> getRules() {
        return rules;
    }

    /**
     * Adds a rule to the set of rules for file filtering.
     *
     * @param rule The rule to add.
     */
    public void addRule(String rule) {
        rules.add(rule);
    }
}