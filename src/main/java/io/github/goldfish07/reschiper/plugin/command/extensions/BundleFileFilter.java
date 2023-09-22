package io.github.goldfish07.reschiper.plugin.command.extensions;

import com.android.bundle.Files;
import com.android.tools.build.bundletool.model.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.goldfish07.reschiper.plugin.bundle.AppBundleUtils;
import io.github.goldfish07.reschiper.plugin.operations.NativeLibrariesOperation;
import io.github.goldfish07.reschiper.plugin.operations.FileOperation;
import io.github.goldfish07.reschiper.plugin.utils.Utils;
import io.github.goldfish07.reschiper.plugin.utils.TimeClock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.rmi.UnexpectedException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import static com.android.tools.build.bundletool.model.AppBundle.METADATA_DIRECTORY;
import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * The `BundleFileFilter` class is responsible for filtering files and metadata within an Android App Bundle (AAB).
 * It allows users to specify rules for filtering files within the bundle and removes the specified files according to
 * the defined rules. Additionally, it filters metadata files and updates the bundle accordingly.
 */
public class BundleFileFilter {
    private static final Set<String> FILE_SIGN = new HashSet<>(
            ImmutableSet.of(
                    "META-INF/*.RSA",
                    "META-INF/*.SF",
                    "META-INF/*.MF"
            )
    );
    private final ZipFile bundleZipFile;
    private final AppBundle rawAppBundle;
    private final Set<String> filterRules;

    private int filterTotalSize = 0;
    private int filterTotalCount = 0;

    /**
     * Constructs a new `BundleFileFilter` instance.
     *
     * @param bundlePath   The path to the AAB file to filter.
     * @param rawAppBundle The raw AppBundle to be filtered.
     * @param filterRules  The set of filter rules specifying which files to exclude.
     * @throws IOException If there is an error accessing the AAB file.
     */
    public BundleFileFilter(Path bundlePath, AppBundle rawAppBundle, Set<String> filterRules) throws IOException {
        checkFileExistsAndReadable(bundlePath);
        this.bundleZipFile = new ZipFile(bundlePath.toFile());
        this.rawAppBundle = rawAppBundle;
        if (filterRules == null) {
            filterRules = new HashSet<>();
        }
        this.filterRules = filterRules;
        filterRules.addAll(FILE_SIGN);
    }

    /**
     * Filters the AppBundle based on the provided rules and returns the filtered AppBundle.
     *
     * @return The filtered AppBundle.
     * @throws IOException If there is an error during the filtering process.
     */
    public AppBundle filter() throws IOException {
        System.out.println("----------------------------------------");
        System.out.println(" Resource File Filter:");
        System.out.println("----------------------------------------");
        TimeClock timeClock = new TimeClock();
        // filter bundle module file
        Map<BundleModuleName, BundleModule> bundleModules = new HashMap<>();
        for (Map.Entry<BundleModuleName, BundleModule> entry : rawAppBundle.getModules().entrySet()) {
            bundleModules.put(entry.getKey(), filterBundleModule(entry.getValue()));
        }
        AppBundle appBundle = rawAppBundle.toBuilder()
                .setBundleMetadata(filterMetaData())
                .setModules(ImmutableMap.copyOf(bundleModules))
                .build();
        System.out.printf("""
                 \n Filtering completed in %s
                -----------------------------------------
                 Reduced file count: %s
                 Reduced file size: %s
                -----------------------------------------
                %n""", timeClock.getElapsedTime(), filterTotalCount, FileOperation.getNetFileSizeDescription(filterTotalSize)
        );
        return appBundle;
    }

