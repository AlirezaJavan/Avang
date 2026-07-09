plugins {
    alias(libs.plugins.musicplayer.android.application)
    alias(libs.plugins.musicplayer.android.application.compose)
    alias(libs.plugins.musicplayer.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.javanapps.musicplayer"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.javanapps.musicplayer"
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Must match DEFAULT_LANGUAGE in core/datastore/build.gradle.kts
        buildConfigField("String", "DEFAULT_LANGUAGE", "\"${libs.versions.defaultLanguage.get()}\"")
    }

    signingConfigs {
        create("release") {
            val storePath = System.getenv("SIGNING_STORE_PATH")
            val alias = System.getenv("SIGNING_KEY_ALIAS")
            val password = System.getenv("SIGNING_KEY_PASSWORD")
            if (storePath != null && alias != null && password != null) {
                storeFile = file(storePath)
                storePassword = password
                keyAlias = alias
                keyPassword = password
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(libs.firebase.crashlytics)
    // Features
    implementation(projects.feature.home.api)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.library.api)
    implementation(projects.feature.library.impl)
    implementation(projects.feature.player.api)
    implementation(projects.feature.player.impl)
    implementation(projects.feature.playlists.api)
    implementation(projects.feature.playlists.impl)
    implementation(projects.feature.favorites.api)
    implementation(projects.feature.favorites.impl)
    implementation(projects.feature.equalizer.api)
    implementation(projects.feature.equalizer.impl)
    implementation(projects.feature.notes.api)
    implementation(projects.feature.notes.impl)
    implementation(projects.feature.settings.api)
    implementation(projects.feature.settings.impl)

    // Core
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.media)

    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.dev.chrisbanes.haze)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.coil.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    // Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
