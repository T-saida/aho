package ac;

import java.util.*;

public class KMP {
    public static int[] prefixFunction(String p) {
        int[] pi = new int[p.length()];
        for (int i = 1, k = 0; i < p.length(); i++) {
            while (k > 0 && p.charAt(i) != p.charAt(k)) k = pi[k - 1];
            if (p.charAt(i) == p.charAt(k)) k++;
            pi[i] = k;
        }
        return pi;
    }

    public static List<Integer> search(String text, String p) {
        List<Integer> res = new ArrayList<>();
        if (p.isEmpty()) return res;
        int[] pi = prefixFunction(p);
        for (int i = 0, k = 0; i < text.length(); i++) {
            while (k > 0 && text.charAt(i) != p.charAt(k)) k = pi[k - 1];
            if (text.charAt(i) == p.charAt(k)) k++;
            if (k == p.length()) { res.add(i - k + 1); k = pi[k - 1]; }
        }
        return res;
    }

    public static long countAll(String text, String[] patterns) {
        long total = 0;
        for (String p : patterns) total += search(text, p).size();
        return total;
    }
}
