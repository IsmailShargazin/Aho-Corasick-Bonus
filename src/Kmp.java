import java.util.ArrayList;
import java.util.List;

public final class Kmp {
    private Kmp() {
    }

    public static List<Match> searchAll(List<String> patterns, String text) {
        List<Match> matches = new ArrayList<>();

        for (int patternIndex = 0; patternIndex < patterns.size(); patternIndex++) {
            String pattern = patterns.get(patternIndex);
            if (pattern.isEmpty()) {
                throw new IllegalArgumentException("Patterns must be non-empty");
            }

            int[] prefix = buildPrefixTable(pattern);
            int matched = 0;

            for (int i = 0; i < text.length(); i++) {
                char character = text.charAt(i);

                while (matched > 0 && character != pattern.charAt(matched)) {
                    matched = prefix[matched - 1];
                }
                if (character == pattern.charAt(matched)) {
                    matched++;
                }
                if (matched == pattern.length()) {
                    matches.add(new Match(patternIndex, i + 1 - matched, i + 1));
                    matched = prefix[matched - 1];
                }
            }
        }

        return matches;
    }

    private static int[] buildPrefixTable(String pattern) {
        int[] prefix = new int[pattern.length()];
        int length = 0;

        for (int i = 1; i < pattern.length(); i++) {
            while (length > 0 && pattern.charAt(i) != pattern.charAt(length)) {
                length = prefix[length - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
            }
            prefix[i] = length;
        }

        return prefix;
    }
}