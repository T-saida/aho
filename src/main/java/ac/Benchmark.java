package ac;

import java.util.*;

public class Benchmark {
    static final int RUNS = 5;
    static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    static String randomString(Random r, int len, int sigma) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(ALPHABET.charAt(r.nextInt(sigma)));
        return sb.toString();
    }

    static String[] randomPatterns(Random r, int n, int minLen, int maxLen, int sigma) {
        String[] ps = new String[n];
        for (int i = 0; i < n; i++) ps[i] = randomString(r, minLen + r.nextInt(maxLen - minLen + 1), sigma);
        return ps;
    }

    public static void main(String[] args) {
        int[] patternCounts = {10, 100, 1000};
        int[] textLengths = {1_000, 10_000, 100_000};
        int sigma = 4; // small alphabet => many real matches; stresses output links
        Random rnd = new Random(42);

        {
            String t = randomString(rnd, 10_000, sigma);
            String[] p = randomPatterns(rnd, 100, 3, 8, sigma);
            for (int i = 0; i < 20; i++) { new AhoCorasick(p).count(t); KMP.countAll(t, p); }
        }

        System.out.println("patterns,textLen,AC_build_ms,AC_search_ms,AC_total_ms,KMP_total_ms,matches,nodes,speedup");
        for (int np : patternCounts) {
            for (int tl : textLengths) {
                String text = randomString(rnd, tl, sigma);
                String[] pats = randomPatterns(rnd, np, 3, 8, sigma);
                double build = 0, search = 0, kmp = 0;
                long acMatches = 0, kmpMatches = 0; int nodes = 0;
                for (int r = 0; r < RUNS; r++) {
                    long t0 = System.nanoTime();
                    AhoCorasick ac = new AhoCorasick(pats);
                    long t1 = System.nanoTime();
                    acMatches = ac.count(text);
                    long t2 = System.nanoTime();
                    kmpMatches = KMP.countAll(text, pats);
                    long t3 = System.nanoTime();
                    build += (t1 - t0); search += (t2 - t1); kmp += (t3 - t2);
                    nodes = ac.getNodeCount();
                }
                if (acMatches != kmpMatches)
                    throw new IllegalStateException("Mismatch: AC=" + acMatches + " KMP=" + kmpMatches);
                double b = build / RUNS / 1e6, s = search / RUNS / 1e6, k = kmp / RUNS / 1e6;
                System.out.printf(Locale.US, "%d,%d,%.3f,%.3f,%.3f,%.3f,%d,%d,%.1fx%n",
                        np, tl, b, s, b + s, k, acMatches, nodes, k / (b + s));
            }
        }
    }
}
