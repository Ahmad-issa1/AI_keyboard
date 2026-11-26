//! High-precision word -> emoji prediction engine (ported from Kotlin `EmojiSuggester`).
//!
//! Stores both the catalog keyword table and curated triggers for English, Arabic,
//! and Spanish natively in Rust memory.

use std::collections::{HashMap, HashSet};

const CURATED_TRIGGERS: &[(&str, &[&str])] = &[
    // English triggers
    ("birthday", &["🎂", "🎉", "🥳", "🎁"]),
    ("coffee", &["☕", "😊"]),
    ("tea", &["🍵", "☕"]),
    ("party", &["🥳", "🎉", "🪩"]),
    ("congrats", &["🎉", "👏", "🥳"]),
    ("congratulations", &["🎉", "👏", "🥳"]),
    ("thanks", &["🙏", "😊", "❤️"]),
    ("thank", &["🙏", "😊"]),
    ("love", &["❤️", "😍", "🥰"]),
    ("haha", &["😂", "🤣"]),
    ("lol", &["😂", "🤣"]),
    ("wow", &["😮", "🤩"]),
    ("sad", &["😢", "😞", "💔"]),
    ("cry", &["😭", "😢"]),
    ("angry", &["😠", "😡"]),
    ("sorry", &["😔", "🙏"]),
    ("please", &["🙏", "🥺"]),
    ("good", &["👍", "😊"]),
    ("great", &["👏", "🔥", "💯"]),
    ("night", &["🌙", "😴", "✨"]),
    ("morning", &["🌅", "☀️", "☕"]),
    ("sleep", &["😴", "💤", "🛌"]),
    ("fire", &["🔥"]),
    ("hot", &["🥵", "🔥", "☀️"]),
    ("cold", &["🥶", "❄️"]),
    ("rain", &["🌧️", "☔", "⛈️"]),
    ("sun", &["☀️", "😎"]),
    ("food", &["🍽️", "😋"]),
    ("hungry", &["😋", "🍽️"]),
    ("pizza", &["🍕"]),
    ("cake", &["🍰", "🎂"]),
    ("music", &["🎵", "🎶", "🎧"]),
    ("football", &["⚽", "🏈"]),
    ("win", &["🏆", "🥇", "🎉"]),
    ("money", &["💰", "💸"]),
    ("work", &["💼", "💻"]),
    ("home", &["🏠", "🏡"]),
    ("car", &["🚗"]),
    ("flight", &["✈️", "🛫"]),
    ("beach", &["🏖️", "🌊"]),
    ("book", &["📚", "📖"]),
    ("idea", &["💡", "🤔"]),
    ("ok", &["👌", "👍"]),
    ("yes", &["✅", "👍"]),
    ("no", &["❌", "🙅"]),
    ("wedding", &["💍", "👰", "🤵", "💒"]),
    ("baby", &["👶", "🍼"]),
    ("eid", &["🌙", "🕌", "✨"]),
    ("ramadan", &["🌙", "🕌", "🤲"]),
    ("iftar", &["🌙", "🍽️", "🕌"]),
    ("prayer", &["🤲", "🙏", "🕌"]),
    // Arabic triggers
    ("ميلاد", &["🎂", "🎉", "🥳", "🎁"]),
    ("قهوة", &["☕", "😊"]),
    ("شاي", &["🍵", "☕"]),
    ("شكرا", &["🙏", "😊", "❤️"]),
    ("شكراً", &["🙏", "😊", "❤️"]),
    ("مبروك", &["🎉", "👏", "🥳"]),
    ("أحبك", &["❤️", "😍", "🥰"]),
    ("احبك", &["❤️", "😍", "🥰"]),
    ("حب", &["❤️", "😍", "🥰"]),
    ("حزين", &["😢", "😞", "💔"]),
    ("غاضب", &["😠", "😡"]),
    ("آسف", &["😔", "🙏"]),
    ("اسف", &["😔", "🙏"]),
    ("صباح", &["🌅", "☀️", "☕"]),
    ("مساء", &["🌙", "✨"]),
    ("نوم", &["😴", "💤"]),
    ("نار", &["🔥"]),
    ("مطر", &["🌧️", "☔"]),
    ("شمس", &["☀️", "😎"]),
    ("طعام", &["🍽️", "😋"]),
    ("أكل", &["🍽️", "😋"]),
    ("بيتزا", &["🍕"]),
    ("كيك", &["🍰", "🎂"]),
    ("موسيقى", &["🎵", "🎶", "🎧"]),
    ("فوز", &["🏆", "🥇", "🎉"]),
    ("عمل", &["💼", "💻"]),
    ("بيت", &["🏠", "🏡"]),
    ("سيارة", &["🚗"]),
    ("سفر", &["✈️", "🛫"]),
    ("كتاب", &["📚", "📖"]),
    ("فكرة", &["💡", "🤔"]),
    ("رمضان", &["🌙", "🕌", "🤲"]),
    ("عيد", &["🌙", "🕌", "✨"]),
    ("إفطار", &["🌙", "🍽️", "🕌"]),
    ("دعاء", &["🤲", "🙏", "🕌"]),
    // Spanish triggers
    ("cumpleaños", &["🎂", "🎉", "🥳", "🎁"]),
    ("café", &["☕", "😊"]),
    ("cafe", &["☕", "😊"]),
    ("gracias", &["🙏", "😊", "❤️"]),
    ("amor", &["❤️", "😍", "🥰"]),
    ("fiesta", &["🥳", "🎉", "🪩"]),
    ("felicidades", &["🎉", "👏", "🥳"]),
    ("triste", &["😢", "😞", "💔"]),
    ("perdón", &["😔", "🙏"]),
    ("fuego", &["🔥"]),
    ("lluvia", &["🌧️", "☔"]),
    ("sol", &["☀️", "😎"]),
    ("comida", &["🍽️", "😋"]),
    ("música", &["🎵", "🎶", "🎧"]),
    ("fútbol", &["⚽"]),
    ("trabajo", &["💼", "💻"]),
    ("casa", &["🏠", "🏡"]),
    ("libro", &["📚", "📖"]),
    ("idea", &["💡", "🤔"]),
];

