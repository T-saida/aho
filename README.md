# Aho–Corasick in Java

Multi-pattern string search: trie + failure links + output (dictionary) links, benchmarked against running KMP once per pattern.

## Structure
```
src/main/java/ac/AhoCorasick.java   automaton (trie, failure links, output links, search)
src/main/java/ac/KMP.java           baseline: KMP run separately for each pattern
src/main/java/ac/Benchmark.java     experiments (3 pattern counts x 3 text sizes x 5 runs)
src/test/java/ac/AhoCorasickTest.java  correctness tests (incl. randomized cross-check vs KMP)
results.csv                         raw benchmark output
```

## Build and run
```
mkdir out
javac -d out src/main/java/ac/*.java src/test/java/ac/*.java
java -cp out ac.AhoCorasickTest
java -cp out ac.Benchmark
```

## How it works
1. **Trie**: every pattern is inserted character by character; the node where a pattern ends stores its id.
2. **Failure links** (BFS): the failure link of node `v` (string `s`) points to the node for the longest proper suffix of `s` that is also a prefix of some pattern. For child `v` of `u` via char `c`, follow `u.fail`, `u.fail.fail`, ... until a node with a `c`-edge is found; that edge's target is `v.fail` (root if none). BFS order guarantees shallower nodes are already done.
3. **Output links**: `v.dictLink` = nearest node along the failure chain that ends a pattern. Walking dictLinks reports every pattern that ends at the current text position (e.g. `she` also reports `he`) without scanning the whole failure chain.
4. **Search**: scan text once; on mismatch follow failure links, on match go down the trie; at each position report the node's own patterns and then follow output links.

## Experimental setup
- Random lowercase text over a 4-letter alphabet (`a`–`d`), seed 42; patterns random, length 3–8.
- `System.nanoTime()`, 5 runs per case, average reported; JIT warm-up before measuring.
- Only counting matches was timed (no result-list allocation); AC and KMP match totals are asserted equal.
- AC total = build + search. KMP total = prefix function + search for all patterns.

## Results (average of 5 runs, ms)
| Patterns | Text length | AC build | AC search | AC total | KMP total | Matches | Trie nodes | KMP / AC |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 10 | 1,000 | 0.085 | 0.032 | 0.117 | 0.143 | 45 | 47 | 1.2x |
| 10 | 10,000 | 0.115 | 0.300 | 0.414 | 1.397 | 69 | 50 | 3.4x |
| 10 | 100,000 | 0.090 | 3.248 | 3.338 | 14.786 | 6,049 | 38 | 4.4x |
| 100 | 1,000 | 0.687 | 0.043 | 0.731 | 1.722 | 344 | 289 | 2.4x |
| 100 | 10,000 | 0.733 | 0.442 | 1.175 | 15.802 | 3,832 | 301 | 13.4x |
| 100 | 100,000 | 0.635 | 3.938 | 4.573 | 149.442 | 29,573 | 336 | 32.7x |
| 1000 | 1,000 | 3.862 | 0.078 | 3.940 | 13.790 | 3,408 | 1,869 | 3.5x |
| 1000 | 10,000 | 2.117 | 0.575 | 2.692 | 140.301 | 37,089 | 1,787 | 52.1x |
| 1000 | 100,000 | 0.987 | 5.372 | 6.358 | 1362.072 | 350,027 | 1,819 | 214.2x |

Timings are machine-dependent; sub-millisecond cases are noisy (JIT, GC), e.g. the 10 x 10,000 build time. Trie sizes are small because random patterns over 4 letters share many prefixes.

## Complexity
Let `m` = total length of all patterns, `n` = text length, `k` = number of patterns, `z` = number of reported matches, `σ` = alphabet size.

- **Build**: trie `O(m)`; failure/output links `O(m)` amortized (each pattern's failure pointer only moves back as far as it has advanced) — with HashMap edges, expected `O(m)`; with an array per node `O(mσ)` for a full goto automaton.
- **Search**: `O(n + z)`. Total failure-link steps are bounded by total forward steps (amortized argument, as in KMP), and output links make each reported match cost `O(1)`.
- **Space**: `O(m)` nodes (at most `m + 1`), each with its edges, failure link and output link (`O(mσ)` if arrays are used).
- **KMP per pattern**: `O(k·n + m + z)` time and `O(max pattern length)` extra space — the text is scanned `k` times.

## Discussion
**Why Aho–Corasick suits multi-pattern search.** The trie merges shared prefixes so all patterns are tracked simultaneously by a single state. Failure links generalize KMP's prefix function to a trie: on a mismatch they move to the longest suffix that is still a valid prefix of some pattern, so the text pointer never moves backward. Output links make overlapping and nested matches cheap to enumerate. The text is read exactly once, regardless of `k`.

Results. AC search time grows only slowly with the number of patterns (3.2 → 3.9 → 5.4 ms at n = 100,000 for 10 → 100 → 1,000 patterns; the growth comes mostly from reporting more matches), while KMP grows roughly linearly in k (14.8 → 149.4 → 1362.1 ms). The speedup reaches ~214x at 1,000 patterns and 100,000 characters. In the smallest case (10 patterns, 1,000 characters) the gap is only 1.2x, which is within measurement noise, because AC pays a one-time build cost.
**Advantages / limitations.**
- AC: one pass over the text, time independent of `k` apart from output, build once and reuse for many texts, ideal for dictionaries, virus signatures, keyword filters. Limitations: higher memory (trie with per-node maps), more complex implementation, build cost not worth it for a single short pattern or a one-off tiny search, and a dynamic pattern set requires rebuilding.
- KMP per pattern: simple, tiny memory, patterns can be added freely, fine for few patterns. Limitation: `O(k·n)` — re-scans the text for every pattern, which scales badly with many patterns or long texts.