    /**
     * Filters the given bundle module by removing files that match filter rules.
     *
     * @param bundleModule The bundle module to filter.
     * @return The filtered bundle module.
     * @throws IOException If there is an error during the filtering process.
     */
    private BundleModule filterBundleModule(@NotNull BundleModule bundleModule) throws IOException {
        BundleModule.Builder builder = bundleModule.toBuilder();
        List<ModuleEntry> filteredModuleEntries = new ArrayList<>();
        List<ModuleEntry> entries = bundleModule.getEntries().stream()
                .filter(entry -> {
                    String filterRule = getMatchedFilterRule(entry.getPath());
                    if (filterRule != null) {
                        checkFilteredEntry(entry, filterRule);
                        System.out.printf(" - %s%n", entry.getPath());
                        filteredModuleEntries.add(entry);
                        filterTotalSize += (int) AppBundleUtils.getZipEntrySize(bundleZipFile, entry, bundleModule);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        builder.setRawEntries(entries);
        filterTotalCount += filteredModuleEntries.size();
        // update pb
        Files.NativeLibraries nativeLibraries = updateLibDirectory(bundleModule, filteredModuleEntries);
        if (nativeLibraries != null) {
            builder.setNativeConfig(nativeLibraries);
        }
        return builder.build();
    }

    /**
     * Updates the native libraries directory in the bundle module.
     *
     * @param bundleModule The bundle module to update.
     * @param entries      The list of filtered module entries.
     * @return The updated native libraries configuration.
     * @throws UnexpectedException If there is an unexpected error.
     */
    private Files.NativeLibraries updateLibDirectory(@NotNull BundleModule bundleModule, @NotNull List<ModuleEntry> entries) throws UnexpectedException {
        List<ModuleEntry> libEntries = entries.stream()
                .filter(entry -> entry.getPath().startsWith(BundleModule.LIB_DIRECTORY))
                .toList();
        Files.NativeLibraries nativeLibraries = bundleModule.getNativeConfig().orElse(null);
        if (libEntries.isEmpty()) {
            return nativeLibraries;
        }

        if (nativeLibraries == null) {
            throw new UnexpectedException(String.format("can not find nativeLibraries file `native.pb` in %s module", bundleModule.getName().getName()));
        }

        Files.NativeLibraries filteredNativeLibraries = nativeLibraries;
        for (Files.TargetedNativeDirectory directory : nativeLibraries.getDirectoryList()) {
            int directoryNativeSize = libEntries.stream()
                    .filter(entry -> entry.getPath().startsWith(directory.getPath()))
                    .toList().size();
            if (directoryNativeSize > 0) {
                int moduleNativeSize = bundleModule.getEntries().stream()
                        .filter(entry -> entry.getPath().startsWith(directory.getPath()))
                        .toList().size();
                if (directoryNativeSize == moduleNativeSize) {
                    filteredNativeLibraries = NativeLibrariesOperation.removeDirectory(filteredNativeLibraries, directory.getPath());
                }
            }
        }
        return filteredNativeLibraries;
    }

    /**
     * Filter metadata directory and return filtered list.
     *
     * @return The filtered metadata.
     */
    private BundleMetadata filterMetaData() {
        BundleMetadata.Builder builder = BundleMetadata.builder();
        Stream.of(rawAppBundle.getBundleMetadata())
                .map(BundleMetadata::getFileContentMap)
                .map(ImmutableMap::entrySet)
                .flatMap(Collection::stream)
                .filter(entry -> {
                    ZipPath entryZipPath = ZipPath.create(AppBundle.METADATA_DIRECTORY + "/" + entry.getKey());
                    if (getMatchedFilterRule(entryZipPath) != null) {
                        System.out.printf(" - %s%n", entryZipPath);
                        filterTotalCount += 1;
                        filterTotalSize += (int) AppBundleUtils.getZipEntrySize(bundleZipFile, entryZipPath);
                        return false;
                    }
                    return true;
                })
                .forEach(entry -> builder.addFile(entry.getKey(), entry.getValue()));
        return builder.build();
    }

    /**
     * Checks if the filtered entry is valid and can be filtered.
     *
     * @param entry      The module entry to be checked.
     * @param filterRule The filter rule applied to the entry.
     */
    private void checkFilteredEntry(@org.jetbrains.annotations.NotNull ModuleEntry entry, String filterRule) {
        if (!entry.getPath().startsWith(BundleModule.LIB_DIRECTORY) &&
            !entry.getPath().startsWith(METADATA_DIRECTORY.toString())) {
            throw new UnsupportedOperationException(String.format("%s entry can not be filtered, please check the filter rule [%s].", entry.getPath(), filterRule));
        }
    }

    /**
     * Get the filter rule that matches the given ZipPath.
     *
     * @param zipPath The ZipPath to match against filter rules.
     * @return The matched filter rule, or null if no rule matches.
     */
    private @Nullable String getMatchedFilterRule(ZipPath zipPath) {
        for (String rule : filterRules) {
            Pattern filterPattern = Pattern.compile(Utils.convertToPatternString(rule));
            if (filterPattern.matcher(zipPath.toString()).matches()) {
                return rule;
            }
        }
        return null;
    }
}