package io.github.goldfish07.reschiper.plugin.command.model;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A command to configure merging of duplicated resources in the bundle.
 */
@AutoValue
public abstract class DuplicateResMergerCommand {

    /**
     * Create a new builder for configuring the DuplicateResMergerCommand.
     *
     * @return A new instance of the DuplicateResMergerCommand.Builder.
     */
    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new AutoValue_DuplicateResMergerCommand.Builder();
    }

    /**
     * Get the flag indicating whether to disable signing during resource merging.
     *
     * @return An optional boolean flag, which, if present, specifies whether signing should be disabled during resource merging.
     */
    public abstract Optional<Boolean> getDisableSign();

    /**
     * A builder for configuring the DuplicateResMergerCommand.
     */
    @AutoValue.Builder
    public abstract static class Builder {

        /**
         * Set the flag to disable signing during resource merging.
         *
         * @param disableSign If true, signing during resource merging will be disabled.
         * @return The builder instance for method chaining.
         */
        public abstract Builder setDisableSign(Boolean disableSign);

        /**
         * Build the DuplicateResMergerCommand instance with the configured options.
         *
         * @return The configured DuplicateResMergerCommand instance.
         */
        public abstract DuplicateResMergerCommand build();
    }
}