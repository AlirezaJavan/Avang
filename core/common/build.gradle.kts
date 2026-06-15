plugins {
    alias(libs.plugins.musicplayer.android.library)
    alias(libs.plugins.musicplayer.hilt)
}

android {
    namespace = "com.javanapps.musicplayer.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
