plugins {
    alias(libs.plugins.musicplayer.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.annotation)
}
