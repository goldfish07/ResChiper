package io.github.goldfish07.reschiper.plugin.command.extensions;

import com.android.aapt.Resources;
import com.android.tools.build.bundletool.model.*;
import com.android.tools.build.bundletool.model.utils.ResourcesUtils;
import io.github.goldfish07.reschiper.plugin.bundle.AppBundleUtils;
import io.github.goldfish07.reschiper.plugin.bundle.ResourceTableBuilder;
import io.github.goldfish07.reschiper.plugin.operations.ResourceTableOperation;
import io.github.goldfish07.reschiper.plugin.operations.FileOperation;
import io.github.goldfish07.reschiper.plugin.utils.TimeClock;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileDoesNotExist;
import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * The `DuplicateResourceMerger` class is responsible for merging and removing duplicated resources
 * in an Android App Bundle (AAB). It identifies duplicated resources by their MD5 hash values and
 * merges them to reduce the size of the bundle.
 * <p>
 * This class processes each module in the App Bundle, identifies duplicated resources, and generates
 * a log file with information about the merged resources and their original paths.
 */
public class DuplicateResourceMerger {
    private static final Logger logger = Logger.getLogger(DuplicateResourceMerger.class.getName());
    public static final String DUPLICATE_LOGGER_FILE_SUFFIX = "-duplicate.txt";
    private final Path outputLogLocationDir;
    private final ZipFile bundleZipFile;
    private final AppBundle rawAppBundle;
    private final Map<String, ZipPath> md5FileList = new HashMap<>();
    private final Map<ZipPath, String> duplicatedFileList = new HashMap<>();
    private int mergeDuplicatedTotalSize = 0;
    private int mergeDuplicatedTotalCount = 0;

    /**
     * Constructs a `DuplicateResourceMerger` instance with the provided parameters.
     *
     * @param bundlePath           The path to the input AAB file.
     * @param appBundle            The original unfiltered App Bundle.
     * @param outputLogLocationDir The directory where log files containing information about duplicated resources will be stored.
     * @throws IOException If there is an issue with reading files or bundle contents.
     */
    public DuplicateResourceMerger(Path bundlePath, AppBundle appBundle, Path outputLogLocationDir) throws IOException {
        checkFileExistsAndReadable(bundlePath);
        this.outputLogLocationDir = outputLogLocationDir;
        bundleZipFile = new ZipFile(bundlePath.toFile());
        rawAppBundle = appBundle;
    }

    /**
     * Merges duplicated resources in all modules of the App Bundle, removing duplicates based on their MD5 hash values.
     * Generates log files containing information about the merged resources and their original paths.
     *
     * @return An AppBundle with duplicated resources removed.
     * @throws IOException If there is an issue with reading files or bundle contents.
     */
    public AppBundle merge() throws IOException {
        TimeClock timeClock = new TimeClock();
        List<BundleModule> mergedBundleModuleList = new ArrayList<>();
        for (Map.Entry<BundleModuleName, BundleModule> moduleEntry : rawAppBundle.getModules().entrySet())
            mergedBundleModuleList.add(mergeBundleModule(moduleEntry.getValue()));
        AppBundle mergedAppBundle = AppBundle.buildFromModules(
                mergedBundleModuleList.stream().collect(toImmutableList()),
                rawAppBundle.getBundleConfig(),
                rawAppBundle.getBundleMetadata()
        );
        System.out.printf(
                """
                        removed duplicate resources done, took %s
                        -----------------------------------------
                         Reduce file count: %s
                         Reduce file size: %s
                        -----------------------------------------%n""",
                timeClock.getElapsedTime(), mergeDuplicatedTotalCount,
                FileOperation.getNetFileSizeDescription(mergeDuplicatedTotalSize)
        );
        return mergedAppBundle;
    }

    /**
     * Merges duplicated resources within a single module of the App Bundle, removing duplicates based on their MD5 hash values.
     * Generates a log file containing information about the merged resources and their original paths for the module.
     *
     * @param bundleModule The bundle module to process.
     * @return A modified bundle module with duplicated resources removed.
     * @throws IOException If there is an issue with reading files or bundle contents.
     */
    private BundleModule mergeBundleModule(@NotNull BundleModule bundleModule) throws IOException {
        File logFile = new File(outputLogLocationDir.toFile(), bundleModule.getName().getName() + DUPLICATE_LOGGER_FILE_SUFFIX);
        if (Files.exists(logFile.toPath())) {
            System.out.println("Log File Cleanup:");
            logger.warning("- Deleted existing log file: " + logFile.toPath());
            Files.delete(logFile.toPath());
        }
        Resources.ResourceTable table = bundleModule.getResourceTable().orElse(Resources.ResourceTable.getDefaultInstance());
        if (table.getPackageList().isEmpty() || bundleModule.getEntries().isEmpty())
            return bundleModule;
        md5FileList.clear();
        duplicatedFileList.clear();
        List<ModuleEntry> mergedModuleEntry = new ArrayList<>();
        for (ModuleEntry entry : bundleModule.getEntries()) {
            if (!entry.getPath().startsWith(BundleModule.RESOURCES_DIRECTORY)) {
                mergedModuleEntry.add(entry);
                continue;
            }
            String md5 = AppBundleUtils.getEntryMd5(bundleZipFile, entry, bundleModule);
            if (md5FileList.containsKey(md5))
                duplicatedFileList.put(entry.getPath(), md5);
            else {
                md5FileList.put(md5, entry.getPath());
                mergedModuleEntry.add(entry);
            }
        }
        generateDuplicatedLog(logFile, bundleModule);
        Resources.ResourceTable mergedTable = mergeResourceTable(table);
        return bundleModule.toBuilder()
                .setResourceTable(mergedTable)
                .setRawEntries(mergedModuleEntry)
                .build();
    }

