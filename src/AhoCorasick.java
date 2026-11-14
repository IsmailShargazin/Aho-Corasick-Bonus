import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class AhoCorasick {
    private static final class Node {
        final Map<Character, Node> children = new HashMap<>();
        final List<Integer> patternIndices = new ArrayList<>();
        Node failure;
        Node output;
    }

    private final Node root = new Node();
    private final List<String> patterns;

    public AhoCorasick(List<String> patterns) {
        this.patterns = List.copyOf(patterns);
        buildTrie();
        buildLinks();
    }

    private void buildTrie() {
        for (int patternIndex = 0; patternIndex < patterns.size(); patternIndex++) {
            String pattern = patterns.get(patternIndex);
            if (pattern.isEmpty()) {
                throw new IllegalArgumentException("Patterns must be non-empty");
            }

            Node node = root;
            for (int i = 0; i < pattern.length(); i++) {
                node = node.children.computeIfAbsent(pattern.charAt(i), key -> new Node());
            }
            node.patternIndices.add(patternIndex);
        }
    }

    private void buildLinks() {
        Queue<Node> queue = new ArrayDeque<>();
        root.failure = root;

        for (Node child : root.children.values()) {
            child.failure = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();

            for (Map.Entry<Character, Node> edge : node.children.entrySet()) {
                Node child = edge.getValue();
                Node failure = advance(node.failure, edge.getKey());

                child.failure = failure;
                child.output = failure.patternIndices.isEmpty() ? failure.output : failure;
                queue.add(child);
            }
        }
    }

    private Node advance(Node node, char character) {
        Node next = node.children.get(character);

        while (next == null && node != root) {
            node = node.failure;
            next = node.children.get(character);
        }

        return next == null ? root : next;
    }

    public List<Match> search(String text) {
        List<Match> matches = new ArrayList<>();
        Node node = root;

        for (int i = 0; i < text.length(); i++) {
            node = advance(node, text.charAt(i));

            for (Node terminal = node; terminal != null; terminal = terminal.output) {
                for (int patternIndex : terminal.patternIndices) {
                    int start = i + 1 - patterns.get(patternIndex).length();
                    matches.add(new Match(patternIndex, start, i + 1));
                }
            }
        }

        return matches;
    }
}