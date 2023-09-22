package io.github.goldfish07.reschiper.plugin.obfuscation;

import com.android.aapt.Resources;
import com.android.tools.build.bundletool.model.*;
import com.android.tools.build.bundletool.model.utils.ResourcesUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import io.github.goldfish07.reschiper.plugin.bundle.AppBundleUtils;
import io.github.goldfish07.reschiper.plugin.bundle.ResourceMapping;
import io.github.goldfish07.reschiper.plugin.bundle.ResourceTableBuilder;
import io.github.goldfish07.reschiper.plugin.operations.FileOperation;
import io.github.goldfish07.reschiper.plugin.operations.ResourceTableOperation;
import io.github.goldfish07.reschiper.plugin.parser.ResourcesMappingParser;
import io.github.goldfish07.reschiper.plugin.utils.TimeClock;
import io.github.goldfish07.reschiper.plugin.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

/**
 * The `ResourcesObfuscator` class is responsible for obfuscating resources in an Android AppBundle.
 * It performs resource obfuscation based on provided mapping rules, while also allowing for whitelisting of resources.
 */
public class ResourcesObfuscator {
    public static final String RESOURCE_ANDROID_PREFIX = "android:";
    public static final String FILE_MAPPING_NAME = "resources-mapping.txt";
    private static final Logger logger = Logger.getLogger(ResourcesObfuscator.class.getName());

    private final AppBundle rawAppBundle;
    private final Set<String> whiteListRules;
    private final Path outputMappingPath;
    private final ZipFile bundleZipFile;
    private final ResourceMapping resourceMapping;

    /**
     * Constructs a `ResourcesObfuscator` with the specified parameters.
     *
     * @param bundlePath           The path to the AppBundle bundle.
     * @param rawAppBundle         The raw AppBundle.
     * @param whiteListRules       The set of whitelisting rules for resources.
     * @param outputLogLocationDir The directory where log files will be generated.
     * @param mappingPath          The path to the resource mapping file (can be null).
     * @throws IOException If an I/O error occurs during initialization.
     */
    public ResourcesObfuscator(Path bundlePath, AppBundle rawAppBundle, Set<String> whiteListRules, Path outputLogLocationDir, Path mappingPath) throws IOException {
        if (mappingPath != null && mappingPath.toFile().exists()) {
            resourceMapping = new ResourcesMappingParser(mappingPath).parse();
        } else {
            resourceMapping = new ResourceMapping();
        }

        this.bundleZipFile = new ZipFile(bundlePath.toFile());
        outputMappingPath = new File(outputLogLocationDir.toFile(), FILE_MAPPING_NAME).toPath();
        if (Files.exists(outputMappingPath)) {
            logger.warning(" Mapping File Cleanup:\n" +
                           "- Deleted existing mapping file: " + outputMappingPath);
            Files.delete(outputMappingPath);
        }

        this.rawAppBundle = rawAppBundle;
        this.whiteListRules = whiteListRules;

    }

    /**
     * Obfuscates resources in the AppBundle based on the provided mapping rules and whitelisting.
     *
     * @return The obfuscated AppBundle.
     * @throws IOException If an I/O error occurs during obfuscation.
     */
    public AppBundle obfuscate() throws IOException {
        System.out.println("""
                ----------------------------------------
                 Resource Obfuscation:
                ----------------------------------------
                - Obfuscating resources...""");

        TimeClock timeClock = new TimeClock();

        checkResMappingRules();
        Map<BundleModuleName, BundleModule> obfuscatedModules = new HashMap<>();
        // generate type entry mapping from mapping rule
        Map<String, Set<String>> typeEntryMapping = generateObfuscatedEntryFilesFromMapping();

        for (Map.Entry<BundleModuleName, BundleModule> entry : rawAppBundle.getModules().entrySet()) {
            BundleModule bundleModule = entry.getValue();
            BundleModuleName bundleModuleName = entry.getKey();
            // generate obfuscation resources mapping
            generateResourceMappingRule(bundleModule, typeEntryMapping);
            // obfuscate module entries
            Map<String, String> obfuscateModuleEntriesMap = obfuscateModuleEntries(bundleModule, typeEntryMapping);
            // obfuscate bundle module
            BundleModule obfuscatedModule = obfuscateBundleModule(bundleModule, obfuscateModuleEntriesMap);
            obfuscatedModules.put(bundleModuleName, obfuscatedModule);
        }

        AppBundle appBundle = rawAppBundle.toBuilder().setModules(ImmutableMap.copyOf(obfuscatedModules)).build();
        System.out.printf("- Obfuscation completed in %s%n\n", timeClock.getElapsedTime());
        // write mapping rules to file.
        resourceMapping.writeMappingToFile(outputMappingPath);
        return appBundle;

    }

