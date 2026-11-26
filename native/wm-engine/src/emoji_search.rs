//! Semantic emoji search and query completion engine (ported from Kotlin `EmojiSearch`).
//!
//! Keeps the packed inverted index and lazy stem index entirely in Rust native
//! memory instead of the Android JVM heap, eliminating ~6 MB of boxed Java
//! collections and per-keystroke GC pressure.

use std::collections::{BTreeMap, HashMap, HashSet};
use std::sync::OnceLock;

use crate::stem::{stem_text_into, Context};

const STEM_WEIGHT: i32 = 50;

/// Concept expansion table for English, Arabic, and Spanish searches.
const SYNONYMS: &[(&str, &[&str])] = &[
    // English
    ("happy", &["joy", "smile", "cheerful", "celebrate"]),
    ("sad", &["cry", "tear", "disappointed", "unhappy"]),
    ("laugh", &["lol", "funny", "haha", "joy", "rofl"]),
    ("funny", &["laugh", "lol", "joke"]),
    ("love", &["heart", "romance", "affection", "adore", "kiss"]),
    ("party", &["celebrate", "celebration", "festive", "confetti", "birthday"]),
    ("birthday", &["party", "celebration", "cake", "gift", "balloon"]),
    ("celebrate", &["party", "celebration", "congrats"]),
    ("angry", &["mad", "rage", "furious"]),
    ("food", &["eat", "hungry", "tasty", "meal"]),
    ("eat", &["food", "tasty", "hungry"]),
    ("drink", &["beverage", "coffee", "tea", "juice"]),
    ("fire", &["flame", "hot", "burn", "explosion", "lit", "comet"]),
    ("hot", &["fire", "heat", "sun"]),
    ("cold", &["snow", "ice", "freezing", "winter"]),
    ("fast", &["speed", "quick", "rocket", "run"]),
    ("money", &["cash", "dollar", "rich", "payment"]),
    ("work", &["office", "job", "laptop", "computer"]),
    ("sleep", &["tired", "zzz", "nap", "bed"]),
    ("music", &["song", "melody", "sing", "instrument"]),
    ("travel", &["flight", "vacation", "trip", "airplane"]),
    ("win", &["trophy", "winner", "champion", "medal", "first"]),
    ("cute", &["adorable", "sweet", "baby", "kitten", "puppy"]),
    ("animal", &["pet", "dog", "cat", "bird"]),
    ("flower", &["blossom", "rose", "bouquet", "spring"]),
    ("rain", &["weather", "storm", "umbrella", "cloud"]),
    ("night", &["moon", "star", "sleep", "dark"]),
    ("pray", &["prayer", "dua", "worship", "religion"]),
    ("sick", &["ill", "fever", "doctor", "medicine"]),
    ("strong", &["muscle", "power", "gym", "flex"]),
    ("think", &["thinking", "wonder", "brain", "idea"]),
    ("scared", &["fear", "afraid", "horror", "scream"]),
    ("cool", &["sunglasses", "awesome"]),
    ("ok", &["okay", "thumbs up", "check"]),
    ("yes", &["check", "correct", "thumbs up"]),
    ("no", &["cross", "wrong", "prohibited", "thumbs down"]),
    // Arabic concept expansion
    ("سعيد", &["فرح", "ابتسامة", "happy", "smile", "joy"]),
    ("فرح", &["سعيد", "احتفال", "happy", "celebrate"]),
    ("حزين", &["بكاء", "دموع", "sad", "cry"]),
    ("ضحك", &["مضحك", "ابتسامة", "laugh", "funny", "lol"]),
    ("حب", &["قلب", "عشق", "love", "heart"]),
    ("قلب", &["حب", "heart", "love"]),
    ("حفلة", &["احتفال", "عيد", "party", "celebrate", "birthday"]),
    ("غضب", &["غاضب", "angry", "mad", "rage"]),
    ("طعام", &["أكل", "جوع", "food", "eat"]),
    ("أكل", &["طعام", "food", "eat"]),
    ("نار", &["حريق", "ساخن", "fire", "flame", "hot"]),
    ("مطر", &["طقس", "غيوم", "rain", "weather", "umbrella"]),
    ("نوم", &["تعب", "سرير", "sleep", "tired", "zzz"]),
    ("عمل", &["مكتب", "حاسوب", "work", "office", "laptop"]),
    ("موسيقى", &["أغنية", "music", "song"]),
    ("سفر", &["طائرة", "رحلة", "travel", "flight", "airplane"]),
    ("فوز", &["كأس", "بطل", "win", "trophy", "medal"]),
    ("دعاء", &["صلاة", "pray", "prayer"]),
    ("مريض", &["طبيب", "دواء", "sick", "doctor", "medicine"]),
    ("نعم", &["صح", "yes", "check"]),
    ("لا", &["خطأ", "no", "cross"]),
    // Spanish concept expansion
    ("feliz", &["alegre", "sonrisa", "happy", "smile", "joy"]),
    ("triste", &["llorar", "lágrima", "sad", "cry"]),
    ("risa", &["gracioso", "jaja", "laugh", "funny"]),
    ("amor", &["corazón", "beso", "love", "heart"]),
    ("fiesta", &["celebrar", "cumpleaños", "party", "celebrate"]),
    ("enojado", &["enfadado", "ira", "angry", "mad"]),
    ("comida", &["comer", "hambre", "food", "eat"]),
    ("fuego", &["llama", "caliente", "fire", "flame"]),
    ("lluvia", &["tormenta", "paraguas", "rain", "weather"]),
    ("dormir", &["sueño", "cama", "sleep", "tired"]),
    ("trabajo", &["oficina", "work", "office"]),
    ("música", &["canción", "music", "song"]),
    ("musica", &["canción", "music", "song"]),
    ("viaje", &["vuelo", "avión", "travel", "flight"]),
    ("ganar", &["trofeo", "medalla", "win", "trophy"]),
];

