plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-paper"))
    api(project(":gamekit-lobby"))
    api(project(":gamekit-queue"))
    api(project(":gamekit-network"))

    api(libs.cloud.paper)
    api(libs.cloud.minecraft.extras)
    api(libs.craftkit.zmenu)
    api(libs.scoreboard.library.api)
    runtimeOnly(libs.scoreboard.library.implementation)

    implementation(libs.boosted.yaml)

    compileOnlyApi(libs.zmenu.api)
    compileOnlyApi(libs.network.player.settings)
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)
}
