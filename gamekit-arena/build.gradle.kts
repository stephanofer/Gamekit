plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":gamekit-core"))
    api(project(":gamekit-network"))

    testImplementation(project(":gamekit-testkit"))
}
