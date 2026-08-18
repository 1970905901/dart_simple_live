pluginManagement {
    val flutterSdkPath =
        run {
            val properties = java.util.Properties()
            file("local.properties").inputStream().use { properties.load(it) }
            val flutterSdkPath = properties.getProperty("flutter.sdk")
            require(flutterSdkPath != null) { "flutter.sdk not set in local.properties" }
            flutterSdkPath
        }

    includeBuild("$flutterSdkPath/packages/flutter_tools/gradle")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // Force a recent Kotlin Gradle Plugin onto the classpath so that plugins
    // using the modern `kotlin { compilerOptions { jvmTarget = ... } }` DSL
    // (e.g. dynamic_color 1.9.x) resolve correctly. Without this, Gradle falls
    // back to the Flutter-bundled (older) Kotlin and the build fails with
    // "Unresolved reference: compilerOptions / jvmTarget".
    plugins {
        id("org.jetbrains.kotlin.android") version "2.1.20"
    }
}

plugins {
    id("dev.flutter.flutter-plugin-loader") version "1.0.0"
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
}

include(":app")
