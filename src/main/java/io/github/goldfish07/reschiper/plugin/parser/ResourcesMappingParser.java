package io.github.goldfish07.reschiper.plugin.parser;

import io.github.goldfish07.reschiper.plugin.bundle.ResourceMapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * This class is responsible for parsing a resource mapping file used for resource obfuscation
 * in Android development and populating a {@link ResourceMapping} object with the mappings.
 */
public class ResourcesMappingParser {
    private static final Pattern MAP_DIR_PATTERN = Pattern.compile("^\\s+(.*)->(.*)");
    private static final Pattern MAP_RES_PATTERN = Pattern.compile("^\\s+(.*):(.*)->(.*)");
    private final Path mappingPath;

    /**
     * Constructs a new ResourcesMappingParser with the specified mapping file path.
     *
     * @param mappingPath The path to the resource mapping file.
     * @throws IllegalArgumentException If the mapping file does not exist or is not readable.
     */
    public ResourcesMappingParser(Path mappingPath) {
        checkFileExistsAndReadable(mappingPath);
        this.mappingPath = mappingPath;
    }

    /**
     * Parses the resource mapping file and returns a populated {@link ResourceMapping} object.
     *
     * @return A {@link ResourceMapping} object containing the parsed resource mappings.
     * @throws IOException If an I/O error occurs while reading the mapping file.
     */
    public ResourceMapping parse() throws IOException {
        ResourceMapping mapping = new ResourceMapping();

        FileReader fr = new FileReader(mappingPath.toFile());
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        while (line != null) {
            if (line.isEmpty()) {
                line = br.readLine();
                continue;
            }
            System.out.println("Res: " + line);
            if (!line.contains(":")) {
                Matcher mat = MAP_DIR_PATTERN.matcher(line);
                if (mat.find()) {
                    String rawName = mat.group(1).trim();
                    String obfuscateName = mat.group(2).trim();
                    if (!line.contains("/") || line.contains(".")) {
                        throw new IllegalArgumentException("Unexpected resource dir: " + line);
                    }
                    mapping.putDirMapping(rawName, obfuscateName);
                }
            } else {
                Matcher mat = MAP_RES_PATTERN.matcher(line);
                if (mat.find()) {
                    String rawName = mat.group(2).trim();
                    String obfuscateName = mat.group(3).trim();
                    if (line.contains("/")) {
                        mapping.putEntryFileMapping(rawName, obfuscateName);
                    } else {
                        int packagePos = rawName.indexOf(".R.");
                        if (packagePos == -1) {
                            throw new IllegalArgumentException(String.format("the mapping file packageName is malformed, "
                                            + "it should be like com.github.goldfish07.ugc.R.attr.test, yours %s\n",
                                    rawName
                            ));
                        }
                        mapping.putResourceMapping(rawName, obfuscateName);
                    }
                }
            }
            line = br.readLine();
        }

        br.close();
        fr.close();
        return mapping;
    }
}
