package io.github.goldfish07.reschiper.plugin.internal;

import com.android.build.gradle.api.ApplicationVariant;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public class Bundle {
    public static @NotNull Path getBundleFilePath(Project project, @NotNull ApplicationVariant variant) {
        String flavor = variant.getName();
        return getBundleFileForAGP(project, flavor).toPath();
    }

    public static @Nullable File getBundleFileForAGP(@NotNull Project project, String flavor) {
        Task finalizeBundleTask = project.getTasks().getByName("sign" + capitalize(flavor) + "Bundle");
        Object bundleFile = finalizeBundleTask.property("finalBundleFile");
        Object regularFile;
        try {
            if (bundleFile != null) {
                regularFile = bundleFile.getClass().getMethod("get").invoke(bundleFile);
                return (File) regularFile.getClass().getMethod("getAsFile").invoke(regularFile);
            } else
                return null;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull String capitalize(@NotNull String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