fn curated_for(word: &str) -> &'static [&'static str] {
    for &(k, v) in CURATED_TRIGGERS {
        if k == word {
            return v;
        }
    }
    &[]
}

pub struct EmojiSuggesterIndex {
    by_keyword: HashMap<String, Vec<String>>,
}

impl EmojiSuggesterIndex {
    /// Builds keyword -> emoji map from parallel slices of base emojis and
    /// `\x1f`-separated keyword strings.
    pub fn new(emojis: &[String], keywords_per_entry: &[String]) -> Self {
        let mut by_keyword: HashMap<String, Vec<String>> = HashMap::new();
        for (emoji, packed_kw) in emojis.iter().zip(keywords_per_entry.iter()) {
            if emoji.is_empty() {
                continue;
            }
            for kw in packed_kw.split('\x1f') {
                if !kw.is_empty() {
                    by_keyword.entry(kw.to_owned()).or_default().push(emoji.clone());
                }
            }
        }
        Self { by_keyword }
    }

    pub fn suggest(
        &self,
        raw_word: &str,
        phrase_hit: &str,
        extra_exact: &[String],
        extra_singular: &[String],
        limit: usize,
    ) -> Vec<String> {
        let cleaned = raw_word.trim().to_lowercase();
        if cleaned.is_empty() || limit == 0 {
            return Vec::new();
        }
        let char_count = cleaned.chars().count();
        if char_count < 2 {
            return Vec::new();
        }

        let mut results: Vec<String> = Vec::with_capacity(limit);
        let mut seen: HashSet<String> = HashSet::with_capacity(limit * 2);

        let push_unique = |e: &str, results: &mut Vec<String>, seen: &mut HashSet<String>| {
            if results.len() < limit && !e.is_empty() && seen.insert(e.to_owned()) {
                results.push(e.to_owned());
            }
        };

        if !phrase_hit.is_empty() {
            push_unique(phrase_hit, &mut results, &mut seen);
        }

        for &e in curated_for(&cleaned) {
            push_unique(e, &mut results, &mut seen);
        }
        for e in extra_exact {
            push_unique(e, &mut results, &mut seen);
        }
        if let Some(list) = self.by_keyword.get(&cleaned) {
            for e in list {
                push_unique(e, &mut results, &mut seen);
            }
        }

        if results.len() < limit && char_count >= 4 && cleaned.ends_with('s') {
            let singular = &cleaned[..cleaned.len() - 1];
            for &e in curated_for(singular) {
                push_unique(e, &mut results, &mut seen);
            }
            for e in extra_singular {
                push_unique(e, &mut results, &mut seen);
            }
            if let Some(list) = self.by_keyword.get(singular) {
                for e in list {
                    push_unique(e, &mut results, &mut seen);
                }
            }
        }

        results
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn suggests_english_arabic_spanish_and_plurals() {
        let idx = EmojiSuggesterIndex::new(
            &["🐱".to_string(), "☕".to_string()],
            &["cat".to_string(), "coffee".to_string()],
        );
        assert_eq!(idx.suggest("قهوة", "", &[], &[], 4)[0], "☕");
        assert_eq!(idx.suggest("gracias", "", &[], &[], 4)[0], "🙏");
        assert_eq!(idx.suggest("cats", "", &[], &[], 4), vec!["🐱"]);
        assert_eq!(idx.suggest("clock", "⏰", &[], &[], 4)[0], "⏰");
    }
}
