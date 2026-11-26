//! High-speed zero-regex clipboard entity scanner (ported from Kotlin `ClipEntities`).
//!
//! Extracts URLs, email addresses, one-time verification codes (English, Arabic,
//! Spanish, plus custom user keywords), and diallable phone number candidates
//! directly in Rust with exact UTF-16 offsets for Android/JVM strings.

use std::sync::{Mutex, OnceLock};

const MAX_SCANNED_CHARS_UTF16: usize = 4000;
const OTP_KEYWORD_RANGE_UTF16: usize = 40;

const OTP_KEYWORD_DENY: &[&str] = &[
    "zip", "postal", "post", "area", "country", "dial", "colour", "color",
    "hex", "sort", "swift", "ifsc", "iban", "bar", "qr", "source", "error",
];

/// English word-bounded OTP trigger phrases (longest first).
const LATIN_OTP_KEYWORDS: &[&str] = &[
    "one-time password", "one time password", "onetime password",
    "one-time passcode", "one time passcode", "onetime passcode",
    "one-time code", "one time code", "onetime code",
    "authentication code", "security code", "verification",
    "confirmation", "access code", "login code", "auth code",
    "pass code", "passcode", "one-time", "one time", "onetime",
    "verify", "code", "otp", "pin", "2fa",
];

/// Arabic and Spanish OTP keywords (matched by containment, case-insensitive).
const MULTILINGUAL_OTP_KEYWORDS: &[&str] = &[
    "رمز التحقق", "كود التحقق", "رمز التفعيل", "كود التفعيل",
    "رمز التأكيد", "كود التأكيد", "رمز الدخول", "الرمز السري",
    "كلمة المرور المؤقتة", "كود الأمان", "رمز الأمان",
    "código de verificación", "codigo de verificacion",
    "código de seguridad", "codigo de seguridad",
    "código de acceso", "codigo de acceso",
    "código de confirmación", "codigo de confirmacion",
    "clave temporal", "código", "codigo",
];

static USER_OTP_KEYWORDS: OnceLock<Mutex<Vec<String>>> = OnceLock::new();

fn user_keywords_store() -> &'static Mutex<Vec<String>> {
    USER_OTP_KEYWORDS.get_or_init(|| Mutex::new(Vec::new()))
}

pub fn set_user_otp_keywords(words: &[String]) {
    let mut kept: Vec<String> = Vec::new();
    for w in words {
        let trimmed = w.trim().to_lowercase();
        if !trimmed.is_empty() && !kept.contains(&trimmed) {
            kept.push(trimmed);
        }
    }
    kept.sort_by(|a, b| b.len().cmp(&a.len()));
    if let Ok(mut guard) = user_keywords_store().lock() {
        *guard = kept;
    }
}

pub fn parse_otp_keywords(raw: &str) -> Vec<String> {
    raw.split(|c| matches!(c, ',' | '،' | '，' | '、' | ';' | '\n'))
        .map(|s| s.trim())
        .filter(|s| !s.is_empty())
        .map(|s| s.to_owned())
        .collect()
}

/// Maps every UTF-8 byte index (0..=text.len()) to its UTF-16 code-unit offset.
fn build_byte_to_utf16(text: &str) -> Vec<usize> {
    let mut map = vec![0usize; text.len() + 1];
    let mut u16_pos = 0usize;
    for (byte_idx, ch) in text.char_indices() {
        map[byte_idx] = u16_pos;
        u16_pos += ch.len_utf16();
    }
    map[text.len()] = u16_pos;
    map
}

fn utf16_to_byte(text: &str, target_u16: usize) -> usize {
    let mut u16_pos = 0usize;
    for (byte_idx, ch) in text.char_indices() {
        if u16_pos >= target_u16 {
            return byte_idx;
        }
        u16_pos += ch.len_utf16();
    }
    text.len()
}

fn truncate_utf16(text: &str, max_u16: usize) -> &str {
    let mut u16_pos = 0usize;
    for (byte_idx, ch) in text.char_indices() {
        let next = u16_pos + ch.len_utf16();
        if next > max_u16 {
            return &text[..byte_idx];
        }
        u16_pos = next;
    }
    text
}

