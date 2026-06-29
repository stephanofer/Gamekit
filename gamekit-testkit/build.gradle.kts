plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-core"))
    api(project(":gamekit-session"))
    api(project(":gamekit-queue"))
    api(project(":gamekit-network"))
    api(project(":gamekit-arena"))
    api(project(":gamekit-match"))
}