fn synonyms_for(token: &str) -> &'static [&'static str] {
    for &(k, vals) in SYNONYMS {
        if k == token {
            return vals;
        }
    }
    &[]
}

struct StemIndex {
    stems: Vec<String>,
    start: Vec<usize>,
    slots: Vec<usize>,
}

pub struct EmojiSearchIndex {
    terms: Vec<String>,
    posting_start: Vec<usize>,
    postings: Vec<i32>,
    stem_index: OnceLock<StemIndex>,
}

impl EmojiSearchIndex {
    /// Builds a packed inverted index from `entries_keywords`, where each element
    /// holds one emoji entry's keywords separated by ASCII Unit Separator (`\x1f`).
    pub fn new(entries_keywords: &[String]) -> Self {
        let mut building: BTreeMap<String, Vec<i32>> = BTreeMap::new();

        for (idx, packed) in entries_keywords.iter().enumerate() {
            let entry_idx = idx as i32;
            for keyword in packed.split('\x1f') {
                if keyword.is_empty() {
                    continue;
                }
                Self::insert_posting(&mut building, keyword, entry_idx);
                for token in keyword.split(' ') {
                    if !token.is_empty() && token != keyword {
                        Self::insert_posting(&mut building, token, entry_idx);
                    }
                }
            }
        }

        let num_terms = building.len();
        let mut terms = Vec::with_capacity(num_terms);
        let mut posting_start = Vec::with_capacity(num_terms + 1);
        let total_postings: usize = building.values().map(|v| v.len()).sum();
        let mut postings = Vec::with_capacity(total_postings);

        for (term, list) in building {
            posting_start.push(postings.len());
            postings.extend_from_slice(&list);
            terms.push(term);
        }
        posting_start.push(postings.len());

        Self {
            terms,
            posting_start,
            postings,
            stem_index: OnceLock::new(),
        }
    }