    /**
     * Generates a mapping of obfuscated entry files based on the provided resource mapping.
     * This method constructs a mapping of obfuscated entry file paths using the resource mapping,
     * which specifies how resources should be obfuscated.
     *
     * @return A map where keys are original entry paths, and values are their corresponding obfuscated paths.
     */
    private @NotNull Map<String, Set<String>> generateObfuscatedEntryFilesFromMapping() {
        Map<String, Set<String>> typeEntryMapping = new HashMap<>();
        // generate an obfuscated entry path from incremental mapping
        for (String path : resourceMapping.getEntryFilesMapping().values()) {
            String parentPath = FileOperation.getParentFromZipFilePath(path);
            String name = FileOperation.getFilePrefixByFileName(FileOperation.getNameFromZipFilePath(path));
            Set<String> entryList = typeEntryMapping.get(parentPath);
            if (entryList == null) entryList = new HashSet<>();
            entryList.add(name);
            typeEntryMapping.put(parentPath, entryList);
        }
        // generate obfuscated entry name from incremental mapping
        for (String entry : resourceMapping.getResourceMapping().values()) {
            String name = AppBundleUtils.getEntryNameByResourceName(entry);
            String type = AppBundleUtils.getTypeNameByResourceName(entry);
            Set<String> entryList = typeEntryMapping.get(type);
            if (entryList == null) entryList = new HashSet<>();
            entryList.add(name);
            typeEntryMapping.put(type, entryList);
        }
        return typeEntryMapping;
    }

    /**
     * Reads resourceTable and generates obfuscation mapping based on the provided typeEntryMapping.
     *
     * @param bundleModule     The bundle module for which to generate obfuscation mapping.
     * @param typeEntryMapping A map of resource types to their corresponding obfuscated entry names.
     */
    private void generateResourceMappingRule(@NotNull BundleModule bundleModule, Map<String, Set<String>> typeEntryMapping) {
        if (bundleModule.getResourceTable().isEmpty()) {
            return;
        }
        StringObfuscator stringObfuscator = new StringObfuscator();
        stringObfuscator.reset(null);

        Resources.ResourceTable table = bundleModule.getResourceTable().get();
        // generate resource directory mapping
        ResourcesUtils.getAllFileReferences(table)
                .stream()
                .map(ZipPath::getParent)
                .filter(Objects::nonNull)
                .filter(path -> !resourceMapping.getDirMapping().containsKey(path.toString()))
                .forEach(path -> {
                    stringObfuscator.reset(null);
                    String name = stringObfuscator.getReplaceString(resourceMapping.getPathMappingNameList());
                    resourceMapping.putDirMapping(path.toString(), BundleModule.RESOURCES_DIRECTORY + "/" + name);
                });
        AtomicBoolean whiteListEnabled = new AtomicBoolean(true);
        // generate resource mapping
        ResourcesUtils.entries(table).forEach(entry -> {
            String resourceId = entry.getResourceId().toString();
            String resourceName = AppBundleUtils.getResourceFullName(entry);
            Set<String> obfuscationList = typeEntryMapping.get(entry.getType().getName());
            if (obfuscationList == null) {
                obfuscationList = new HashSet<>();
            }
            if (whiteListEnabled.get() && shouldBeObfuscated(resourceName)){
                System.out.println("- Found whiteList resources:");
                whiteListEnabled.set(false);
            }

            stringObfuscator.reset(null);
            if (resourceMapping.getResourceMapping().containsKey(resourceName)) {
                if (shouldBeObfuscated(resourceName)) {
                    System.out.printf(" removing from mapping: %s, id: %s%n", resourceName, resourceId);
                    resourceMapping.getResourceMapping().remove(resourceName);
                } else {
                    String obfuscateResourceName = resourceMapping.getResourceMapping().get(resourceName);
                    obfuscationList.add(AppBundleUtils.getEntryNameByResourceName(obfuscateResourceName));
                }
            } else {
                if (shouldBeObfuscated(resourceName)) {
                    System.out.printf(" - %s, id: %s%n", resourceName, resourceId);
                } else {
                    String name = stringObfuscator.getReplaceString(obfuscationList);
                    obfuscationList.add(name);
                    String obfuscatedResourceName = AppBundleUtils.getResourceFullName(entry.getPackage().getPackageName(), entry.getType().getName(), name);
                    resourceMapping.putResourceMapping(resourceName, obfuscatedResourceName);
                }
            }
            typeEntryMapping.put(entry.getType().getName(), obfuscationList);
        });
    }

