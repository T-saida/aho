package ac;

import java.util.*;

public class AhoCorasick {

    private static final class Node {
        final Map<Character, Node> next = new HashMap<>();
        Node fail;
        Node dictLink;
        final List<Integer> out = new ArrayList<>(2);
        int depth;
    }

    public record Match(int patternId, int start) {}

    private final Node root = new Node();
    private final String[] patterns;
    private int nodeCount = 1;

    public AhoCorasick(String[] patterns) {
        this.patterns = patterns.clone();
        buildTrie();
        buildFailureLinks();
    }

    private void buildTrie() {
        for (int id = 0; id < patterns.length; id++) {
            String p = patterns[id];
            if (p.isEmpty()) continue;
            Node cur = root;
            for (char c : p.toCharArray()) {
                Node nxt = cur.next.get(c);
                if (nxt == null) {
                    nxt = new Node();
                    nxt.depth = cur.depth + 1;
                    cur.next.put(c, nxt);
                    nodeCount++;
                }
                cur = nxt;
            }
            cur.out.add(id);
        }
    }

    private void buildFailureLinks() {
        Deque<Node> queue = new ArrayDeque<>();
        for (Node child : root.next.values()) {
            child.fail = root;
            queue.add(child);
        }
        while (!queue.isEmpty()) {
            Node u = queue.poll();
            for (Map.Entry<Character, Node> e : u.next.entrySet()) {
                char c = e.getKey();
                Node v = e.getValue();
                Node f = u.fail;
                while (f != root && !f.next.containsKey(c)) f = f.fail;
                Node target = f.next.get(c);
                v.fail = (target != null && target != v) ? target : root;
                v.dictLink = v.fail.out.isEmpty() ? v.fail.dictLink : v.fail;
                queue.add(v);
            }
        }
    }

    public List<Match> search(String text) {
        List<Match> result = new ArrayList<>();
        Node state = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (state != root && !state.next.containsKey(c)) state = state.fail;
            Node nxt = state.next.get(c);
            if (nxt != null) state = nxt;

            for (Node n = state; n != null; n = n.dictLink) {
                for (int id : n.out) {
                    result.add(new Match(id, i - patterns[id].length() + 1));
                }
            }
        }
        return result;
    }

    public long count(String text) {
        long total = 0;
        Node state = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (state != root && !state.next.containsKey(c)) state = state.fail;
            Node nxt = state.next.get(c);
            if (nxt != null) state = nxt;
            for (Node n = state; n != null; n = n.dictLink) total += n.out.size();
        }
        return total;
    }

    public int getNodeCount() { return nodeCount; }
}
