import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.javanapps.musicplayer.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.android.tools.common)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.room.gradlePlugin)
    implementation(libs.ktlint.gradlePlugin)
}

// Register each convention plugin
gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "musicplayer.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "musicplayer.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "musicplayer.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "musicplayer.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeatureImpl") {
            id = "musicplayer.android.feature.impl"
            implementationClass = "AndroidFeatureImplConventionPlugin"
        }
        register("androidFeatureApi") {
            id = "musicplayer.android.feature.api"
            implementationClass = "AndroidFeatureApiConventionPlugin"
        }
        register("androidRoom") {
            id = "musicplayer.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("hilt") {
            id = "musicplayer.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = "musicplayer.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("root") {
            id = "musicplayer.root"
            implementationClass = "RootConventionPlugin"
        }
    }
}
