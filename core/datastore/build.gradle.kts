plugins {
    alias(libs.plugins.musicplayer.android.library)
    alias(libs.plugins.musicplayer.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.javanapps.musicplayer.core.datastore"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // Change this value to set the app's default language ("fa" = Persian, "en" = English)
        buildConfigField("String", "DEFAULT_LANGUAGE", "\"${libs.versions.defaultLanguage.get()}\"")
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
}
