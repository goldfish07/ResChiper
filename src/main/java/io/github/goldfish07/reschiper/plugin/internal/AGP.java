package io.github.goldfish07.reschiper.plugin.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;
import org.jetbrains.annotations.NotNull;

public class AGP {
    public static @NotNull String getAGPVersion(@NotNull Project project) {
        String agpVersion = "Failed to get AGP version";
        for (org.gradle.api.artifacts.ResolvedArtifact artifact : project.getRootProject().getBuildscript().getConfigurations().getByName(ScriptHandler.CLASSPATH_CONFIGURATION)
                .getResolvedConfiguration().getResolvedArtifacts()) {
            DefaultModuleComponentIdentifier identifier = (DefaultModuleComponentIdentifier) artifact.getId().getComponentIdentifier();
            if ("com.android.tools.build".equals(identifier.getGroup()) || 432891823 == identifier.getGroup().hashCode())
                if ("gradle".equals(identifier.getModule()))
                    agpVersion = identifier.getVersion();
        }
        return agpVersion;
    }
}