/// Drops trailing sentence punctuation and unmatched closing brackets from a URL.
pub fn trim_url_tail(raw: &str) -> &str {
    let mut url = raw.trim_end_matches(|c| {
        matches!(c, '.' | ',' | ';' | ':' | '!' | '?' | '\'' | '"' | '”' | '’')
    });
    while let Some(last) = url.chars().last() {
        let open = match last {
            ')' => '(',
            ']' => '[',
            '}' => '{',
            _ => break,
        };
        let open_count = url.chars().filter(|&c| c == open).count();
        let close_count = url.chars().filter(|&c| c == last).count();
        if open_count >= close_count {
            break;
        }
        url = &url[..url.len() - last.len_utf8()];
        url = url.trim_end_matches(|c| matches!(c, '.' | ',' | ';' | ':' | '!' | '?'));
    }
    url
}

#[inline]
fn is_ascii_word(b: u8) -> bool {
    b.is_ascii_alphanumeric() || b == b'_'
}

#[inline]
fn is_unicode_word_char(c: char) -> bool {
    c.is_alphanumeric() || c == '_'
}

fn prev_char(s: &str, byte_idx: usize) -> Option<char> {
    s[..byte_idx].chars().next_back()
}

fn next_char(s: &str, byte_idx: usize) -> Option<char> {
    s[byte_idx..].chars().next()
}

/// Checks if the word immediately preceding `start_utf16` disqualifies a built-in OTP keyword.
pub fn is_denied_otp_keyword(text: &str, start_utf16: usize) -> bool {
    let byte_pos = utf16_to_byte(text, start_utf16);
    let before = text[..byte_pos].trim_end();
    let word_rev: String = before.chars().rev().take_while(|c| c.is_alphabetic()).collect();
    if word_rev.is_empty() {
        return false;
    }
    let word: String = word_rev.chars().rev().collect::<String>().to_lowercase();
    OTP_KEYWORD_DENY.contains(&word.as_str())
}

/// Scans `text` for scheme/www URLs, returning `(start_utf16, end_utf16, raw_match)`.
fn find_scheme_urls(text: &str, b2u16: &[usize]) -> Vec<(usize, usize, String)> {
    let bytes = text.as_bytes();
    let n = bytes.len();
    let mut out = Vec::new();
    let mut i = 0usize;

    while i < n {
        let prefix_len = if i + 8 <= n && bytes[i..i + 8].eq_ignore_ascii_case(b"https://") {
            8
        } else if i + 7 <= n && bytes[i..i + 7].eq_ignore_ascii_case(b"http://") {
            7
        } else if i + 4 <= n && bytes[i..i + 4].eq_ignore_ascii_case(b"www.") {
            4
        } else {
            i += 1;
            continue;
        };

        let mut end = i + prefix_len;
        while end < n {
            let ch = text[end..].chars().next().unwrap();
            if ch.is_whitespace() || matches!(ch, '<' | '>' | '"' | '\'' | '`') {
                break;
            }
            end += ch.len_utf8();
        }

        if end - (i + prefix_len) >= 2 {
            out.push((b2u16[i], b2u16[end], text[i..end].to_owned()));
            i = end;
        } else {
            i += 1;
        }
    }
    out
}

