package io.github.goldfish07.reschiper.plugin.command.model;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a command responsible for obfuscating resources in an App Bundle.
 * This class is immutable and uses the AutoValue library for code generation.
 */
@AutoValue
public abstract class ObfuscateBundleCommand {

    /**
     * Creates a new {@link Builder} instance to construct an {@link ObfuscateBundleCommand}.
     *
     * @return A new {@link Builder} instance.
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new AutoValue_ObfuscateBundleCommand.Builder();
    }

    /**
     * Get the flag indicating whether obfuscation is enabled.
     *
     * @return A boolean flag indicating whether obfuscation is enabled.
     */
    public abstract Boolean getEnableObfuscate();

    /**
     * Get the resource obfuscation mode.
     *
     * @return A resource obfuscation mode, Mode are [dir, file, default].
     */
    public abstract String getObfuscationMode();

    /**
     * Get an optional path to the obfuscation mapping file.
     *
     * @return An optional path to the obfuscation mapping file.
     */
    public abstract Optional<Path> getMappingPath();

    /**
     * Get an optional flag indicating whether duplicated resources should be merged.
     *
     * @return An optional flag indicating whether duplicated resources should be merged.
     */
    public abstract Optional<Boolean> getMergeDuplicatedResources();

    /**
     * Get an optional flag indicating whether resource signing is disabled.
     *
     * @return An optional flag indicating whether resource signing is disabled.
     */
    public abstract Optional<Boolean> getDisableSign();

    /**
     * Get the set of white-listed resources that should not be obfuscated.
     *
     * @return The set of white-listed resources.
     */
    public abstract Set<String> getWhiteList();

    /**
     * Get an optional set of file filtering rules.
     *
     * @return An optional set of file filtering rules.
     */
    public abstract Optional<Set<String>> getFileFilterRules();

    /**
     * Get an optional flag indicating whether file filtering is enabled.
     *
     * @return An optional flag indicating whether file filtering is enabled.
     */
    public abstract Optional<Boolean> getFilterFile();

    /**
     * Get an optional flag indicating whether string removal is enabled.
     *
     * @return An optional flag indicating whether string removal is enabled.
     */
    public abstract Optional<Boolean> getRemoveStr();

    /**
     * Get an optional path to the unused string resources file.
     *
     * @return An optional path to the unused string resources file.
     */
    public abstract Optional<String> getUnusedStrPath();

    /**
     * Get an optional set of language white-lists for string filtering.
     *
     * @return An optional set of language white-lists.
     */
    public abstract Optional<Set<String>> getLanguageWhiteList();

    /**
     * Builder pattern for constructing {@link ObfuscateBundleCommand} instances.
     */
    @AutoValue.Builder
    public abstract static class Builder {

        /**
         * Set the flag indicating whether obfuscation is enabled.
         *
         * @param enable A boolean flag indicating whether obfuscation is enabled.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setEnableObfuscate(Boolean enable);

        /**
         * Set the resource obfuscation mode.
         *
         * @param mode flag indicating to toggle resource obfuscation mode [dir, file, default].
         * @return This builder instance for method chaining.
         */
        public abstract Builder setObfuscationMode(String mode);

        /**
         * Set the set of white-listed resources that should not be obfuscated.
         *
         * @param whiteList The set of white-listed resources.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setWhiteList(Set<String> whiteList);

        /**
         * Set the flag indicating whether string removal is enabled.
         *
         * @param removeStr A boolean flag indicating whether string removal is enabled.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setRemoveStr(Boolean removeStr);

        /**
         * Set the path to the unused string resources file.
         *
         * @param unusedStrPath The path to the unused string resources file.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setUnusedStrPath(String unusedStrPath);

        /**
         * Set the set of language white-lists for string filtering.
         *
         * @param languageWhiteList The set of language white-lists.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setLanguageWhiteList(Set<String> languageWhiteList);

        /**
         * Set the flag indicating whether file filtering is enabled.
         *
         * @param filterFile A boolean flag indicating whether file filtering is enabled.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setFilterFile(Boolean filterFile);

        /**
         * Set the set of file filtering rules.
         *
         * @param fileFilterRules The set of file filtering rules.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setFileFilterRules(Set<String> fileFilterRules);

        /**
         * Set the path to the obfuscation mapping file.
         *
         * @param mappingPath The path to the obfuscation mapping file.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setMappingPath(Path mappingPath);

        /**
         * Set the flag indicating whether duplicated resources should be merged.
         *
         * @param mergeDuplicatedResources A boolean flag indicating whether duplicated resources should be merged.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setMergeDuplicatedResources(Boolean mergeDuplicatedResources);

        /**
         * Set the flag indicating whether resource signing is disabled.
         *
         * @param disableSign A boolean flag indicating whether resource signing is disabled.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setDisableSign(Boolean disableSign);

        /**
         * Build a new {@link ObfuscateBundleCommand} instance with the configured properties.
         *
         * @return A new {@link ObfuscateBundleCommand} instance.
         */
        public abstract ObfuscateBundleCommand build();
    }
}
