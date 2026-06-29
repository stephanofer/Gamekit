plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-experience"))
    api(project(":gamekit-paper"))

    api(libs.scoreboard.library.api)
    runtimeOnly(libs.scoreboard.library.implementation)
    compileOnly(libs.paper.api)
}
