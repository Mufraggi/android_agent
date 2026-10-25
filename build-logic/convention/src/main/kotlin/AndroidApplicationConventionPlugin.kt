import com.android.build.api.dsl.ApplicationExtension
import dev.mufraggi.agentique.buildlogic.COMPILE_SDK
import dev.mufraggi.agentique.buildlogic.JAVA_VERSION
import dev.mufraggi.agentique.buildlogic.MIN_SDK
import dev.mufraggi.agentique.buildlogic.TARGET_SDK
import dev.mufraggi.agentique.buildlogic.configureAgentique
import dev.mufraggi.agentique.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            compileSdk = COMPILE_SDK
            defaultConfig {
                minSdk = MIN_SDK
                targetSdk = TARGET_SDK
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
        }
        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions.configureAgentique()
        }

        dependencies {
            "testImplementation"(libs.findLibrary("junit").get())
        }
    }
}
