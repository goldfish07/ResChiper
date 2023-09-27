package io.github.goldfish07.reschiper.plugin.android;

/**
 * Utility class for detecting the current platform/operating system.
 */
public class PlatformDetector {

    /**
     * Constant representing an unknown platform.
     */
    public static final int PLATFORM_UNKNOWN = 0;

    /**
     * Constant representing the Linux platform.
     */
    public static final int PLATFORM_LINUX = 1;

    /**
     * Constant representing the Windows platform.
     */
    public static final int PLATFORM_WINDOWS = 2;

    /**
     * Constant representing the macOS (Darwin) platform.
     */
    public static final int PLATFORM_DARWIN = 3;

    /**
     * Detects and returns the current platform/operating system.
     *
     * @return An integer representing the current platform:
     * - {@link #PLATFORM_UNKNOWN} if the platform cannot be determined.
     * - {@link #PLATFORM_LINUX} if the platform is Linux.
     * - {@link #PLATFORM_WINDOWS} if the platform is Windows.
     * - {@link #PLATFORM_DARWIN} if the platform is macOS (Darwin).
     */
    public static int currentPlatform() {
        String os = System.getProperty("os.name");
        if (os.startsWith("Mac OS"))
            return PLATFORM_DARWIN;
        else if (os.startsWith("Windows"))
            return PLATFORM_WINDOWS;
        else if (os.startsWith("Linux"))
            return PLATFORM_LINUX;
        return PLATFORM_UNKNOWN;
    }
}
