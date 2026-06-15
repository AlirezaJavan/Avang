plugins {
    alias(libs.plugins.musicplayer.android.feature.impl)
    alias(libs.plugins.musicplayer.android.library.compose)
}

android {
    namespace = "com.javanapps.musicplayer.feature.playlists.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(kotlin("test"))

    implementation(projects.feature.playlists.api)

    implementation(libs.androidx.compose.animation)

    testImplementation(libs.robolectric)
}
