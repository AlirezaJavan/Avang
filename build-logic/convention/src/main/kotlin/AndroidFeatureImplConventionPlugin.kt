import com.javanapps.musicplayer.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("musicplayer.android.library")
            pluginManager.apply("musicplayer.hilt")

            dependencies {
                "implementation"(project(":core:ui"))
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:domain"))

                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("androidx-navigation-compose").get())
                "implementation"(libs.findLibrary("hilt-navigation-compose").get())
                "implementation"(libs.findLibrary("kotlinx-serialization-json").get())

                "testImplementation"(project(":core:testing"))
                "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
                "testImplementation"(libs.findLibrary("turbine").get())
                "testImplementation"(libs.findLibrary("truth").get())

                "debugImplementation"(libs.findLibrary("androidx-compose-ui-test-manifest").get())
            }
        }
    }
}