/// Scans ASCII email addresses matching `ClipEntities.EMAIL`.
fn find_emails(text: &str, b2u16: &[usize]) -> Vec<(usize, usize, String)> {
    let bytes = text.as_bytes();
    let n = bytes.len();
    let mut out = Vec::new();
    let mut pos = 0usize;

    while pos < n {
        let Some(rel_at) = bytes[pos..].iter().position(|&b| b == b'@') else {
            break;
        };
        let at = pos + rel_at;

        // Walk backward for local part
        let mut lstart = at;
        while lstart > 0 {
            let b = bytes[lstart - 1];
            if b.is_ascii_alphanumeric() || matches!(b, b'.' | b'_' | b'%' | b'+' | b'-') {
                lstart -= 1;
            } else {
                break;
            }
        }
        // Trim leading non-alphanumeric so first char is [A-Za-z0-9]
        while lstart < at && !bytes[lstart].is_ascii_alphanumeric() {
            lstart += 1;
        }
        let local_len = at - lstart;
        if local_len == 0 || local_len > 64 {
            pos = at + 1;
            continue;
        }
        // Negative lookbehind (?<![\w.%+-])
        if let Some(pc) = prev_char(text, lstart) {
            if is_unicode_word_char(pc) || matches!(pc, '.' | '%' | '+' | '-') {
                pos = at + 1;
                continue;
            }
        }

        // Walk forward for domain labels
        let mut dend = at + 1;
        let mut label_start = dend;
        let mut valid_domain = true;
        let mut dots = 0usize;
        let mut last_label_start = dend;
        while dend < n {
            let b = bytes[dend];
            if b.is_ascii_alphanumeric() || b == b'-' {
                dend += 1;
            } else if b == b'.' {
                let llen = dend - label_start;
                if llen == 0
                    || llen > 64
                    || !bytes[label_start].is_ascii_alphanumeric()
                    || !bytes[dend - 1].is_ascii_alphanumeric()
                {
                    valid_domain = false;
                    break;
                }
                dots += 1;
                dend += 1;
                label_start = dend;
                last_label_start = dend;
            } else {
                break;
            }
        }
        if valid_domain && dots >= 1 {
            let tld_len = dend - last_label_start;
            let tld_ok = (2..=24).contains(&tld_len)
                && bytes[last_label_start..dend].iter().all(|b| b.is_ascii_alphabetic());
            let after_ok = match next_char(text, dend) {
                Some(nc) => !(is_unicode_word_char(nc) || nc == '-'),
                None => true,
            };
            if tld_ok && after_ok {
                out.push((b2u16[lstart], b2u16[dend], text[lstart..dend].to_owned()));
                pos = dend;
                continue;
            }
        }
        pos = at + 1;
    }
    out
}

pub fn find_first_email(text: &str) -> Option<String> {
    let prefix = truncate_utf16(text, MAX_SCANNED_CHARS_UTF16);
    let b2u16 = build_byte_to_utf16(prefix);
    find_emails(prefix, &b2u16).into_iter().next().map(|(_, _, v)| v)
}

fn is_code_shaped(token: &str) -> bool {
    let body = token.rsplit('-').next().unwrap_or(token);
    let mut digits = 0usize;
    let mut has_letter = false;
    for b in body.bytes() {
        if b.is_ascii_digit() {
            digits += 1;
        } else if b.is_ascii_alphabetic() {
            has_letter = true;
        }
    }
    if digits == 0 {
        return false;
    }
    if has_letter && digits < 2 {
        return false;
    }
    if body.len() == 4 && digits == 4 {
        if let Ok(year) = body.parse::<u32>() {
            if (1900..=2099).contains(&year) {
                return false;
            }
        }
    }
    true
}

