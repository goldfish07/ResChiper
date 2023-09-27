package io.github.goldfish07.reschiper.plugin.operations;

import com.android.aapt.Resources;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for operations on Android resource tables.
 */
public class ResourceTableOperation {

    /**
     * Replaces the entry path in a ConfigValue.
     *
     * @param configValue The ConfigValue to update.
     * @param path        The new path to set.
     * @return The updated ConfigValue.
     */
    public static Resources.@NotNull ConfigValue replaceEntryPath(Resources.@NotNull ConfigValue configValue, String path) {
        Resources.ConfigValue.Builder entryBuilder = configValue.toBuilder();
        entryBuilder.setValue(
                configValue.getValue().toBuilder().setItem(
                        configValue.getValue().getItem().toBuilder().setFile(
                                configValue.getValue().getItem().getFile().toBuilder().setPath(path).build()
                        ).build()
                ).build()
        );
        return entryBuilder.build();
    }

    /**
     * Updates the configuration values list for an Entry.
     *
     * @param entry           The Entry to update.
     * @param configValueList The new list of ConfigValues.
     * @return The updated Entry.
     */
    public static Resources.@NotNull Entry updateEntryConfigValueList(Resources.@NotNull Entry entry, List<Resources.ConfigValue> configValueList) {
        Resources.Entry.Builder entryBuilder = entry.toBuilder();
        entryBuilder.clearConfigValue();
        entryBuilder.addAllConfigValue(configValueList);
        return entryBuilder.build();
    }

    /**
     * Updates the name of an Entry.
     *
     * @param entry The Entry to update.
     * @param name  The new name to set.
     * @return The updated Entry.
     */
    public static Resources.@NotNull Entry updateEntryName(Resources.@NotNull Entry entry, String name) {
        Resources.Entry.Builder builder = entry.toBuilder();
        builder.setName(name);
        return builder.build();
    }

    /**
     * Checks for duplicate configurations in an Entry.
     *
     * @param entry The Entry to check.
     * @throws IllegalArgumentException if duplicate configurations are found.
     */
    public static void checkConfiguration(Resources.@NotNull Entry entry) {
        if (entry.getConfigValueCount() == 0)
            return;
        Set<Resources.ConfigValue> configValues = new HashSet<>();
        for (Resources.ConfigValue configValue : entry.getConfigValueList()) {
            if (configValues.contains(configValue))
                throw new IllegalArgumentException("Duplicate configuration for entry: " + entry.getName());
            configValues.add(configValue);
        }
    }
}