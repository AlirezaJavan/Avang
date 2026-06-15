plugins {
    alias(libs.plugins.musicplayer.android.library)
    alias(libs.plugins.musicplayer.hilt)
}

android {
    namespace = "com.javanapps.musicplayer.core.media"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.common)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.ui)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.dev.chrisbanes.haze)
}