/// Finds OTP code spans anchored to English, Arabic, Spanish, or user keywords.
fn find_otp_tokens(text: &str, b2u16: &[usize]) -> Vec<(usize, usize, String)> {
    // Build lowercase mapping preserving UTF-16 positions per char
    let lower = text.to_lowercase();
    let lower_b2u16 = build_byte_to_utf16(&lower);
    let mut keywords: Vec<(usize, usize)> = Vec::new();

    // 1. English word-bounded keywords
    for &kw in LATIN_OTP_KEYWORDS {
        let mut search_from = 0usize;
        while let Some(rel) = lower[search_from..].find(kw) {
            let start = search_from + rel;
            let end = start + kw.len();
            let before_ok = prev_char(&lower, start).map_or(true, |c| !is_unicode_word_char(c));
            let after_ok = next_char(&lower, end).map_or(true, |c| !is_unicode_word_char(c));
            if before_ok && after_ok {
                let u16_s = lower_b2u16[start];
                let u16_e = lower_b2u16[end];
                if !is_denied_otp_keyword(text, u16_s) {
                    keywords.push((u16_s, u16_e));
                }
            }
            search_from = end;
        }
    }

    // 2. Arabic / Spanish keywords
    for &kw in MULTILINGUAL_OTP_KEYWORDS {
        let mut search_from = 0usize;
        while let Some(rel) = lower[search_from..].find(kw) {
            let start = search_from + rel;
            let end = start + kw.len();
            let u16_s = lower_b2u16[start];
            let u16_e = lower_b2u16[end];
            if !is_denied_otp_keyword(text, u16_s) {
                keywords.push((u16_s, u16_e));
            }
            search_from = end;
        }
    }

    // 3. User-defined OTP keywords (skip deny list)
    if let Ok(guard) = user_keywords_store().lock() {
        for kw in guard.iter() {
            let mut search_from = 0usize;
            while let Some(rel) = lower[search_from..].find(kw.as_str()) {
                let start = search_from + rel;
                let end = start + kw.len();
                keywords.push((lower_b2u16[start], lower_b2u16[end]));
                search_from = end;
            }
        }
    }

    if keywords.is_empty() {
        return Vec::new();
    }

    // Scan OTP candidate tokens: (?<![A-Za-z0-9_-])(?<!\d[.,])(?:[A-Za-z]-)?[A-Z0-9]{4,8}(?![A-Za-z0-9_-])(?![.,]\d)
    let bytes = text.as_bytes();
    let n = bytes.len();
    let mut out = Vec::new();
    let mut i = 0usize;

    while i < n {
        let b = bytes[i];
        if !(b.is_ascii_alphanumeric()) {
            i += 1;
            continue;
        }
        // Check left boundary: not preceded by [A-Za-z0-9_-]
        if i > 0 && (is_ascii_word(bytes[i - 1]) || bytes[i - 1] == b'-') {
            i += 1;
            continue;
        }
        // Not preceded by \d[.,]
        if i >= 2 && bytes[i - 2].is_ascii_digit() && matches!(bytes[i - 1], b'.' | b',') {
            i += 1;
            continue;
        }

        let token_start = i;
        let mut body_start = i;
        if b.is_ascii_alphabetic() && i + 1 < n && bytes[i + 1] == b'-' {
            body_start = i + 2;
        }
        let mut end = body_start;
        while end < n && (bytes[end].is_ascii_uppercase() || bytes[end].is_ascii_digit()) {
            end += 1;
        }
        let body_len = end - body_start;
        if (4..=8).contains(&body_len) {
            let right_ok = end == n || !(is_ascii_word(bytes[end]) || bytes[end] == b'-');
            let not_decimal = !(end + 1 < n
                && matches!(bytes[end], b'.' | b',')
                && bytes[end + 1].is_ascii_digit());
            if right_ok && not_decimal {
                let raw_tok = &text[token_start..end];
                if is_code_shaped(raw_tok) {
                    let u16_s = b2u16[token_start];
                    let u16_e = b2u16[end];
                    let near = keywords.iter().any(|&(ks, ke)| {
                        let gap = if u16_s >= ke {
                            u16_s - ke
                        } else if u16_e <= ks {
                            ks - u16_e
                        } else {
                            return false;
                        };
                        gap <= OTP_KEYWORD_RANGE_UTF16
                    });
                    if near {
                        let body = &text[body_start..end];
                        out.push((b2u16[body_start], u16_e, body.to_owned()));
                    }
                }
                i = end;
                continue;
            }
        }
        i += 1;
    }

    out
}

/// Checks `yyyy-m-d` or `d-m-yy(yy)` date impostors.
fn is_date_impostor(raw: &str) -> bool {
    let trimmed = raw.trim();
    let parts: Vec<&str> = trimmed.split(|c| matches!(c, '-' | '/' | '.')).collect();
    if parts.len() != 3 || !parts.iter().all(|p| !p.is_empty() && p.bytes().all(|b| b.is_ascii_digit())) {
        return false;
    }
    let (l0, l1, l2) = (parts[0].len(), parts[1].len(), parts[2].len());
    (l0 == 4 && (1..=2).contains(&l1) && (1..=2).contains(&l2))
        || ((1..=2).contains(&l0) && (1..=2).contains(&l1) && (2..=4).contains(&l2))
}

