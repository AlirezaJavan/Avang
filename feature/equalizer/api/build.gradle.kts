plugins {
    alias(libs.plugins.musicplayer.android.feature.api)
}

android {
    namespace = "com.javanapps.musicplayer.feature.equalizer.api"
}

dependencies {
    implementation(projects.core.model)
}
