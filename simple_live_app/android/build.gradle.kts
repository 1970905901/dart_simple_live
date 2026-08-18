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
