package io.github.goldfish07.reschiper.plugin.command.extensions;

import com.android.aapt.Resources;
import com.android.tools.build.bundletool.model.AppBundle;
import com.android.tools.build.bundletool.model.BundleModule;
import com.android.tools.build.bundletool.model.BundleModuleName;
import com.google.common.collect.ImmutableMap;
import io.github.goldfish07.reschiper.plugin.bundle.ResourceTableBuilder;
import io.github.goldfish07.reschiper.plugin.utils.TimeClock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * The `BundleStringFilter` class is responsible for filtering strings in an Android App Bundle (AAB) based on specific criteria.
 * It can remove unused strings and, optionally, specific languages from the resource table in each module of the bundle.
 * <p>
 * This class provides methods for filtering strings in the App Bundle and obfuscates the resource tables in each module
 * while respecting white-listed languages and a list of unused string names.
 */
public class BundleStringFilter {
    private static final String replaceValue = "[value removed]";
    private final AppBundle rawAppBundle;
    private final String unusedStrPath;
    private final Set<String> languageWhiteList;
    private final Set<String> unUsedNameSet = new HashSet<>(5000);

    /**
     * Constructs a `BundleStringFilter` instance with the provided parameters.
     *
     * @param bundlePath        The path to the input AAB file.
     * @param rawAppBundle      The original unfiltered App Bundle.
     * @param unusedStrPath     The path to a file containing a list of unused string names (one per line).
     * @param languageWhiteList A set of language codes to be preserved (optional).
     */
    public BundleStringFilter(Path bundlePath, AppBundle rawAppBundle, String unusedStrPath, Set<String> languageWhiteList) {
        checkFileExistsAndReadable(bundlePath);
        this.rawAppBundle = rawAppBundle;
        this.unusedStrPath = unusedStrPath;
        this.languageWhiteList = languageWhiteList;
    }

    /**
     * Filters the strings in the App Bundle based on the provided criteria.
     *
     * @return An AppBundle with filtered strings, or the original AppBundle if no filtering is applied.
     * @throws IOException If there is an issue with reading files or bundle contents.
     */
    public AppBundle filter() throws IOException {
        TimeClock timeClock = new TimeClock();
        File unusedStrFile = new File(unusedStrPath);
        Map<BundleModuleName, BundleModule> obfuscatedModules = new HashMap<>();
        if (unusedStrFile.exists()) {
            //shrink-results
            unUsedNameSet.addAll(Files.readAllLines(Paths.get(unusedStrPath)));
            System.out.println("unused string : " + unUsedNameSet.size());
        }
        if (!unUsedNameSet.isEmpty() || !languageWhiteList.isEmpty())
            for (Map.Entry<BundleModuleName, BundleModule> entry : rawAppBundle.getModules().entrySet()) {
                BundleModule bundleModule = entry.getValue();
                BundleModuleName bundleModuleName = entry.getKey();
                // obfuscate bundle module
                BundleModule obfuscatedModule = obfuscateBundleModule(bundleModule);
                obfuscatedModules.put(bundleModuleName, obfuscatedModule);
            }
        else
            return rawAppBundle;
        AppBundle appBundle = rawAppBundle.toBuilder()
                .setModules(ImmutableMap.copyOf(obfuscatedModules))
                .build();
        System.out.printf("filtering strings completed in %s\n%n", timeClock.getElapsedTime());
        return appBundle;
    }

    /**
     * Obfuscates the resource table of a bundle module based on the specified criteria.
     *
     * @param bundleModule The bundle module to obfuscate.
     * @return The obfuscated bundle module.
     */
    private BundleModule obfuscateBundleModule(@NotNull BundleModule bundleModule) {
        BundleModule.Builder builder = bundleModule.toBuilder();
        // obfuscate resourceTable
        Resources.ResourceTable obfuscatedResTable = obfuscateResourceTable(bundleModule);
        if (obfuscatedResTable != null)
            builder.setResourceTable(obfuscatedResTable);
        return builder.build();
    }

