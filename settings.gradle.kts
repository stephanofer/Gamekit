pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.groupez.dev/releases")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

rootProject.name = "GameKit"

include(
    "gamekit-bom",
    "gamekit-core",
    "gamekit-session",
    "gamekit-lobby",
    "gamekit-queue",
    "gamekit-network",
    "gamekit-arena",
    "gamekit-match",
    "gamekit-experience",
    "gamekit-progression",
    "gamekit-season",
    "gamekit-competitive-integrity",
    "gamekit-admin",
    "gamekit-infra-craftkit",
    "gamekit-paper",
    "gamekit-scoreboard-paper",
    "gamekit-zmenu-paper",
    "gamekit-cloud-paper",
    "gamekit-velocity",
    "gamekit-velocity-plugin",
    "gamekit-paper-worldedit",
    "gamekit-testkit",
)