/// Scans phone number candidates matching `ClipEntities.PHONE` and filters date/currency impostors.
fn find_phone_candidates(text: &str, b2u16: &[usize]) -> Vec<(usize, usize, String)> {
    let bytes = text.as_bytes();
    let n = bytes.len();
    let mut out = Vec::new();
    let mut i = 0usize;

    while i < n {
        let is_plus = bytes[i] == b'+' && i + 1 < n && bytes[i + 1].is_ascii_digit();
        let is_digit = bytes[i].is_ascii_digit();
        if !is_plus && !is_digit {
            i += 1;
            continue;
        }
        // Negative lookbehind (?<![\w+])
        if let Some(pc) = prev_char(text, i) {
            if is_unicode_word_char(pc) || pc == '+' || matches!(pc, '#' | '$' | '£' | '€') {
                i += 1;
                continue;
            }
        }

        let start = i;
        let first_digit = if is_plus { i + 1 } else { i };
        // Match up to 19 chars after first_digit where middle chars are [\d ().\-] and end is \d
        let mut j = first_digit + 1;
        let max_end = (first_digit + 20).min(n);
        let mut best_end = None;
        while j < max_end {
            let b = bytes[j];
            if b.is_ascii_digit() {
                if j - first_digit >= 6 {
                    let after_ok = next_char(text, j + 1).map_or(true, |c| !is_unicode_word_char(c));
                    if after_ok {
                        best_end = Some(j + 1);
                    }
                }
                j += 1;
            } else if matches!(b, b' ' | b'(' | b')' | b'.' | b'-') {
                j += 1;
            } else {
                break;
            }
        }

        if let Some(end) = best_end {
            let candidate = &text[start..end];
            if !is_date_impostor(candidate) {
                out.push((b2u16[start], b2u16[end], candidate.to_owned()));
                i = end;
                continue;
            }
        }
        i += 1;
    }
    out
}

/// Runs all native scanners in a single pass and packs findings as `KIND\tSTART\tEND\tVALUE`.
pub fn scan_text_packed(text: &str) -> Vec<String> {
    let b2u16 = build_byte_to_utf16(text);
    let mut out = Vec::new();

    for (s, e, v) in find_scheme_urls(text, &b2u16) {
        out.push(format!("URL\t{s}\t{e}\t{v}"));
    }
    for (s, e, v) in find_emails(text, &b2u16) {
        out.push(format!("EMAIL\t{s}\t{e}\t{v}"));
    }
    for (s, e, v) in find_otp_tokens(text, &b2u16) {
        out.push(format!("OTP\t{s}\t{e}\t{v}"));
    }
    for (s, e, v) in find_phone_candidates(text, &b2u16) {
        out.push(format!("PHONE\t{s}\t{e}\t{v}"));
    }
    out
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn extracts_arabic_spanish_and_english_otps_with_utf16_offsets() {
        let ar = "مرحباً! رمز التحقق الخاص بك هو 482913 صالح لدقائق";
        let hits = scan_text_packed(ar);
        assert!(hits.iter().any(|h| h.starts_with("OTP\t") && h.ends_with("\t482913")));
        let otp_line = hits.iter().find(|h| h.starts_with("OTP\t")).unwrap();
        let parts: Vec<&str> = otp_line.split('\t').collect();
        let s: usize = parts[1].parse().unwrap();
        let e: usize = parts[2].parse().unwrap();
        let u16s: Vec<u16> = ar.encode_utf16().collect();
        assert_eq!(String::from_utf16_lossy(&u16s[s..e]), "482913");

        let es = "Tu código de verificación es G-739104.";
        let hits_es = scan_text_packed(es);
        assert!(hits_es.iter().any(|h| h.starts_with("OTP\t") && h.ends_with("\t739104")));
    }

    #[test]
    fn extracts_email_url_and_rejects_date_impostor() {
        let txt = "Visit https://example.com/wiki/Foo_(bar). Contact me at ahmad@test.org on 2026-11-26 or +49 170 1234567";
        assert_eq!(find_first_email(txt).as_deref(), Some("ahmad@test.org"));
        assert_eq!(trim_url_tail("https://example.com/wiki/Foo_(bar)."), "https://example.com/wiki/Foo_(bar)");
        let hits = scan_text_packed(txt);
        assert!(hits.iter().any(|h| h.starts_with("URL\t")));
        assert!(hits.iter().any(|h| h.starts_with("EMAIL\t") && h.ends_with("\tahmad@test.org")));
        assert!(hits.iter().any(|h| h.starts_with("PHONE\t") && h.ends_with("\t+49 170 1234567")));
        assert!(!hits.iter().any(|h| h.contains("2026-11-26")));
    }
}
