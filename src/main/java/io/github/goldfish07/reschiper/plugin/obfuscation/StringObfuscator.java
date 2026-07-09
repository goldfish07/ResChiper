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
     * Generates a hash-based string that starts with a letter.
     *
     * @param input The input string to hash.
     * @return A hash string that starts with a letter.
     */
    private String getHashStartingWithLetter(String input) {
        int hash = input.hashCode();
        String hashStr = Integer.toHexString(Math.abs(hash));

        // Ensure the hash starts with a letter
        if (Character.isDigit(hashStr.charAt(0))) {
            // Map digits to letters: 0->a, 1->b, 2->c, 3->d, 4->e, 5->f, 6->g, 7->h, 8->i, 9->j
            char firstChar = (char) ('a' + (hashStr.charAt(0) - '0'));
            hashStr = firstChar + hashStr.substring(1);
        }

        return hashStr;
    }

    /**
     * Gets a replacement string from the buffer based on the provided names.
     *
     * @param names A collection of names to exclude from the replacements.
     * @param obfuscationSeed An optional obfuscation seed string for obfuscation. If provided, uses seed-based hashing logic.
     * @return The replacement string.
     * @throws IllegalArgumentException If the replacement buffer is empty.
     */
    public String getReplaceString(Collection<String> names, String obfuscationSeed) throws IllegalArgumentException {
        if (replaceStringBuffer.isEmpty())
            throw new IllegalArgumentException("Now can only obfuscate up to " + MAX_OBFUSCATION_LIMIT + " in a single type");

        // If obfuscationSeed is provided, use seed-based hashing logic
        if (obfuscationSeed != null && !obfuscationSeed.isEmpty()) {
            String result;
            if (names != null) {
                for (int i = 0; i < replaceStringBuffer.size(); i++) {
                    result = replaceStringBuffer.get(i);
                    String nameWithHash = getHashStartingWithLetter(result + obfuscationSeed);

                    if (names.contains(nameWithHash)) {
                        continue;
                    }

                    replaceStringBuffer.remove(i);
                    return nameWithHash;
                }
            }
            result = replaceStringBuffer.remove(0);
            return getHashStartingWithLetter(result + obfuscationSeed);
        }

        // Original logic when obfuscationSeed is not provided
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