    /**
     * Obfuscates module entries and returns the mapping rules.
     *
     * @param bundleModule   The bundle module to obfuscate entries for.
     * @param typeMappingMap A map of resource types to their corresponding obfuscated entry names.
     * @return A map of obfuscated entry paths.
     */
    private @NotNull Map<String, String> obfuscateModuleEntries(@NotNull BundleModule bundleModule, Map<String, Set<String>> typeMappingMap) {
        StringObfuscator guardStringBuilder = new StringObfuscator();
        guardStringBuilder.reset(null);
        Map<String, String> obfuscateEntries = new HashMap<>();
        bundleModule.getEntries().stream()
                .filter(entry -> entry.getPath().startsWith(BundleModule.RESOURCES_DIRECTORY))
                .forEach(entry -> {
                    guardStringBuilder.reset(null);
                    String entryDir = entry.getPath().getParent().toString();
                    String obfuscateDir = resourceMapping.getDirMapping().get(entryDir);
                    if (obfuscateDir == null) {
                        throw new RuntimeException(String.format("can not find resource directory: %s", entryDir));
                    }
                    Set<String> mapping = typeMappingMap.get(obfuscateDir);
                    if (mapping == null) {
                        mapping = new HashSet<>();
                    }

                    String bundleRawPath = bundleModule.getName().getName() + "/" + entry.getPath().toString();
                    String bundleObfuscatedPath = resourceMapping.getEntryFilesMapping().get(bundleRawPath);
                    if (bundleObfuscatedPath == null) {
                        if (shouldBeObfuscated(bundleRawPath)) {
                            System.out.printf(" Found whiteList resource file, resource: %s%n", bundleRawPath);
                            return;
                        } else {
                            String fileSuffix = FileOperation.getFileSuffix(entry.getPath());
                            String obfuscatedName = guardStringBuilder.getReplaceString(mapping);
                            mapping.add(obfuscatedName);
                            bundleObfuscatedPath = obfuscateDir + "/" + obfuscatedName + fileSuffix;
                            resourceMapping.putEntryFileMapping(bundleRawPath, bundleObfuscatedPath);
                        }
                    }
                    if (obfuscateEntries.containsValue(bundleObfuscatedPath)) {
                        throw new IllegalArgumentException(
                                String.format("Multiple entries with same key: %s -> %s", bundleRawPath, bundleObfuscatedPath));
                    }
                    obfuscateEntries.put(bundleRawPath, bundleObfuscatedPath);
                    typeMappingMap.put(obfuscateDir, mapping);
                });
        return obfuscateEntries;
    }

    /**
     * Obfuscates a bundle module by applying obfuscation rules to its entries and resourceTable.
     *
     * @param bundleModule       The bundle module to obfuscate.
     * @param obfuscatedEntryMap A map of obfuscated entry paths.
     * @return The obfuscated bundle module.
     * @throws IOException If an I/O error occurs during obfuscation.
     */
    private BundleModule obfuscateBundleModule(@NotNull BundleModule bundleModule, Map<String, String> obfuscatedEntryMap) throws IOException {
        BundleModule.Builder builder = bundleModule.toBuilder();

        // obfuscate module entries
        List<ModuleEntry> obfuscateEntries = new ArrayList<>();
        for (ModuleEntry entry : bundleModule.getEntries()) {
            String bundleRawPath = bundleModule.getName().getName() + "/" + entry.getPath().toString();
            String obfuscatedPath = obfuscatedEntryMap.get(bundleRawPath);
            if (obfuscatedPath != null) {
                ModuleEntry obfuscatedEntry = ModuleEntry.builder().setPath(ZipPath.create(obfuscatedPath))
                        .setContent(ByteSource.wrap(AppBundleUtils.readByte(bundleZipFile, entry, bundleModule))).build();
                obfuscateEntries.add(obfuscatedEntry);
            } else {
                obfuscateEntries.add(entry);
            }
        }
        builder.setRawEntries(obfuscateEntries);

        // obfuscate resourceTable
        Resources.ResourceTable obfuscatedResTable = obfuscateResourceTable(bundleModule, obfuscatedEntryMap);
        if (obfuscatedResTable != null) {
            builder.setResourceTable(obfuscatedResTable);
        }
        return builder.build();
    }

