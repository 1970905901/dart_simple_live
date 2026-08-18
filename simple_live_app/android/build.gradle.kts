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
                // Align Kotlin's JVM target with this module's own Java target
                // (compileOptions.targetCompatibility), which varies per plugin
                // (auto_orientation_v2 -> 1.8, device_info_plus -> 17). Read it
                // lazily so the plugin's own build.gradle has configured it by
                // execution time. Prevents
                // "Inconsistent JVM Target Compatibility" between the Kotlin and
                // Java compile tasks.
                val androidExt = extensions.getByType<com.android.build.gradle.LibraryExtension>()
                tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                    compilerOptions.jvmTarget.set(
                        project.provider {
                            org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(
                                androidExt.compileOptions.targetCompatibility.toString()
                            )
                        }
                    )
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
