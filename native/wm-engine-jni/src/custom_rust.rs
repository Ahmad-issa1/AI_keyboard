//! JNI bridge for our custom Rust engines:
//! - `EmojiSearchIndex` (`wm_engine::emoji_search`)
//! - `EmojiSuggesterIndex` (`wm_engine::emoji_suggester`)
//! - `ClipEntities` scanner (`wm_engine::clip_entities`)
//!
//! Exposed on `com.wasimaster.wmkeyboard.core.engine.CustomRustNative`.

use std::collections::VecDeque;
use std::ptr::null_mut;
use std::sync::atomic::{AtomicI64, Ordering};
use std::sync::{Arc, Mutex, OnceLock};

use jni::objects::{JClass, JIntArray, JObjectArray, JString};
use jni::sys::{jboolean, jint, jintArray, jlong, jobjectArray, jsize, jstring, JNI_FALSE, JNI_TRUE};
use jni::JNIEnv;

use wm_engine::clip_entities;
use wm_engine::emoji_search::EmojiSearchIndex;
use wm_engine::emoji_suggester::EmojiSuggesterIndex;

use crate::guard::{guarded, read};

const MAX_LIVE_HANDLES: usize = 8;

struct BoundedStore<T> {
    next_id: AtomicI64,
    items: Mutex<VecDeque<(i64, Arc<T>)>>,
}

impl<T> BoundedStore<T> {
    fn new() -> Self {
        Self {
            next_id: AtomicI64::new(1),
            items: Mutex::new(VecDeque::with_capacity(MAX_LIVE_HANDLES)),
        }
    }

    fn insert(&self, value: T) -> i64 {
        let id = self.next_id.fetch_add(1, Ordering::Relaxed);
        if let Ok(mut guard) = self.items.lock() {
            if guard.len() >= MAX_LIVE_HANDLES {
                guard.pop_front();
            }
            guard.push_back((id, Arc::new(value)));
        }
        id
    }

    fn get(&self, id: i64) -> Option<Arc<T>> {
        let guard = self.items.lock().ok()?;
        guard.iter().find(|(k, _)| *k == id).map(|(_, v)| Arc::clone(v))
    }
}

static EMOJI_SEARCH_STORE: OnceLock<BoundedStore<EmojiSearchIndex>> = OnceLock::new();
static EMOJI_SUGGESTER_STORE: OnceLock<BoundedStore<EmojiSuggesterIndex>> = OnceLock::new();

fn emoji_search_store() -> &'static BoundedStore<EmojiSearchIndex> {
    EMOJI_SEARCH_STORE.get_or_init(BoundedStore::new)
}

fn emoji_suggester_store() -> &'static BoundedStore<EmojiSuggesterIndex> {
    EMOJI_SUGGESTER_STORE.get_or_init(BoundedStore::new)
}

fn read_string_array(env: &mut JNIEnv, arr: &JObjectArray) -> Option<Vec<String>> {
    if arr.is_null() {
        return Some(Vec::new());
    }
    let n = env.get_array_length(arr).ok()?;
    let mut out = Vec::with_capacity(n as usize);
    for i in 0..n {
        let item = JString::from(env.get_object_array_element(arr, i).ok()?);
        if item.is_null() {
            out.push(String::new());
        } else {
            out.push(read(env, &item)?);
        }
        let _ = env.delete_local_ref(item);
    }
    Some(out)
}

fn read_int_array(env: &mut JNIEnv, arr: &JIntArray) -> Option<Vec<i32>> {
    if arr.is_null() {
        return Some(Vec::new());
    }
    let n = env.get_array_length(arr).ok()? as usize;
    let mut buf = vec![0i32; n];
    if n > 0 {
        env.get_int_array_region(arr, 0, &mut buf).ok()?;
    }
    Some(buf)
}

fn write_string_array(env: &mut JNIEnv, items: &[String]) -> Option<jobjectArray> {
    let class = env.find_class("java/lang/String").ok()?;
    let out = env
        .new_object_array(items.len() as jsize, &class, JString::default())
        .ok()?;
    for (i, s) in items.iter().enumerate() {
        let js = env.new_string(s).ok()?;
        env.set_object_array_element(&out, i as jsize, &js).ok()?;
        let _ = env.delete_local_ref(js);
    }
    Some(out.into_raw())
}

fn write_int_array(env: &mut JNIEnv, items: &[i32]) -> Option<jintArray> {
    let arr = env.new_int_array(items.len() as jsize).ok()?;
    if !items.is_empty() {
        env.set_int_array_region(&arr, 0, items).ok()?;
    }
    Some(arr.into_raw())
}