    /**
     * Obfuscates the resourceTable of a bundle module based on the obfuscated entry mapping.
     *
     * @param bundleModule       The bundle module whose resourceTable is to be obfuscated.
     * @param obfuscatedEntryMap A map of obfuscated entry paths.
     * @return The obfuscated resourceTable.
     */
    private Resources.@Nullable ResourceTable obfuscateResourceTable(@NotNull BundleModule bundleModule, Map<String, String> obfuscatedEntryMap) {
        if (bundleModule.getResourceTable().isEmpty()) {
            return null;
        }
        Resources.ResourceTable resourceTable = bundleModule.getResourceTable().get();

        ResourceTableBuilder resourceTableBuilder = new ResourceTableBuilder();
        ResourcesUtils.entries(resourceTable).map(entry -> {
            String resourceName = AppBundleUtils.getResourceFullName(entry);
            String resourceId = entry.getResourceId().toString();
            String obfuscatedResName = resourceMapping.getResourceMapping().get(resourceName);
            resourceMapping.addResourceNameAndId(resourceName, resourceId);

            Resources.Entry obfuscatedEntry = entry.getEntry();
            if (obfuscatedResName != null) {
                // update entry name
                String entryName = AppBundleUtils.getEntryNameByResourceName(obfuscatedResName);
                obfuscatedEntry = ResourceTableOperation.updateEntryName(obfuscatedEntry, entryName);
            }

            // update config values
            List<Resources.ConfigValue> configValues = Stream.of(obfuscatedEntry)
                    .map(Resources.Entry::getConfigValueList)
                    .flatMap(Collection::stream)
                    .map(configValue -> {
                        if (!configValue.getValue().getItem().hasFile()) {
                            return configValue;
                        }
                        String rawPath = configValue.getValue().getItem().getFile().getPath();
                        String bundleRawPath = bundleModule.getName().getName() + "/" + rawPath;
                        String obfuscatedPath = obfuscatedEntryMap.get(bundleRawPath);
                        if (obfuscatedPath != null) {
                            resourceMapping.addResourcePathAndId(bundleRawPath, resourceId);
                            resourceMapping.putEntryFileMapping(bundleRawPath, obfuscatedPath);
                            return ResourceTableOperation.replaceEntryPath(configValue, obfuscatedPath);
                        }
                        return configValue;
                    })
                    .collect(Collectors.toList());
            if (!configValues.isEmpty()) {
                obfuscatedEntry = ResourceTableOperation.updateEntryConfigValueList(obfuscatedEntry, configValues);
            }

            return ResourceTableEntry.create(entry.getPackage(), entry.getType(), obfuscatedEntry);
        }).forEach(entry -> {
            ResourceTableOperation.checkConfiguration(entry.getEntry());
            resourceTableBuilder.addPackage(entry.getPackage()).addResource(entry.getType(), entry.getEntry());
        });

        return resourceTableBuilder.build();
    }

    /**
     * Validates the resource mapping rules by ensuring that mapped directories are valid.
     */
    private void checkResMappingRules() {
        resourceMapping.getDirMapping().values().stream()
                .map(ZipPath::create)
                .forEach(path -> {
                    if (!path.startsWith(BundleModule.RESOURCES_DIRECTORY)) {
                        throw new IllegalArgumentException(String.format(
                                "Module files can be only in pre-defined directories, the mapping obfuscation rule is %s", path));
                    }
                });
    }

    /**
     * Checks whether a resource should be obfuscated based on whitelist rules.
     *
     * @param resourceName The name of the resource to check.
     * @return `true` if the resource should be obfuscated, `false` if it should be whitelisted.
     */
    private boolean shouldBeObfuscated(@NotNull String resourceName) {
        // android system resources should not be obfuscated
        if (resourceName.startsWith(RESOURCE_ANDROID_PREFIX)) {
            return true;
        }
        for (String rule : whiteListRules) {
            Pattern filterPattern = Pattern.compile(Utils.convertToPatternString(rule));
            if (filterPattern.matcher(resourceName).matches()) {
                return true;
            }
        }
        return false;
    }
}
