pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven (  "https://jitpack.io" )

        maven ("https://repo.supabase.io/repository/maven-public/")
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "racketRivals"
include(":app")
 