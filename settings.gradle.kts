pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MusicPlayer"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Enforce JDK 17+
check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    "This project requires JDK 17+. Current: ${JavaVersion.current()}"
}

// Core modules
include(":core:common")
include(":core:model")
include(":core:database")
include(":core:datastore")
include(":core:domain")
include(":core:data")
include(":core:designsystem")
include(":core:ui")
include(":core:media")
include(":core:analysis")
include(":core:testing")

// Feature modules (api + impl)
include(":feature:library:api")
include(":feature:library:impl")
include(":feature:player:api")
include(":feature:player:impl")
include(":feature:playlists:api")
include(":feature:playlists:impl")
include(":feature:favorites:api")
include(":feature:favorites:impl")
include(":feature:equalizer:api")
include(":feature:equalizer:impl")
include(":feature:notes:api")
include(":feature:notes:impl")
include(":feature:settings:api")
include(":feature:settings:impl")

// App
include(":app")