// =========================================================================
// EmojiSearch JNI entry points
// =========================================================================

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeBuildEmojiSearch(
    mut env: JNIEnv,
    _class: JClass,
    keywords_per_entry: JObjectArray,
) -> jlong {
    guarded(&mut env, 0, |env| {
        let kw = read_string_array(env, &keywords_per_entry)?;
        let index = EmojiSearchIndex::new(&kw);
        Some(emoji_search_store().insert(index))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeSearchEmoji(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    normalized_code: JString,
    limit: jint,
    enable_stem: jboolean,
    lang_tag: JString,
    bonus_indices: JIntArray,
    bonus_scores: JIntArray,
) -> jintArray {
    guarded(&mut env, null_mut(), |env| {
        let index = emoji_search_store().get(handle)?;
        let code = read(env, &normalized_code)?;
        let tag = if lang_tag.is_null() {
            String::new()
        } else {
            read(env, &lang_tag)?
        };
        let b_idx = read_int_array(env, &bonus_indices)?;
        let b_scr = read_int_array(env, &bonus_scores)?;
        let lim = limit.max(0) as usize;
        let res = index.search(&code, lim, enable_stem != 0, &tag, &b_idx, &b_scr);
        write_int_array(env, &res)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeEmojiCompletions(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    typed: JString,
    context: JObjectArray,
    extra_shortcodes: JObjectArray,
    limit: jint,
) -> jobjectArray {
    guarded(&mut env, null_mut(), |env| {
        let index = emoji_search_store().get(handle)?;
        let t = read(env, &typed)?;
        let ctx = read_string_array(env, &context)?;
        let extra = read_string_array(env, &extra_shortcodes)?;
        let res = index.completions(&t, &ctx, &extra, limit.max(0) as usize);
        write_string_array(env, &res)
    })
}

// =========================================================================
// EmojiSuggester JNI entry points
// =========================================================================

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeBuildEmojiSuggester(
    mut env: JNIEnv,
    _class: JClass,
    emojis: JObjectArray,
    keywords_per_entry: JObjectArray,
) -> jlong {
    guarded(&mut env, 0, |env| {
        let em = read_string_array(env, &emojis)?;
        let kw = read_string_array(env, &keywords_per_entry)?;
        let index = EmojiSuggesterIndex::new(&em, &kw);
        Some(emoji_suggester_store().insert(index))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeSuggestEmoji(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    word: JString,
    phrase_hit: JString,
    extra_exact: JObjectArray,
    extra_singular: JObjectArray,
    limit: jint,
) -> jobjectArray {
    guarded(&mut env, null_mut(), |env| {
        let index = emoji_suggester_store().get(handle)?;
        let w = read(env, &word)?;
        let ph = if phrase_hit.is_null() {
            String::new()
        } else {
            read(env, &phrase_hit)?
        };
        let ex = read_string_array(env, &extra_exact)?;
        let sg = read_string_array(env, &extra_singular)?;
        let res = index.suggest(&w, &ph, &ex, &sg, limit.max(0) as usize);
        write_string_array(env, &res)
    })
}

// =========================================================================
// ClipEntities JNI entry points
// =========================================================================

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeClipFindFirstEmail(
    mut env: JNIEnv,
    _class: JClass,
    text: JString,
) -> jstring {
    guarded(&mut env, null_mut(), |env| {
        let s = read(env, &text)?;
        match clip_entities::find_first_email(&s) {
            Some(email) => Some(env.new_string(email).ok()?.into_raw()),
            None => Some(null_mut()),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeClipSetUserOtpKeywords(
    mut env: JNIEnv,
    _class: JClass,
    words: JObjectArray,
) {
    guarded(&mut env, (), |env| {
        let list = read_string_array(env, &words)?;
        clip_entities::set_user_otp_keywords(&list);
        Some(())
    })
}

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeClipParseOtpKeywords(
    mut env: JNIEnv,
    _class: JClass,
    raw: JString,
) -> jobjectArray {
    guarded(&mut env, null_mut(), |env| {
        let s = read(env, &raw)?;
        let parsed = clip_entities::parse_otp_keywords(&s);
        write_string_array(env, &parsed)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeClipIsDeniedOtpKeyword(
    mut env: JNIEnv,
    _class: JClass,
    text: JString,
    start_utf16: jint,
) -> jboolean {
    guarded(&mut env, JNI_FALSE, |env| {
        let s = read(env, &text)?;
        let denied = clip_entities::is_denied_otp_keyword(&s, start_utf16.max(0) as usize);
        Some(if denied { JNI_TRUE } else { JNI_FALSE })
    })
}

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeClipTrimUrlTail(
    mut env: JNIEnv,
    _class: JClass,
    raw: JString,
) -> jstring {
    guarded(&mut env, null_mut(), |env| {
        let s = read(env, &raw)?;
        let trimmed = clip_entities::trim_url_tail(&s);
        Some(env.new_string(trimmed).ok()?.into_raw())
    })
}

#[no_mangle]
pub extern "system" fn Java_com_wasimaster_wmkeyboard_core_engine_CustomRustNative_nativeClipScanText(
    mut env: JNIEnv,
    _class: JClass,
    text: JString,
) -> jobjectArray {
    guarded(&mut env, null_mut(), |env| {
        let s = read(env, &text)?;
        let packed = clip_entities::scan_text_packed(&s);
        write_string_array(env, &packed)
    })
}
