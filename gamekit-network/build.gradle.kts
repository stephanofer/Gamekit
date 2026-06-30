plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-core"))

    testImplementation(project(":gamekit-testkit"))
}
