package io.github.goldfish07.reschiper.plugin.command.model;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents a command responsible for filtering and processing string resources in an App Bundle.
 * This class is immutable and uses the AutoValue library for code generation.
 */
@AutoValue
public abstract class StringFilterCommand {

    /**
     * Creates a new {@link Builder} instance to construct a {@link StringFilterCommand}.
     *
     * @return A new {@link Builder} instance.
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new AutoValue_StringFilterCommand.Builder();
    }

    /**
     * Get an optional path to the configuration file for string filtering.
     *
     * @return An optional path to the configuration file for string filtering.
     */
    public abstract Optional<Path> getConfigPath();

    /**
     * Builder pattern for constructing {@link StringFilterCommand} instances.
     */
    @AutoValue.Builder
    public abstract static class Builder {

        /**
         * Set the path to the configuration file for string filtering.
         *
         * @param configPath The path to the configuration file.
         * @return This builder instance for method chaining.
         */
        public abstract Builder setConfigPath(Path configPath);

        /**
         * Build a new {@link StringFilterCommand} instance with the configured properties.
         *
         * @return A new {@link StringFilterCommand} instance.
         */
        public abstract StringFilterCommand build();
    }
}
