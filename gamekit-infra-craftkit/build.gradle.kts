plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-core"))
    api(project(":gamekit-network"))

    api(libs.craftkit.database)
    api(libs.craftkit.feedback)
    api(libs.craftkit.redis)
}
