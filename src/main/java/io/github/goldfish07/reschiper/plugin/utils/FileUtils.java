package io.github.goldfish07.reschiper.plugin.utils;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class FileUtils {
    private static final String UNIX_LINE_SEPARATOR = "\n";

    /**
     * Loads a text file, forcing the line separator to be Unix-style '\n'.
     *
     * @param file the file to read from
     * @return the content of the file with Unix-style line separators
     * @throws IOException if an I/O error occurs or the file does not exist
     */
    public static @NotNull String loadFileWithUnixLineSeparators(@NotNull File file) throws IOException {
        checkNotNull(file, "File must not be null");
        checkArgument(file.exists(), "File does not exist");
        checkArgument(file.isFile(), "File is not a regular file");

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return String.join(UNIX_LINE_SEPARATOR, lines);
        }
    }

    /**
     * Creates a new text file or replaces the content of an existing file.
     *
     * @param file    the file to write to
     * @param content the new content of the file
     * @throws IOException if an I/O error occurs
     */
    public static void writeToFile(@NotNull File file, @NotNull String content) throws IOException {
        checkNotNull(file, "File must not be null");
        checkNotNull(content, "Content must not be null");

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}
