plugins {
    alias(libs.plugins.musicplayer.android.feature.impl)
    alias(libs.plugins.musicplayer.android.library.compose)
}

android {
    namespace = "com.javanapps.musicplayer.feature.library.impl"
}

dependencies {
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(kotlin("test"))

    implementation(projects.feature.library.api)
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.domain)

    implementation(libs.androidx.compose.animation)
    implementation(libs.accompanist.permissions)
}
