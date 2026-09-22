# ========================
# 🔐 KEEP MODEL (Gson)
# ========================
-keep class com.kkphim.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keep class com.google.gson.** { *; }
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ========================
# 🌐 RETROFIT / OKHTTP
# ========================
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# ========================
# 🖼 GLIDE
# ========================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# ========================
# 🎬 MEDIA3 (ExoPlayer)
# ========================
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ========================
# 🧠 KOTLIN / COROUTINES
# ========================
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# ========================
# 🔧 ANDROIDX
# ========================
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# ========================
# 🚫 REMOVE LOG (optional)
# ========================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
}
