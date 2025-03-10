package io.github.goldfish07.reschiper.plugin.internal;

import com.android.SdkConstants;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.repository.Revision;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BuildToolInfo {
    public static @NotNull com.android.sdklib.BuildToolInfo getBuildToolInfo(Project project) {
        Path buildToolsDir = Paths.get(
                project.getExtensions().getByType(AndroidComponentsExtension.class).getSdkComponents()
                        .getSdkDirectory().get().toString(),
                SdkConstants.FD_BUILD_TOOLS,
                SdkConstants.CURRENT_BUILD_TOOLS_VERSION
        );

        return com.android.sdklib.BuildToolInfo.fromStandardDirectoryLayout(
                Revision.parseRevision(SdkConstants.CURRENT_BUILD_TOOLS_VERSION),
                buildToolsDir
        );
    }
}
