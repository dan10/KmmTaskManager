rootProject.name = "KmmTaskManager"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()

    }
}

// Conditionally include the composeApp subproject only when its directory exists to
// avoid build failures in environments where the folder is absent (e.g., CI or Docker).
val composeAppDir = File(rootDir, "composeApp")
if (composeAppDir.exists() && composeAppDir.isDirectory) {
    include(":composeApp")
} else {
    logger.lifecycle("Skipping :composeApp - directory '${composeAppDir.absolutePath}' not found.")
}

include(":server")
include(":shared")

// Conditionally include the paging-compose subproject only when its directory exists to
// avoid build failures in environments where the folder is absent (e.g., CI or Docker).
val pagingComposeDir = File(rootDir, "paging-compose")
if (pagingComposeDir.exists() && pagingComposeDir.isDirectory) {
    include(":paging-compose")
} else {
    logger.lifecycle("Skipping :paging-compose - directory '${pagingComposeDir.absolutePath}' not found.")
}

// Conditionally include the appium subproject only when its directory exists to
// avoid build failures in environments where the folder is absent (e.g., CI or Docker).
val appiumDir = File(rootDir, "appium")
if (appiumDir.exists() && appiumDir.isDirectory) {
    include(":appium")
} else {
    logger.lifecycle("Skipping :appium - directory '${appiumDir.absolutePath}' not found.")
}
