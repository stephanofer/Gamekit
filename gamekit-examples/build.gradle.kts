plugins {
    `java-library`
}

dependencies {
    implementation(platform(project(":gamekit-bom")))

    implementation(project(":gamekit-core"))
    implementation(project(":gamekit-session"))
    implementation(project(":gamekit-lobby"))
    implementation(project(":gamekit-queue"))
    implementation(project(":gamekit-network"))
    implementation(project(":gamekit-arena"))
    implementation(project(":gamekit-match"))
    implementation(project(":gamekit-paper"))
}
