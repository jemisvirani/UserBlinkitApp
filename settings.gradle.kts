pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://jitpack.io")
            url = uri("https://maven.google.com")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            url = uri("https://maven.google.com")
        }

        maven(
            url = "https://phonepe.mycloudrepo.io/public/repositories/phonepe-intentsdk-android"
        )
    }
}

rootProject.name = "UserBlinkitClone"
include(":app")
 