import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public final class Benchmark {
    private static final int[] PATTERN_COUNTS = {10, 100, 1_000};
    private static final int[] TEXT_LENGTHS = {1_000, 10_000, 100_000};
    private static final int WARMUP_RUNS = 15;
    private static final int MEASURED_RUNS = 5;

    private record TimedSearch(long nanoseconds, List<Match> matches) {
    }

    public static void main(String[] args) {
        List<String> allPatterns = generatePatterns(PATTERN_COUNTS[PATTERN_COUNTS.length - 1]);

        System.out.println("pattern_count,text_length,match_count,"
                + "ac_run1_ns,ac_run2_ns,ac_run3_ns,ac_run4_ns,ac_run5_ns,ac_avg_ns,"
                + "kmp_run1_ns,kmp_run2_ns,kmp_run3_ns,kmp_run4_ns,kmp_run5_ns,kmp_avg_ns");

        for (int patternCount : PATTERN_COUNTS) {
            List<String> patterns = allPatterns.subList(0, patternCount);
            for (int textLength : TEXT_LENGTHS) {
                runCase(patterns, generateText(patterns, textLength));
            }
        }
    }

    private static void runCase(List<String> patterns, String text) {
        long[] acTimes = new long[MEASURED_RUNS];
        long[] kmpTimes = new long[MEASURED_RUNS];
        int matchCount = 0;

        for (int run = 0; run < WARMUP_RUNS + MEASURED_RUNS; run++) {
            TimedSearch ac;
            TimedSearch kmp;

            if (run % 2 == 0) {
                ac = timeAhoCorasick(patterns, text);
                kmp = timeKmp(patterns, text);
            } else {
                kmp = timeKmp(patterns, text);
                ac = timeAhoCorasick(patterns, text);
            }

            AlgorithmTest.requireSameMatches(patterns.size() + " patterns, " + text.length()
                    + " characters, run " + (run + 1), ac.matches(), kmp.matches());
            matchCount = ac.matches().size();

            if (run >= WARMUP_RUNS) {
                int index = run - WARMUP_RUNS;
                acTimes[index] = ac.nanoseconds();
                kmpTimes[index] = kmp.nanoseconds();
            }
        }

        System.out.printf(Locale.ROOT, "%d,%d,%d", patterns.size(), text.length(), matchCount);
        printTimes(acTimes);
        printTimes(kmpTimes);
        System.out.println();
    }

    private static TimedSearch timeAhoCorasick(List<String> patterns, String text) {
        long start = System.nanoTime();
        List<Match> matches = new AhoCorasick(patterns).search(text);
        long elapsed = System.nanoTime() - start;
        return new TimedSearch(elapsed, matches);
    }

    private static TimedSearch timeKmp(List<String> patterns, String text) {
        long start = System.nanoTime();
        List<Match> matches = Kmp.searchAll(patterns, text);
        long elapsed = System.nanoTime() - start;
        return new TimedSearch(elapsed, matches);
    }

    private static void printTimes(long[] times) {
        long total = 0;

        for (long time : times) {
            System.out.print("," + time);
            total += time;
        }

        System.out.printf(Locale.ROOT, ",%.1f", (double) total / times.length);
    }

    private static List<String> generatePatterns(int count) {
        Random random = new Random(2026);
        Set<String> patterns = new LinkedHashSet<>();

        while (patterns.size() < count) {
            int length = 5 + random.nextInt(6);
            StringBuilder pattern = new StringBuilder(length);

            for (int i = 0; i < length; i++) {
                pattern.append(randomLetter(random));
            }

            patterns.add(pattern.toString());
        }

        return new ArrayList<>(patterns);
    }

    private static String generateText(List<String> patterns, int length) {
        Random random = new Random(2027);
        StringBuilder text = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            text.append(randomLetter(random));
        }

        for (int offset = 0, i = 0; offset + 10 <= length; offset += 97, i++) {
            String pattern = patterns.get(i % patterns.size());
            text.replace(offset, offset + pattern.length(), pattern);
        }

        return text.toString();
    }

    private static char randomLetter(Random random) {
        return (char) ('a' + random.nextInt(26));
    }
}