package io.github.goldfish07.reschiper.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import io.github.goldfish07.reschiper.plugin.internal.AGP;
import io.github.goldfish07.reschiper.plugin.tasks.ResChiperTask;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin for integrating ResChiper into an Android Gradle project.
 */
public class ResChiperPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        checkApplicationPlugin(project);
        AppExtension android = (AppExtension) project.getExtensions().getByName("android");
        project.getExtensions().create("resChiper", Extension.class);
        project.afterEvaluate(project1 -> android.getApplicationVariants().all(variant -> createResChiperTask(project1, variant)));
    }

    /**
     * Creates a ResChiper task for the given variant.
     *
     * @param project The Gradle project.
     * @param variant The Android application variant.
     */
    private void createResChiperTask(@NotNull Project project, @NotNull ApplicationVariant variant) {
        String variantName = variant.getName().substring(0, 1).toUpperCase() + variant.getName().substring(1);
        String bundleTaskName = "bundle" + variantName;
        if (project.getTasks().findByName(bundleTaskName) == null)
            return;
        String taskName = "resChiper" + variantName;
        ResChiperTask resChiperTask;
        if (project.getTasks().findByName(taskName) == null)
            resChiperTask = project.getTasks().create(taskName, ResChiperTask.class);
        else
            resChiperTask = (ResChiperTask) project.getTasks().getByName(taskName);

        resChiperTask.setVariantScope(variant);
        resChiperTask.doFirst(task -> {
            printResChiperBuildConfiguration();
            printProjectBuildConfiguration(project);
        });

        Task bundleTask = project.getTasks().getByName(bundleTaskName);
        Task bundlePackageTask = project.getTasks().getByName("package" + variantName + "Bundle");
        bundleTask.dependsOn(resChiperTask);
        resChiperTask.dependsOn(bundlePackageTask);

        String finalizeBundleTaskName = "sign" + variantName + "Bundle";
        if (project.getTasks().findByName(finalizeBundleTaskName) != null)
            resChiperTask.dependsOn(project.getTasks().getByName(finalizeBundleTaskName));
    }

    /**
     * Checks if the Android Application plugin is applied to the project.
     *
     * @param project The Gradle project.
     */
    private void checkApplicationPlugin(@NotNull Project project) {
        if (!project.getPlugins().hasPlugin("com.android.application"))
            throw new GradleException("Android Application plugin 'com.android.application' is required");
    }

    /**
     * Prints the ResChiper build configuration information.
     */
    private void printResChiperBuildConfiguration() {
        System.out.println("----------------------------------------");
        System.out.println(" ResChiper Plugin Configuration:");
        System.out.println("----------------------------------------");
        System.out.println("- ResChiper version:\t" + ResChiper.VERSION);
        System.out.println("- BundleTool version:\t" + ResChiper.BT_VERSION);
        System.out.println("- AGP version:\t\t" + ResChiper.AGP_VERSION);
        System.out.println("- Gradle Wrapper:\t" + ResChiper.GRADLE_WRAPPER_VERSION);
    }

    /**
     * Prints the project's build information.
     *
     * @param project The Android Gradle project.
     */
    private void printProjectBuildConfiguration(@NotNull Project project) {
        System.out.println("----------------------------------------");
        System.out.println(" App Build Information:");
        System.out.println("----------------------------------------");
        System.out.println("- Project name:\t\t\t" + project.getRootProject().getName());
        System.out.println("- AGP version:\t\t\t" + AGP.getAGPVersion(project));
        System.out.println("- Running Gradle version:\t" + project.getGradle().getGradleVersion());
    }
}
