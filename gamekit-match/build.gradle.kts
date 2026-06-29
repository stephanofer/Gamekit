plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-core"))
    api(project(":gamekit-arena"))
    api(project(":gamekit-session"))
}
