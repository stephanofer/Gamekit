plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-core"))
    api(project(":gamekit-network"))
    api(project(":gamekit-arena"))

    api(libs.craftkit.database)
    api(libs.craftkit.feedback)
    api(libs.craftkit.redis)

    testImplementation(project(":gamekit-testkit"))
}
