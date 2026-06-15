# Media3 / ExoPlayer
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.common.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.internal.** { *; }
-dontwarn dagger.hilt.internal.aggregatedroot.codegen.**

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
