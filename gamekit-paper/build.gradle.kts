plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-core"))
    api(project(":gamekit-session"))
    api(project(":gamekit-lobby"))
    api(project(":gamekit-network"))
    api(project(":gamekit-arena"))
    api(project(":gamekit-match"))
    api(project(":gamekit-experience"))

    compileOnlyApi(libs.paper.api)
    compileOnlyApi(libs.network.player.settings)
    compileOnly(libs.placeholderapi)
}
