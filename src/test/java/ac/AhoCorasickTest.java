package ac;

import java.util.*;


public class AhoCorasickTest {
    static int failures = 0;

    static void check(boolean cond, String name) {
        System.out.println((cond ? "PASS " : "FAIL ") + name);
        if (!cond) failures++;
    }

    static Set<String> toSet(List<AhoCorasick.Match> ms, String[] ps) {
        Set<String> s = new TreeSet<>();
        for (var m : ms) s.add(ps[m.patternId()] + "@" + m.start());
        return s;
    }

    public static void main(String[] args) {
        String[] ps = {"he", "she", "his", "hers"};
        var ms = new AhoCorasick(ps).search("ushers");
        check(toSet(ms, ps).equals(new TreeSet<>(List.of("she@1", "he@2", "hers@2"))), "classic ushers");

        String[] ps2 = {"a", "aa", "aaa"};
        check(new AhoCorasick(ps2).search("aaaa").size() == 4 + 3 + 2, "nested patterns on aaaa");

        String[] ps3 = {"abcd", "bcd", "cd", "d"};
        check(new AhoCorasick(ps3).search("abcd").size() == 4, "suffix chain");

        check(new AhoCorasick(new String[]{"xyz"}).search("abcabc").isEmpty(), "no match");

        String[] ps4 = {"ab", "ab"};
        check(new AhoCorasick(ps4).search("abab").size() == 4, "duplicate patterns");

        check(new AhoCorasick(new String[]{"a"}).search("").isEmpty(), "empty text");

        Random r = new Random(1);
        boolean ok = true;
        for (int t = 0; t < 200 && ok; t++) {
            String text = Benchmark.randomString(r, 200, 3);
            String[] p = Benchmark.randomPatterns(r, 1 + r.nextInt(15), 1, 5, 3);
            Set<String> expected = new TreeSet<>();
            for (int i = 0; i < p.length; i++)
                for (int pos : KMP.search(text, p[i])) expected.add(p[i] + "@" + pos);
            Set<String> got = new TreeSet<>();
            for (var m : new AhoCorasick(p).search(text)) got.add(p[m.patternId()] + "@" + m.start());
            ok = expected.equals(got);
        }
        check(ok, "200 random cases vs KMP");

        if (failures > 0) { System.out.println(failures + " test(s) failed"); System.exit(1); }
        System.out.println("All tests passed");
    }
}
