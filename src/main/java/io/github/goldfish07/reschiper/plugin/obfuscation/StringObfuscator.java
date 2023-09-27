package io.github.goldfish07.reschiper.plugin.obfuscation;

import io.github.goldfish07.reschiper.plugin.utils.Utils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * A utility class for generating obfuscated replacement strings.
 */
public class StringObfuscator {

    private final List<String> replaceStringBuffer;
    private final Set<Integer> isReplaced;
    private final Set<Integer> isWhiteList;

    private static final String[] A_TO_Z = {
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
            "w", "x", "y", "z"
    };
    private static final String[] A_TO_ALL = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "_", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    };
    private static final Set<String> FILE_NAME_BLACKLIST = new HashSet<>(Arrays.asList("con", "prn", "aux", "nul"));
    private static final int MAX_OBFUSCATION_LIMIT = 35594;

    /**
     * Initializes a new instance of the StringObfuscator class.
     */
    public StringObfuscator() {
        replaceStringBuffer = new ArrayList<>();
        isReplaced = new HashSet<>();
        isWhiteList = new HashSet<>();
    }

    /**
     * Resets the state of the StringObfuscator with the provided blacklist patterns.
     *
     * @param blacklistPatterns A set of regular expression patterns for blacklisted strings.
     */
    public void reset(HashSet<Pattern> blacklistPatterns) {
        replaceStringBuffer.clear();
        isReplaced.clear();
        isWhiteList.clear();

        for (String str : A_TO_Z)
            if (Utils.match(str, blacklistPatterns))
                replaceStringBuffer.add(str);

        for (String first : A_TO_Z)
            for (String aMAToAll : A_TO_ALL) {
                String str = first + aMAToAll;
                if (Utils.match(str, blacklistPatterns))
                    replaceStringBuffer.add(str);
            }

        for (String first : A_TO_Z)
            for (String second : A_TO_ALL)
                for (String third : A_TO_ALL) {
                    String str = first + second + third;
                    if (!FILE_NAME_BLACKLIST.contains(str) && Utils.match(str, blacklistPatterns))
                        replaceStringBuffer.add(str);
                }
    }

    /**
     * Removes a collection of strings from the replacement buffer.
     *
     * @param collection The collection of strings to remove.
     */
    public void removeStrings(Collection<String> collection) {
        if (collection == null)
            return;
        replaceStringBuffer.removeAll(collection);
    }

    /**
     * Checks if a specific identifier has been replaced.
     *
     * @param id The identifier to check.
     * @return True if the identifier has been replaced; otherwise, false.
     */
    public boolean isReplaced(int id) {
        return isReplaced.contains(id);
    }

    /**
     * Checks if a specific identifier is in the white list.
     *
     * @param id The identifier to check.
     * @return True if the identifier is in the white list; otherwise, false.
     */
    public boolean isInWhiteList(int id) {
        return isWhiteList.contains(id);
    }

    /**
     * Adds an identifier to the white list.
     *
     * @param id The identifier to add to the white list.
     */
    public void setInWhiteList(int id) {
        isWhiteList.add(id);
    }

    /**
     * Adds an identifier to the replacement list.
     *
     * @param id The identifier to add to the replacement list.
     */
    public void setInReplaceList(int id) {
        isReplaced.add(id);
    }

    /**
     * Gets a replacement string from the buffer based on the provided names.
     *
     * @param names A collection of names to exclude from the replacements.
     * @return The replacement string.
     * @throws IllegalArgumentException If the replacement buffer is empty.
     */
    public String getReplaceString(Collection<String> names) throws IllegalArgumentException {
        if (replaceStringBuffer.isEmpty())
            throw new IllegalArgumentException("Now can only obfuscate up to " + MAX_OBFUSCATION_LIMIT + " in a single type");
        if (names != null)
            for (int i = 0; i < replaceStringBuffer.size(); i++) {
                String name = replaceStringBuffer.get(i);
                if (names.contains(name))
                    continue;
                return replaceStringBuffer.remove(i);
            }
        return replaceStringBuffer.remove(0);
    }
}
