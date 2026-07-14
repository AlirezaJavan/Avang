plugins {
    alias(libs.plugins.musicplayer.android.library)
    alias(libs.plugins.musicplayer.android.library.compose)
}

android {
    namespace = "com.javanapps.musicplayer.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
}
