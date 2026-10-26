plugins {
    alias(libs.plugins.agentique.jvm.library)
    `java-test-fixtures`
}

dependencies {
    // These types appear in :core's public signatures (ports, use cases, tools).
    api(libs.arrow.core)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)

    testImplementation(libs.kotest.assertions.arrow)
    testImplementation(libs.kotlinx.coroutines.test)
}
