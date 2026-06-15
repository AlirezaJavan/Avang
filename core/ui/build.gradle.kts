plugins {
    alias(libs.plugins.musicplayer.android.library)
    alias(libs.plugins.musicplayer.android.library.compose)
    alias(libs.plugins.musicplayer.hilt)
}

android {
    namespace = "com.javanapps.musicplayer.core.ui"
}

dependencies {
    api(projects.core.model)
    api(projects.core.designsystem)

    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.material.icons.core)

    api(libs.coil.compose)
    api(libs.coil.core)
    implementation(libs.coil.network)
    implementation(libs.accompanist.permissions)
}
