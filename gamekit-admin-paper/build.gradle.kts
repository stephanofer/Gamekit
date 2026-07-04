plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-paper"))
    api(project(":gamekit-admin"))
    api(project(":gamekit-network"))
    api(project(":gamekit-arena"))
    api(project(":gamekit-match"))
    api(project(":gamekit-queue"))
    api(project(":gamekit-progression"))
    api(project(":gamekit-competitive-integrity"))

    api(libs.cloud.paper)
    api(libs.cloud.minecraft.extras)
    api(libs.craftkit.zmenu)

    implementation(libs.boosted.yaml)

    compileOnlyApi(libs.zmenu.api)
    compileOnlyApi(libs.network.player.settings)
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)
}
