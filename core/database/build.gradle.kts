plugins {
    alias(libs.plugins.musicplayer.android.library)
    alias(libs.plugins.musicplayer.android.room)
    alias(libs.plugins.musicplayer.hilt)
}

android {
    namespace = "com.javanapps.musicplayer.core.database"
}

dependencies {
    implementation(projects.core.model)

    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.core.ktx)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(kotlin("test"))
}
