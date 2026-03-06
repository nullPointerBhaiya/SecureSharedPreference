# ProGuard rules for SecureSharedPreference Library

# Keep all public classes and methods
-keep public class io.github.nullpointerbhaiya.secureprefs.** {
    public *;
}

# Keep encryption classes
-keep class io.github.nullpointerbhaiya.secureprefs.GuardSquare { *; }
-keep class io.github.nullpointerbhaiya.secureprefs.GuardSquareEncryption { *; }
-keep class io.github.nullpointerbhaiya.secureprefs.SecureSharedPreference { *; }

# Keep Android KeyStore classes
-keep class android.security.keystore.** { *; }
-keep class javax.crypto.** { *; }

# Don't warn about missing classes
-dontwarn android.security.keystore.**
-dontwarn javax.crypto.**
