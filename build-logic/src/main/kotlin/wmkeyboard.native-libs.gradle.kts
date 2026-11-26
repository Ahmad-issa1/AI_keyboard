import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension

// Patched for custom slim build: tolerates modules whose optional native gates were emptied.
val root = rootDir.toPath()
val here = root.relativize(projectDir.toPath()).toString().replace('\\', '/')
val dirs = NativeGates.of(project).ownLibraries
    .map { (_, library) -> library.dir }
    .distinct()
    .filter { it.startsWith("$here/src/") }
    .map { it.removePrefix("$here/") }

if (dirs.isNotEmpty()) {
    val playStoreChannel = (wmkbFlag("wmkb.enablePlayStore", "WMKB_ENABLE_PLAY_STORE") ?: "false").toBoolean()

    if (playStoreChannel && "src/main/jniLibs" in dirs) {
        extensions.getByType(LibraryExtension::class.java).sourceSets
            .getByName("main").jniLibs.setSrcDirs(emptyList<String>())
    }

    extensions.getByType(LibraryAndroidComponentsExtension::class.java).onVariants { variant ->
        if (!playStoreChannel) {
            for (dir in dirs - "src/main/jniLibs") variant.sources.jniLibs?.addStaticSourceDirectory(dir)
        }
    }
}
