import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.gradle.kotlin.dsl.configure

class RootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")

            subprojects {
                pluginManager.apply("org.jlleitschuh.gradle.ktlint")
                extensions.configure<KtlintExtension> {
                    android.set(true)
                    ignoreFailures.set(false)
                }
            }
        }
    }
}
