plugins {
    alias(libs.plugins.agentique.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "dev.mufraggi.agentique.storage"
    // Lets MemoryDaoTest subclass ContextWrapper on the JVM.
    testOptions.unitTests.isReturnDefaultValues = true
    // The bundled SQLite driver loads a native library.
    testOptions.unitTests.all { it.jvmArgs("--enable-native-access=ALL-UNNAMED") }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.bundled)
    ksp(libs.androidx.room.compiler)

    // Host build of the bundled driver: same SQLite as the app, on the JVM.
    testImplementation(libs.androidx.sqlite.bundled.jvm)
    testImplementation(libs.kotlinx.coroutines.test)
}
