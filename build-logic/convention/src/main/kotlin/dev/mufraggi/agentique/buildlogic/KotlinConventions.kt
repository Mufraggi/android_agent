package dev.mufraggi.agentique.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

internal val JAVA_VERSION = JavaVersion.VERSION_17
internal const val COMPILE_SDK = 37
internal const val MIN_SDK = 31
internal const val TARGET_SDK = 37

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun KotlinJvmCompilerOptions.configureAgentique() {
    jvmTarget.set(JvmTarget.fromTarget(JAVA_VERSION.toString()))
}
