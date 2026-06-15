plugins {
    alias(libs.plugins.musicplayer.android.library)
    alias(libs.plugins.musicplayer.hilt)
}

android {
    namespace = "com.javanapps.musicplayer.core.data"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.datastore)
    implementation(projects.core.analysis)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.coil.compose)
    implementation(libs.coil.core)
    implementation(libs.coil.network)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.room.ktx) // For test support if needed
    testImplementation(projects.core.testing)
}
