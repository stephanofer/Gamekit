plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-arena"))
    api(project(":gamekit-paper"))

    compileOnly(libs.paper.api)
}
