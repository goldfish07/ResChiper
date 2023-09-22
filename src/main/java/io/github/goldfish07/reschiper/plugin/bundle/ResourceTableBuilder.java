package io.github.goldfish07.reschiper.plugin.bundle;

import com.android.aapt.Resources;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Builder for generating {@link com.android.aapt.Resources.ResourceTable}.
 */
public class ResourceTableBuilder {

    private final Resources.ResourceTable.Builder table;
    private final Map<String, PackageBuilder> resPackageMap;

    /**
     * Constructs a new ResourceTableBuilder.
     */
    public ResourceTableBuilder() {
        table = Resources.ResourceTable.newBuilder();
        resPackageMap = new HashMap<>();
    }

    /**
     * Adds a package to the resource table builder.
     *
     * @param resPackage The package to add.
     * @return The package builder.
     */
    public PackageBuilder addPackage(Resources.@NotNull Package resPackage) {
        if (resPackageMap.containsKey(resPackage.getPackageName())) {
            return resPackageMap.get(resPackage.getPackageName());
        }
        PackageBuilder packageBuilder = new PackageBuilder(resPackage);
        resPackageMap.put(resPackage.getPackageName(), packageBuilder);
        return packageBuilder;
    }

    /**
     * Generates the ResourceTable.
     *
     * @return The generated ResourceTable.
     */
    public Resources.ResourceTable build() {
        resPackageMap.forEach((key, value) -> table.addPackage(value.resPackageBuilder.build()));
        return table.build();
    }

    /**
     * Builder for generating packages within the ResourceTable.
     */
    public class PackageBuilder {

        Resources.Package.Builder resPackageBuilder;

        private PackageBuilder(Resources.Package resPackage) {
            addPackage(resPackage);
        }

        /**
         * Builds the package and returns to the ResourceTableBuilder.
         *
         * @return The ResourceTableBuilder.
         */
        public ResourceTableBuilder build() {
            return ResourceTableBuilder.this;
        }

        /**
         * Adds a package to the builder.
         *
         * @param resPackage The package to add.
         */
        private void addPackage(Resources.@NotNull Package resPackage) {
            int id = resPackage.getPackageId().getId();
            checkArgument(
                    table.getPackageList().stream().noneMatch(pkg -> pkg.getPackageId().getId() == id),
                    "Package ID %s already in use.", id);

            resPackageBuilder = Resources.Package.newBuilder()
                    .setPackageId(resPackage.getPackageId())
                    .setPackageName(resPackage.getPackageName());
        }

        /**
         * Gets a resource type from the package builder.
         *
         * @param resType The resource type to retrieve.
         * @return The resource type builder.
         */
        Resources.Type.Builder getResourceType(Resources.Type resType) {
            return resPackageBuilder.getTypeBuilderList().stream()
                    .filter(type -> type.getTypeId().getId() == resType.getTypeId().getId())
                    .findFirst()
                    .orElseGet(() -> addResourceType(resType));
        }

        /**
         * Adds a resource type to the package builder.
         *
         * @param resType The resource type to add.
         * @return The resource type builder.
         */
        Resources.Type.Builder addResourceType(Resources.@NotNull Type resType) {
            Resources.Type.Builder typeBuilder = Resources.Type.newBuilder()
                    .setName(resType.getName())
                    .setTypeId(resType.getTypeId());
            resPackageBuilder.addType(typeBuilder);
            return getResourceType(resType);
        }

        /**
         * Adds a resource entry to the package builder.
         *
         * @param resType  The resource type for the entry.
         * @param resEntry The resource entry to add.
         * @return The package builder.
         */
        public PackageBuilder addResource(Resources.Type resType, Resources.@NotNull Entry resEntry) {
            Resources.Type.Builder type = getResourceType(resType);
            checkState(resPackageBuilder != null, "A package must be created before a resource can be added.");
            if (!resEntry.getEntryId().isInitialized()) {
                resEntry = resEntry.toBuilder().setEntryId(
                        resEntry.getEntryId().toBuilder().setId(0).build()
                ).build();
            }
            type.addEntry(resEntry.toBuilder());
            return this;
        }
    }
}