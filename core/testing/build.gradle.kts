plugins {
    alias(libs.plugins.musicplayer.android.library)
    alias(libs.plugins.musicplayer.hilt)
}

android {
    namespace = "com.javanapps.musicplayer.core.testing"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // Must match DEFAULT_LANGUAGE in core/datastore/build.gradle.kts
        buildConfigField("String", "DEFAULT_LANGUAGE", "\"${libs.versions.defaultLanguage.get()}\"")
    }
}

dependencies {
    api(libs.kotlinx.coroutines.test)
    api(projects.core.common)
    api(projects.core.domain)
    api(projects.core.data)
    api(projects.core.database)
    api(projects.core.model)

    implementation(libs.hilt.android)
    implementation(libs.turbine)
    implementation(libs.truth)
    implementation(libs.junit)
}
