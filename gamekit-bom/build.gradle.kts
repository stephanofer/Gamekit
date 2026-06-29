plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":gamekit-core"))
        api(project(":gamekit-session"))
        api(project(":gamekit-lobby"))
        api(project(":gamekit-queue"))
        api(project(":gamekit-network"))
        api(project(":gamekit-arena"))
        api(project(":gamekit-match"))
        api(project(":gamekit-experience"))
        api(project(":gamekit-progression"))
        api(project(":gamekit-season"))
        api(project(":gamekit-competitive-integrity"))
        api(project(":gamekit-admin"))
        api(project(":gamekit-infra-craftkit"))
        api(project(":gamekit-paper"))
        api(project(":gamekit-scoreboard-paper"))
        api(project(":gamekit-zmenu-paper"))
        api(project(":gamekit-cloud-paper"))
        api(project(":gamekit-velocity"))
        api(project(":gamekit-paper-worldedit"))
        api(project(":gamekit-testkit"))

        api(libs.paper.api)
        api(libs.velocity.api)
        api(libs.boosted.yaml)
        api(libs.cloud.paper)
        api(libs.cloud.velocity)
        api(libs.cloud.minecraft.extras)
        api(libs.scoreboard.library.api)
        api(libs.scoreboard.library.implementation)
        api(libs.zmenu.api)
        api(libs.network.player.settings)
        api(libs.placeholderapi)
        api(libs.craftkit.database)
        api(libs.craftkit.feedback)
        api(libs.craftkit.paper)
        api(libs.craftkit.redis)
        api(libs.craftkit.zmenu)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["javaPlatform"])
        }
    }
}
