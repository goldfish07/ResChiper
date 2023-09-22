package io.github.goldfish07.reschiper.plugin.command.model;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * Represents a configuration command for file filtering in the ResChiper tool.
 * This class is immutable and uses the AutoValue library for code generation.
 */
@AutoValue
public abstract class FileFilterCommand {

    /**
     * Creates a new {@link Builder} instance to construct a {@link FileFilterCommand}.
     *
     * @return A new {@link Builder} instance.
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new AutoValue_FileFilterCommand.Builder();
    }

    /**
     * Get the set of file filtering rules.
     *
     * @return The set of file filtering rules.
     */
    public abstract Set<String> getFileFilterRules();

    /**
     * Get an optional flag indicating whether file signing is disabled.
     *
     * @return An optional flag indicating whether file signing is disabled.
     */
    public abstract Optional<Boolean> getDisableSign();

    /**
     * Builder pattern for constructing {@link FileFilterCommand} instances.
     */
    @AutoValue.Builder
    public abstract static class Builder {

        /**
         * Set the file filtering rules for the command.
         *
         * @param fileFilterRules The set of file filtering rules.
         * @return This builder for method chaining.
         */
        public abstract Builder setFileFilterRules(Set<String> fileFilterRules);

        /**
         * Set the flag indicating whether file signing should be disabled for the command.
         *
         * @param disableSign An optional flag indicating whether file signing is disabled.
         * @return This builder for method chaining.
         */
        public abstract Builder setDisableSign(Boolean disableSign);

        /**
         * Build a new {@link FileFilterCommand} instance with the configured properties.
         *
         * @return A new {@link FileFilterCommand} instance.
         */
        public abstract FileFilterCommand build();
    }
}