    /**
     * Obfuscates the resource table of a bundle module by removing unused strings and languages.
     *
     * @param bundleModule The bundle module containing the resource table to obfuscate.
     * @return The obfuscated resource table, or null if it's empty.
     */
    private Resources.@Nullable ResourceTable obfuscateResourceTable(@NotNull BundleModule bundleModule) {
        if (bundleModule.getResourceTable().isEmpty()) {
            return null;
        }
        Resources.ResourceTable rawTable = bundleModule.getResourceTable().get();
        ResourceTableBuilder tableBuilder = new ResourceTableBuilder();
        List<Resources.Package> packageList = rawTable.getPackageList();
        if (packageList.isEmpty())
            return tableBuilder.build();
        for (Resources.Package resPackage : packageList) {
            if (resPackage == null)
                continue;
            ResourceTableBuilder.PackageBuilder packageBuilder = tableBuilder.addPackage(resPackage);
            List<Resources.Type> typeList = resPackage.getTypeList();
            Set<String> languageFilterSet = new HashSet<>(100);
            List<String> nameFilterList = new ArrayList<>(3000);
            for (Resources.Type resType : typeList) {
                if (resType == null)
                    continue;
                List<Resources.Entry> entryList = resType.getEntryList();
                for (Resources.Entry resEntry : entryList) {
                    if (resEntry == null)
                        continue;
                    if (resPackage.getPackageId().getId() == 127 && resType.getName().equals("string") &&
                            languageWhiteList != null && !languageWhiteList.isEmpty()) {
                        //delete language
                        List<Resources.ConfigValue> languageValue = resEntry.getConfigValueList().stream()
                                .filter(Objects::nonNull)
                                .filter(configValue -> {
                                    String locale = configValue.getConfig().getLocale();
                                    if (keepLanguage(locale))
                                        return true;
                                    languageFilterSet.add(locale);
                                    return false;
                                }).collect(Collectors.toList());
                        resEntry = resEntry.toBuilder().clearConfigValue().addAllConfigValue(languageValue).build();
                    }
                    // delete unused strings identified by the shrink process
                    if (resPackage.getPackageId().getId() == 127 && resType.getName().equals("string")
                            && !unUsedNameSet.isEmpty() && unUsedNameSet.contains(resEntry.getName())) {
                        List<Resources.ConfigValue> proguardConfigValue = resEntry.getConfigValueList().stream()
                                .filter(Objects::nonNull)
                                .map(configValue -> {
                                    Resources.ConfigValue.Builder rcb = configValue.toBuilder();
                                    Resources.Value.Builder rvb = rcb.getValueBuilder();
                                    Resources.Item.Builder rib = rvb.getItemBuilder();
                                    Resources.String.Builder rfb = rib.getStrBuilder();
                                    return rcb.setValue(
                                            rvb.setItem(
                                                    rib.setStr(
                                                            rfb.setValue(replaceValue).build()
                                                    ).build()
                                            ).build()
                                    ).build();
                                }).collect(Collectors.toList());
                        nameFilterList.add(resEntry.getName());
                        resEntry = resEntry.toBuilder().clearConfigValue().addAllConfigValue(proguardConfigValue).build();
                    }
                    packageBuilder.addResource(resType, resEntry);
                }
            }
            System.out.println("filtering " + resPackage.getPackageName() + " id:" + resPackage.getPackageId().getId());
            StringBuilder l = new StringBuilder();
            for (String lan : languageFilterSet)
                l.append("[remove language] : ").append(lan).append("\n");
            System.out.println(l);
            l = new StringBuilder();
            for (String name : nameFilterList)
                l.append("[delete name] ").append(name).append("\n");
            System.out.println(l);
            System.out.println("-----------");
            packageBuilder.build();
        }
        return tableBuilder.build();
    }

    /**
     * Determines whether a language should be preserved based on the language white list.
     *
     * @param lan The language code to check.
     * @return True if the language should be preserved, false otherwise.
     */
    private boolean keepLanguage(String lan) {
        if (lan == null || lan.equals(" ") || lan.isEmpty())
            return true;
        if (lan.contains("-")) {
            int index = lan.indexOf("-");
            if (index != -1) {
                String language = lan.substring(0, index);
                return languageWhiteList.contains(language);
            }
        } else
            return languageWhiteList.contains(lan);
        return false;
    }
}
