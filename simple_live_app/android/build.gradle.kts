allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Flutter 3.38 no longer auto-applies the Kotlin Gradle plugin to plugin
// modules. Plugins that ship .kt sources but omit `kotlin-android` (e.g.
// auto_orientation_v2) then fail to compile their Kotlin, producing
// "cannot find symbol <PluginClass>" at app compile time. Apply it here for
// any Android library module that actually contains Kotlin sources.
allprojects {
    plugins.withId("com.android.library") {
        val hasKotlinPlugin =
            plugins.hasPlugin("kotlin-android") || plugins.hasPlugin("org.jetbrains.kotlin.android")
        if (!hasKotlinPlugin) {
            val srcDir = file("src")
            val hasKt = srcDir.exists() && srcDir.walkTopDown().any { it.isFile && it.extension == "kt" }
            if (hasKt) {
                apply(plugin = "org.jetbrains.kotlin.android")
                // Kotlin 2.x otherwise defaults its jvmTarget to 17, which clashes
                // with the module's Java compile target (1.8) and fails with
                // "Inconsistent JVM Target Compatibility". Pin Kotlin to 1.8 to match.
                tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
                }
            }
        }
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
