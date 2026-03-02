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

# Keep domain models (used by Firestore serialization via reflection)
-keep class com.safeguard.domain.model.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Google Play Services Auth
-keep class com.google.android.gms.auth.** { *; }
-dontwarn com.google.android.gms.auth.**

# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }

# Obfuscate
-repackageclasses ''
-allowaccessmodification
