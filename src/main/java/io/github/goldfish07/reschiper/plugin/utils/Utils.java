package io.github.goldfish07.reschiper.plugin.utils;

import io.github.goldfish07.reschiper.plugin.operations.FileOperation;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {
    public static boolean isPresent(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean isBlank(String str) {
        return !isPresent(str);
    }

    public static boolean isPresent(Iterator<Boolean> iterator) {
        return iterator != null && iterator.hasNext();
    }

    public static boolean isBlank(Iterator<Boolean> iterator) {
        return !isPresent(iterator);
    }

    public static String convertToPatternString(String input) {
        // ?	Zero or one character
        // *	Zero or more of character
        // +	One or more of characters
        final String[] searchList = new String[]{".", "?", "*", "+"};
        final String[] replacementList = new String[]{"\\.", ".?", ".*", ".+"};
        return replaceEach(input, searchList, replacementList);
    }

    public static boolean match(String str, HashSet<Pattern> patterns) {
        if (patterns == null) {
            return true;
        }
        for (Pattern p : patterns) {
            boolean isMatch = p.matcher(str).matches();
            if (isMatch) return false;
        }
        return true;
    }

    public static void cleanDir(@NotNull File dir) {
        if (dir.exists()) {
            FileOperation.deleteDir(dir);
            dir.mkdirs();
        }
    }

    public static String readInputStream(@NotNull InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    public static String runCmd(String... cmd) throws IOException, InterruptedException {
        String output;
        Process process = null;
        try {
            process = new ProcessBuilder(cmd).start();
            output = readInputStream(process.getInputStream());
            process.waitFor();
            if (process.exitValue() != 0) {
                System.err.printf("%s Failed! Please check your signature file.\n%n", cmd[0]);
                throw new RuntimeException(readInputStream(process.getErrorStream()));
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return output;
    }

    public static String runExec(String[] argv) throws IOException, InterruptedException {
        Process process = null;
        String output;
        try {
            process = Runtime.getRuntime().exec(argv);
            output = readInputStream(process.getInputStream());
            process.waitFor();
            if (process.exitValue() != 0) {
                System.err.printf("%s Failed! Please check your signature file.\n%n", argv[0]);
                throw new RuntimeException(readInputStream(process.getErrorStream()));
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return output;
    }

    private static void processOutputStreamInThread(@NotNull Process process) throws IOException {
        InputStreamReader ir = new InputStreamReader(process.getInputStream());
        LineNumberReader input = new LineNumberReader(ir);
        // If not read, there may be issues; it is blocked.
        while (input.readLine() != null) {
        }
    }

    private static String replaceEach(String text, String[] searchList, String[] replacementList) {
        // TODO: throw new IllegalArgumentException() if any param doesn't make sense
        //validateParams(text, searchList, replacementList);

        SearchTracker tracker = new SearchTracker(text, searchList, replacementList);
        if (!tracker.hasNextMatch(0)) {
            return text;
        }

        StringBuilder buf = new StringBuilder(text.length() * 2);
        int start = 0;

        do {
            SearchTracker.MatchInfo matchInfo = tracker.matchInfo;
            int textIndex = matchInfo.textIndex;
            String pattern = matchInfo.pattern;
            String replacement = matchInfo.replacement;

            buf.append(text, start, textIndex);
            buf.append(replacement);

            start = textIndex + pattern.length();
        } while (tracker.hasNextMatch(start));

        return buf.append(text.substring(start)).toString();
    }

    static class SearchTracker {
        final String text;
        final Map<String, String> patternToReplacement = new HashMap<>();
        final Set<String> pendingPatterns = new HashSet<>();
        MatchInfo matchInfo = null;

        SearchTracker(String text, String @NotNull [] searchList, String[] replacementList) {
            this.text = text;
            for (int i = 0; i < searchList.length; ++i) {
                String pattern = searchList[i];
                patternToReplacement.put(pattern, replacementList[i]);
                pendingPatterns.add(pattern);
            }
        }

        boolean hasNextMatch(int start) {
            int textIndex = -1;
            String nextPattern = null;

            for (String pattern : new ArrayList<>(pendingPatterns)) {
                int matchIndex = text.indexOf(pattern, start);
                if (matchIndex == -1) {
                    pendingPatterns.remove(pattern);
                } else {
                    if (textIndex == -1 || matchIndex < textIndex) {
                        textIndex = matchIndex;
                        nextPattern = pattern;
                    }
                }
            }

            if (nextPattern != null) {
                matchInfo = new MatchInfo(nextPattern, patternToReplacement.get(nextPattern), textIndex);
                return true;
            }
            return false;
        }

        private record MatchInfo(String pattern, String replacement, int textIndex) {
        }
    }
}
