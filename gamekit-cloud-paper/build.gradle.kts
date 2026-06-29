plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-admin"))
    api(project(":gamekit-paper"))

    api(libs.cloud.paper)
    api(libs.cloud.minecraft.extras)
    compileOnly(libs.paper.api)
}
