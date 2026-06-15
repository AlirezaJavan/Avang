plugins {
    alias(libs.plugins.musicplayer.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.kotlinx.coroutines.android)
}
