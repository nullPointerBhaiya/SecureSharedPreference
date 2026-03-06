# Consumer ProGuard rules for apps using SecureSharedPreference

# Keep all public API
-keep public class io.github.nullpointerbhaiya.secureprefs.SecureSharedPreference {
    public *;
}

# Keep encryption internals
-keep class io.github.nullpointerbhaiya.secureprefs.** { *; }
