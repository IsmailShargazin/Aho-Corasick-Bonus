# Bonus Task — Aho–Corasick Algorithm

This Java project finds all occurrences of multiple patterns in a text and compares Aho–Corasick with running KMP separately for each pattern.

## How to run

Open the project in IntelliJ IDEA with JDK 17 or newer. Run `AlgorithmTest.java` to check correctness and `Benchmark.java` to measure execution time.

## How Aho–Corasick works

1. **Build the trie.** Insert each pattern one character at a time. Patterns with the same prefix share nodes. Mark the nodes where patterns end.
2. **Build failure links.** Process nodes level by level. Each child of the root links to the root. For a deeper node, start at its parent's failure link and try the edge with the character that leads to the node. Follow failure links until the edge is found. If the root has no such edge, link to the root.
3. **Search the text.** Read the text from left to right. If an edge is missing, follow failure links and retry the current character. Report matches at the current node. Then follow output links to report shorter patterns that end at the same position. An output link points to the nearest node on the failure chain where a pattern ends.

For example, with the patterns `he`, `she`, `his`, and `hers`, searching `ushers` finds `she` at index 1, `he` at index 2, and `hers` at index 2. The output link from `she` reports `he` at the same ending position. When the next character is `r`, the failure link to `he` lets the search continue toward `hers`.

## Complexity

`S` is the total length of the patterns, `P` is the number of patterns, `N` is the text length, `L` is the longest pattern length, and `Z` is the number of matches.

| Algorithm | Preprocessing time | Search time | Extra space including matches |
| --- | --- | --- | --- |
| Aho–Corasick | O(S) | O(N + Z) | O(S + Z) |
| KMP for each pattern | O(S) | O(P × N + Z) | O(L + Z) |

Aho–Corasick assumes expected O(1) trie edge lookups. Each failure step moves to a shallower node, so the total number of failure steps during the scan is O(N). Reporting matches takes O(Z) time. KMP keeps one prefix table at a time and scans the text once for each pattern.

## Tests and benchmark

The tests cover overlaps, suffix matches, duplicate patterns, and empty inputs. Both algorithms are also checked against a simple search on 200 random inputs.

The benchmark runs all nine combinations of pattern counts and text lengths. Each case has 15 warm-up runs and five measured runs on macOS with JDK 25. `System.nanoTime()` measures preprocessing, search, and match collection. Both algorithms returned the same matches in every run. The individual times and their averages are saved in `results.csv` in nanoseconds.

## Results

| Patterns | Text length | Matches | Aho–Corasick mean (ms) | KMP mean (ms) |
| ---: | ---: | ---: | ---: | ---: |
| 10 | 1,000 | 11 | 0.106 | 0.132 |
| 10 | 10,000 | 103 | 0.242 | 0.173 |
| 10 | 100,000 | 1,031 | 0.822 | 1.805 |
| 100 | 1,000 | 11 | 0.199 | 0.171 |
| 100 | 10,000 | 103 | 0.436 | 1.776 |
| 100 | 100,000 | 1,031 | 1.366 | 17.481 |
| 1,000 | 1,000 | 11 | 0.420 | 1.684 |
| 1,000 | 10,000 | 103 | 0.679 | 17.291 |
| 1,000 | 100,000 | 1,033 | 2.208 | 174.529 |

Aho–Corasick was faster in seven of the nine cases. In the largest case, it averaged 2.208 ms, and repeated KMP averaged 174.529 ms. KMP was faster with 10 patterns and 10,000 characters and with 100 patterns and 1,000 characters.

Aho–Corasick searches for all patterns in one pass and can reuse its trie for other texts. Building and storing the trie takes time and memory. KMP uses a smaller prefix table and is simpler for a single pattern. Its search time grows with the number of patterns because each pattern requires a separate scan.