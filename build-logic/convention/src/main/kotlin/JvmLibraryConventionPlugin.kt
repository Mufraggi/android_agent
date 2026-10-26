import dev.mufraggi.agentique.buildlogic.JAVA_VERSION
import dev.mufraggi.agentique.buildlogic.configureAgentique
import dev.mufraggi.agentique.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Pure Kotlin/JVM module: no Android dependency can compile here (ADR 0006). */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions.configureAgentique()
        }

        tasks.withType<Test>().configureEach { useJUnitPlatform() }

        dependencies {
            "testImplementation"(platform(libs.findLibrary("junit-bom").get()))
            "testImplementation"(libs.findLibrary("junit-jupiter").get())
            "testImplementation"(libs.findLibrary("kotest-assertions-core").get())
            "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
        }
    }
}
