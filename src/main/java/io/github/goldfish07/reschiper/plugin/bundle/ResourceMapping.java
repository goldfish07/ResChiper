package io.github.goldfish07.reschiper.plugin.bundle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The ResourceMapping class represents a mapping of resources, directories, and entry files
 * used for obfuscating resource-related data within Android apps.
 * It provides methods to manage and write these mapping rules to a file.
 */
public class ResourceMapping {

    private final Map<String, String> dirMapping = new HashMap<>();
    private final Map<String, String> resourceMapping = new HashMap<>();
    private final Map<String, String> entryFilesMapping = new HashMap<>();
    private final Map<String, String> resourceNameToIdMapping = new HashMap<>();
    private final Map<String, String> resourcePathToIdMapping = new HashMap<>();

    /**
     * Constructs an empty ResourceMapping object.
     */
    public ResourceMapping() {
    }

    /**
     * Extracts the simple name from a resource path.
     *
     * @param resourceName The resource name containing the path.
     * @return The simple name of the resource.
     */
    @Contract(pure = true)
    public static String getResourceSimpleName(@NotNull String resourceName) {
        String[] values = resourceName.split("/");
        return values[values.length - 1];
    }

    /**
     * Gets the directory mapping.
     *
     * @return A map of raw directory paths to obfuscate directory paths.
     */
    public Map<String, String> getDirMapping() {
        return dirMapping;
    }

    /**
     * Gets the resource mapping.
     *
     * @return A map of raw resource names to obfuscate resource names.
     */
    public Map<String, String> getResourceMapping() {
        return resourceMapping;
    }

    /**
     * Gets the entry files mapping.
     *
     * @return A map of raw entry file paths to obfuscated entry file paths.
     */
    public Map<String, String> getEntryFilesMapping() {
        return entryFilesMapping;
    }

    /**
     * Adds a directory mapping to the resource mapping.
     *
     * @param rawPath       The raw directory path.
     * @param obfuscatePath The obfuscated directory path.
     */
    public void putDirMapping(String rawPath, String obfuscatePath) {
        dirMapping.put(rawPath, obfuscatePath);
    }

    /**
     * Adds a resource mapping to the resource mapping.
     *
     * @param rawResource       The raw resource name.
     * @param obfuscateResource The obfuscated resource name.
     * @throws IllegalArgumentException if the obfuscateResource already exists in the mapping.
     */
    public void putResourceMapping(String rawResource, String obfuscateResource) {
        if (resourceMapping.containsValue(obfuscateResource))
            throw new IllegalArgumentException(String.format("Multiple entries: %s -> %s", rawResource, obfuscateResource));
        resourceMapping.put(rawResource, obfuscateResource);
    }

    /**
     * Adds an entry file mapping to the entry files mapping.
     *
     * @param rawPath        The raw entry file path.
     * @param obfuscatedPath The obfuscated entry file path.
     */
    public void putEntryFileMapping(String rawPath, String obfuscatedPath) {
        entryFilesMapping.put(rawPath, obfuscatedPath);
    }

    /**
     * Gets a list of simple names from the directory mapping.
     *
     * @return A list of simple names extracted from obfuscated directory paths.
     */
    public List<String> getPathMappingNameList() {
        return dirMapping.values().stream()
                .map(value -> {
                    String[] values = value.split("/");
                    if (value.isEmpty())
                        return value;
                    return values[values.length - 1];
                })
                .collect(Collectors.toList());
    }

    /**
     * Adds a resource name and its corresponding ID to the mapping.
     *
     * @param name The resource name.
     * @param id   The ID associated with the resource name.
     */
    public void addResourceNameAndId(String name, String id) {
        resourceNameToIdMapping.put(name, id);
    }

    /**
     * Adds a resource path and its corresponding ID to the mapping.
     *
     * @param path The resource path.
     * @param id   The ID associated with the resource path.
     */
    public void addResourcePathAndId(String path, String id) {
        resourcePathToIdMapping.put(path, id);
    }

    /**
     * Writes the mapping rules to a file at the specified path.
     *
     * @param mappingPath The path to the mapping file.
     * @throws IOException If there is an issue with file I/O.
     */
    public void writeMappingToFile(@NotNull Path mappingPath) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(mappingPath.toFile(), false));
        // Write resource directory mapping
        writer.write("res dir mapping:\n");
        for (Map.Entry<String, String> entry : dirMapping.entrySet())
            writer.write(String.format("\t%s -> %s\n", entry.getKey(), entry.getValue()));
        writer.write("\n\n");
        writer.flush();
        // Write resource ID mapping
        writer.write("res id mapping:\n");
        for (Map.Entry<String, String> entry : resourceMapping.entrySet())
            writer.write(String.format(
                    "\t%s : %s -> %s\n",
                    resourceNameToIdMapping.get(entry.getKey()),
                    entry.getKey(),
                    entry.getValue()
            ));
        writer.write("\n\n");
        writer.flush();
        // Write resource entries path mapping
        writer.write("res entries path mapping:\n");
        for (Map.Entry<String, String> entry : entryFilesMapping.entrySet())
            writer.write(String.format(
                    "\t%s : %s -> %s\n",
                    resourcePathToIdMapping.get(entry.getKey()),
                    entry.getKey(),
                    entry.getValue()
            ));
        writer.write("\n\n");
        writer.flush();
        writer.close();
    }
}