    #[inline]
    fn insert_posting(map: &mut BTreeMap<String, Vec<i32>>, term: &str, entry_idx: i32) {
        if let Some(list) = map.get_mut(term) {
            if list.last().copied() != Some(entry_idx) {
                list.push(entry_idx);
            }
        } else {
            map.insert(term.to_owned(), vec![entry_idx]);
        }
    }

    fn stem_str(ctx: &Context, s: &str, u16_in: &mut Vec<u16>, u16_out: &mut Vec<u16>) -> String {
        u16_in.clear();
        u16_in.extend(s.encode_utf16());
        u16_out.clear();
        stem_text_into(ctx, u16_in, u16_out);
        String::from_utf16_lossy(u16_out)
    }

    fn get_or_build_stem_index(&self, lang_tag: &str) -> &StemIndex {
        self.stem_index.get_or_init(|| {
            let tag = if lang_tag.is_empty() { "en" } else { lang_tag };
            let ctx = Context::of_tag(tag);
            let mut grouped: BTreeMap<String, Vec<usize>> = BTreeMap::new();
            let mut u16_in = Vec::with_capacity(32);
            let mut u16_out = Vec::with_capacity(32);

            for (t, term) in self.terms.iter().enumerate() {
                let stemmed = Self::stem_str(&ctx, term, &mut u16_in, &mut u16_out);
                if !stemmed.is_empty() {
                    grouped.entry(stemmed).or_default().push(t);
                }
            }

            let mut stems = Vec::with_capacity(grouped.len());
            let mut start = Vec::with_capacity(grouped.len() + 1);
            let mut slots = Vec::with_capacity(self.terms.len());
            for (s, list) in grouped {
                start.push(slots.len());
                slots.extend_from_slice(&list);
                stems.push(s);
            }
            start.push(slots.len());
            StemIndex { stems, start, slots }
        })
    }

    #[inline]
    fn term_index(&self, term: &str) -> Option<usize> {
        self.terms.binary_search_by(|t| t.as_str().cmp(term)).ok()
    }

    #[inline]
    fn prefix_range(&self, prefix: &str) -> std::ops::Range<usize> {
        let low = self
            .terms
            .binary_search_by(|t| t.as_str().cmp(prefix))
            .unwrap_or_else(|i| i);
        let mut high = low;
        while high < self.terms.len() && self.terms[high].starts_with(prefix) {
            high += 1;
        }
        low..high
    }

    #[inline]
    fn postings_of(&self, t: usize) -> &[i32] {
        &self.postings[self.posting_start[t]..self.posting_start[t + 1]]
    }

    #[inline]
    fn score_term(&self, term: &str, weight: i32, scores: &mut HashMap<i32, i32>) {
        if let Some(t) = self.term_index(term) {
            for &entry_idx in self.postings_of(t) {
                *scores.entry(entry_idx).or_insert(0) += weight;
            }
        }
    }

    fn score_stem(
        &self,
        token: &str,
        token_stem: &str,
        stem_idx: &StemIndex,
        scores: &mut HashMap<i32, i32>,
    ) {
        if token_stem.is_empty() {
            return;
        }
        let Ok(i) = stem_idx.stems.binary_search_by(|s| s.as_str().cmp(token_stem)) else {
            return;
        };
        let mut reached = HashSet::new();
        for &t in &stem_idx.slots[stem_idx.start[i]..stem_idx.start[i + 1]] {
            if self.terms[t] != token {
                for &entry_idx in self.postings_of(t) {
                    reached.insert(entry_idx);
                }
            }
        }
        for entry_idx in reached {
            *scores.entry(entry_idx).or_insert(0) += STEM_WEIGHT;
        }
    }

