pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // For Paho MQTT Client
        maven {
            url = uri("https://repo.eclipse.org/content/repositories/paho-releases/")
        }
    }
}

rootProject.name = "MoveEcho"
include(":app")