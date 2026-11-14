import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class AlgorithmTest {
    private static final Comparator<Match> MATCH_ORDER = Comparator
            .comparingInt(Match::patternIndex)
            .thenComparingInt(Match::startIndex)
            .thenComparingInt(Match::endIndexExclusive);

    public static void main(String[] args) {
        check("failure and output links", List.of("he", "she", "his", "hers"), "ushers",
                List.of(new Match(0, 2, 4), new Match(1, 1, 4), new Match(3, 2, 6)));

        check("multiple suffix matches", List.of("a", "ba", "cba"), "cba",
                List.of(new Match(0, 2, 3), new Match(1, 1, 3), new Match(2, 0, 3)));

        check("non-terminal state with a terminal suffix", List.of("abc", "b"), "ab",
                List.of(new Match(1, 1, 2)));

        check("overlaps and duplicate patterns", List.of("aa", "aaa", "aa"), "aaaa",
                List.of(new Match(0, 0, 2), new Match(0, 1, 3), new Match(0, 2, 4),
                        new Match(1, 0, 3), new Match(1, 1, 4),
                        new Match(2, 0, 2), new Match(2, 1, 3), new Match(2, 2, 4)));

        check("case, punctuation and UTF-16 offsets", List.of("🙂", "!Ә", "ә"), "🙂!Әә",
                List.of(new Match(0, 0, 2), new Match(1, 2, 4), new Match(2, 4, 5)));

        check("no matches", List.of("ab", "abcdef"), "aaa", List.of());
        check("empty text", List.of("a"), "", List.of());
        check("empty pattern list", List.of(), "abc", List.of());

        expectEmptyPatternRejected(() -> new AhoCorasick(List.of("a", "")));
        expectEmptyPatternRejected(() -> Kmp.searchAll(List.of("a", ""), "abc"));

        checkAgainstNaiveSearch();
        System.out.println("PASS: 8 fixed cases, 2 empty-pattern checks, 200 randomized cases");
    }

    private static void check(String label, List<String> patterns, String text,
                              List<Match> expected) {
        requireSameMatches(label + " (Aho-Corasick)", expected,
                new AhoCorasick(patterns).search(text));
        requireSameMatches(label + " (KMP)", expected, Kmp.searchAll(patterns, text));
    }

    private static void expectEmptyPatternRejected(Runnable search) {
        try {
            search.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("An empty pattern was accepted");
    }

    private static void checkAgainstNaiveSearch() {
        Random random = new Random(17);

        for (int test = 0; test < 200; test++) {
            List<String> patterns = new ArrayList<>();
            int patternCount = 1 + random.nextInt(8);

            for (int i = 0; i < patternCount; i++) {
                patterns.add(randomString(random, 1 + random.nextInt(6)));
            }

            String text = randomString(random, random.nextInt(41));
            List<Match> expected = new ArrayList<>();

            for (int i = 0; i < patterns.size(); i++) {
                String pattern = patterns.get(i);
                for (int start = 0; start <= text.length() - pattern.length(); start++) {
                    if (text.startsWith(pattern, start)) {
                        expected.add(new Match(i, start, start + pattern.length()));
                    }
                }
            }

            check("random case " + test, patterns, text, expected);
        }
    }

    private static String randomString(Random random, int length) {
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            result.append((char) ('a' + random.nextInt(3)));
        }

        return result.toString();
    }

    static void requireSameMatches(String label, List<Match> expected, List<Match> actual) {
        List<Match> sortedExpected = new ArrayList<>(expected);
        List<Match> sortedActual = new ArrayList<>(actual);
        sortedExpected.sort(MATCH_ORDER);
        sortedActual.sort(MATCH_ORDER);

        if (!sortedExpected.equals(sortedActual)) {
            throw new AssertionError(label + "\nExpected: " + sortedExpected
                    + "\nActual: " + sortedActual);
        }
    }
}