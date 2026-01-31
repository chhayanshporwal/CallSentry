# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.safeguard.data.local.entity.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Obfuscate
-repackageclasses ''
-allowaccessmodification