    /**
     * Merges the resource table of a module, removing duplicated resources based on their MD5 hash values.
     *
     * @param resourceTable The original resource table to be modified.
     * @return A modified resource table with duplicated resources removed.
     */
    private Resources.ResourceTable mergeResourceTable(Resources.ResourceTable resourceTable) {
        ResourceTableBuilder resourceTableBuilder = new ResourceTableBuilder();
        ResourcesUtils.entries(resourceTable).forEach(entry -> {
            ResourceTableBuilder.PackageBuilder packageBuilder = resourceTableBuilder.addPackage(entry.getPackage());
            // replace the duplicated path
            List<Resources.ConfigValue> configValues = getDuplicatedMergedConfigValues(entry.getEntry());
            Resources.Entry mergedEntry = ResourceTableOperation.updateEntryConfigValueList(entry.getEntry(), configValues);
            packageBuilder.addResource(entry.getType(), mergedEntry);
        });
        return resourceTableBuilder.build();
    }

    /**
     * Modifies the configuration values of duplicated resources within an entry, updating file paths if necessary.
     *
     * @param entry The entry containing configuration values to be modified.
     * @return A list of modified configuration values with updated file paths for duplicated resources.
     */
    private List<Resources.ConfigValue> getDuplicatedMergedConfigValues(Resources.@NotNull Entry entry) {
        return Stream.of(entry.getConfigValueList())
                .flatMap(Collection::stream)
                .map(configValue -> {
                    if (!configValue.getValue().getItem().hasFile())
                        return configValue;
                    ZipPath zipPath = ZipPath.create(configValue.getValue().getItem().getFile().getPath());
                    if (duplicatedFileList.containsKey(zipPath))
                        zipPath = md5FileList.get(duplicatedFileList.get(zipPath));
                    return ResourceTableOperation.replaceEntryPath(configValue, zipPath.toString());
                }).collect(Collectors.toList());
    }

    /**
     * Generates a log file containing information about duplicated resources and their original paths.
     *
     * @param logFile      The file where the log information will be written.
     * @param bundleModule The bundle module containing the duplicated resources.
     * @throws IOException If there is an issue with writing the log file.
     */
    private void generateDuplicatedLog(@NotNull File logFile, BundleModule bundleModule) throws IOException {
        int duplicatedSize = 0;
        checkFileDoesNotExist(logFile.toPath());
        Writer writer = new BufferedWriter(new FileWriter(logFile, false));
        writer.write("res filter path mapping:\n");
        writer.flush();
        System.out.println("----------------------------------------");
        System.out.println(" Resource Duplication Detected:");
        System.out.println("----------------------------------------");

        for (Map.Entry<ZipPath, String> entry : duplicatedFileList.entrySet()) {
            ModuleEntry moduleEntry = bundleModule.getEntry(entry.getKey()).get();
            long fileSize = AppBundleUtils.getZipEntrySize(bundleZipFile, moduleEntry, bundleModule);
            duplicatedSize += (int) fileSize;
        }

        System.out.printf("Found duplicated resources (Count: %d, Total Size: %s):\n%n", duplicatedFileList.size(), FileOperation.getNetFileSizeDescription(duplicatedSize));
        duplicatedSize = 0;
        for (Map.Entry<ZipPath, String> entry : duplicatedFileList.entrySet()) {
            ZipPath keepPath = md5FileList.get(entry.getValue());
            ModuleEntry moduleEntry = bundleModule.getEntry(entry.getKey()).get();
            long fileSize = AppBundleUtils.getZipEntrySize(bundleZipFile, moduleEntry, bundleModule);
            duplicatedSize += (int) fileSize;
            System.out.printf("- %s (size %s)%n", entry.getKey().toString(), FileOperation.getNetFileSizeDescription(duplicatedSize));
            writer.write("\t" + entry.getKey().toString()
                    + " -> "
                    + keepPath.toString()
                    + " (size " + FileOperation.getNetFileSizeDescription(fileSize) + ")"
                    + "\n"
            );
        }
        writer.write("removed: count(" + duplicatedFileList.size() + "), totalSize(" + FileOperation.getNetFileSizeDescription(duplicatedSize) + ")");
        writer.close();
        mergeDuplicatedTotalSize += duplicatedSize;
        mergeDuplicatedTotalCount += duplicatedFileList.size();
    }
}
