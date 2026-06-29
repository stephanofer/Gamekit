plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-paper"))
    api(project(":gamekit-experience"))

    api(libs.craftkit.zmenu)
    compileOnlyApi(libs.zmenu.api)
    compileOnly(libs.paper.api)
}