    /// Searches the emoji index and returns up to `limit` catalog indices ordered by
    /// descending relevance score, breaking ties by ascending catalog index.
    pub fn search(
        &self,
        normalized_code: &str,
        limit: usize,
        enable_stem: bool,
        lang_tag: &str,
        bonus_indices: &[i32],
        bonus_scores: &[i32],
    ) -> Vec<i32> {
        if limit == 0 {
            return Vec::new();
        }
        let spaced = normalized_code.replace('_', " ");
        let tokens: Vec<&str> = spaced.split_whitespace().collect();
        if tokens.is_empty() {
            return Vec::new();
        }

        let mut scores: HashMap<i32, i32> = HashMap::new();
        for (&idx, &bonus) in bonus_indices.iter().zip(bonus_scores.iter()) {
            *scores.entry(idx).or_insert(0) += bonus;
        }

        let stem_idx = if enable_stem {
            Some(self.get_or_build_stem_index(lang_tag))
        } else {
            None
        };
        let ctx = if enable_stem {
            let tag = if lang_tag.is_empty() { "en" } else { lang_tag };
            Some(Context::of_tag(tag))
        } else {
            None
        };
        let mut u16_in = Vec::with_capacity(32);
        let mut u16_out = Vec::with_capacity(32);

        for token in tokens {
            self.score_term(token, 100, &mut scores);

            if let (Some(si), Some(c)) = (stem_idx, ctx.as_ref()) {
                let ts = Self::stem_str(c, token, &mut u16_in, &mut u16_out);
                self.score_stem(token, &ts, si, &mut scores);
            }

            for &syn in synonyms_for(token) {
                self.score_term(syn, 60, &mut scores);
            }

            let char_len = token.chars().count();
            if char_len >= 2 {
                for t in self.prefix_range(token) {
                    if self.terms[t].len() > token.len() {
                        for &entry_idx in self.postings_of(t) {
                            *scores.entry(entry_idx).or_insert(0) += 40;
                        }
                    }
                }
            }

            if char_len >= 4 {
                let token_chars: Vec<char> = token.chars().collect();
                for t in 0..self.terms.len() {
                    if is_one_edit_away_chars(&token_chars, &self.terms[t]) {
                        for &entry_idx in self.postings_of(t) {
                            *scores.entry(entry_idx).or_insert(0) += 30;
                        }
                    }
                }
            }
        }

        let mut ranked: Vec<(i32, i32)> = scores.into_iter().collect();
        ranked.sort_unstable_by(|a, b| b.1.cmp(&a.1).then_with(|| a.0.cmp(&b.0)));
        ranked.truncate(limit);
        ranked.into_iter().map(|(idx, _)| idx).collect()
    }

    /// Returns query completion words guaranteed to match at least one emoji.
    pub fn completions(
        &self,
        typed: &str,
        context: &[String],
        extra_shortcodes: &[String],
        limit: usize,
    ) -> Vec<String> {
        let prefix = typed.trim().to_lowercase();
        if prefix.is_empty() || limit == 0 {
            return Vec::new();
        }

        let mut already: HashSet<String> = context
            .iter()
            .map(|c| c.trim().to_lowercase())
            .filter(|c| !c.is_empty())
            .collect();
        already.insert(prefix.clone());

        let mut narrowed: HashSet<i32> = HashSet::new();
        for token in &already {
            if token == &prefix {
                continue;
            }
            if let Some(t) = self.term_index(token) {
                for &idx in self.postings_of(t) {
                    narrowed.insert(idx);
                }
            }
        }

        let mut reach: HashMap<String, usize> = HashMap::new();
        let mut shared: HashMap<String, usize> = HashMap::new();

        for t in self.prefix_range(&prefix) {
            let kw = &self.terms[t];
            if kw.len() <= prefix.len() || already.contains(kw) {
                continue;
            }
            let plist = self.postings_of(t);
            reach.insert(kw.clone(), plist.len());
            if !narrowed.is_empty() {
                let cnt = plist.iter().filter(|idx| narrowed.contains(idx)).count();
                shared.insert(kw.clone(), cnt);
            }
        }

        for code in extra_shortcodes {
            if !code.contains('_') && !already.contains(code) {
                reach.entry(code.clone()).or_insert(1);
            }
        }

        for &(word, expansions) in SYNONYMS {
            if word.len() <= prefix.len() || !word.starts_with(&prefix) {
                continue;
            }
            if already.contains(word) || reach.contains_key(word) {
                continue;
            }
            let expanded: usize = expansions
                .iter()
                .filter_map(|e| self.term_index(e))
                .map(|t| self.postings_of(t).len())
                .sum();
            if expanded > 0 {
                reach.insert(word.to_owned(), expanded);
            }
        }

        let mut ranked: Vec<(String, usize, usize)> = reach
            .into_iter()
            .map(|(k, r)| {
                let s = shared.get(&k).copied().unwrap_or(0);
                (k, s, r)
            })
            .collect();

        ranked.sort_unstable_by(|a, b| {
            b.1.cmp(&a.1)
                .then_with(|| b.2.cmp(&a.2))
                .then_with(|| a.0.chars().count().cmp(&b.0.chars().count()))
                .then_with(|| a.0.cmp(&b.0))
        });
        ranked.truncate(limit);
        ranked.into_iter().map(|(k, _, _)| k).collect()
    }
}

