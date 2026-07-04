plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-core"))

    compileOnlyApi(libs.paper.api)
}