/// Damerau-Levenshtein distance == 1 over Unicode scalar values.
fn is_one_edit_away_chars(a: &[char], b_str: &str) -> bool {
    // Fast UTF-8 byte-length prefilter before decoding `b_str` into chars.
    let a_len = a.len();
    let b_bytes = b_str.len();
    if b_bytes + 4 < a_len || b_bytes > a_len * 4 + 4 {
        return false;
    }

    let b: Vec<char> = b_str.chars().collect();
    let b_len = b.len();
    let diff = a_len as isize - b_len as isize;
    if diff < -1 || diff > 1 {
        return false;
    }

    if diff == 0 {
        let mut mismatches = 0usize;
        let mut swap_possible = false;
        for i in 0..a_len {
            if a[i] != b[i] {
                mismatches += 1;
                if mismatches == 2 {
                    swap_possible = a[i] == b[i - 1] && a[i - 1] == b[i];
                }
                if mismatches > 2 {
                    return false;
                }
            }
        }
        return mismatches == 1 || (mismatches == 2 && swap_possible);
    }

    let (short, long) = if a_len < b_len { (a, b.as_slice()) } else { (b.as_slice(), a) };
    let mut i = 0;
    let mut j = 0;
    let mut skipped = false;
    while i < short.len() && j < long.len() {
        if short[i] == long[j] {
            i += 1;
            j += 1;
        } else {
            if skipped {
                return false;
            }
            skipped = true;
            j += 1;
        }
    }
    true
}

#[cfg(test)]
mod tests {
    use super::*;

    fn sample_index() -> EmojiSearchIndex {
        let entries = vec![
            "smile\x1fhappy\x1fjoy".to_string(),
            "heart\x1fred heart\x1flove".to_string(),
            "fire\x1fflame".to_string(),
            "headphone\x1fmusic".to_string(),
            "cat\x1fpet".to_string(),
        ];
        EmojiSearchIndex::new(&entries)
    }

    #[test]
    fn exact_fuzzy_and_multilingual_synonyms() {
        let idx = sample_index();
        // Exact match
        assert_eq!(idx.search("fire", 5, true, "en", &[], &[])[0], 2);
        // Fuzzy typo ("hapy" -> "happy")
        assert_eq!(idx.search("hapy", 5, true, "en", &[], &[])[0], 0);
        // Arabic synonym ("حب" -> "love"/"heart")
        assert_eq!(idx.search("حب", 5, true, "ar", &[], &[])[0], 1);
        // Spanish synonym ("fuego" -> "fire")
        assert_eq!(idx.search("fuego", 5, true, "es", &[], &[])[0], 2);
        // Stemming ("cats" -> "cat")
        assert_eq!(idx.search("cats", 5, true, "en", &[], &[])[0], 4);
    }

    #[test]
    fn completions_respect_context() {
        let idx = sample_index();
        let res = idx.completions("hea", &["red".to_string()], &[], 4);
        assert_eq!(res.first().map(String::as_str), Some("heart"));
    }
}
