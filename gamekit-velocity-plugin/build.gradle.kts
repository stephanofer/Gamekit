plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":gamekit-core"))
    implementation(project(":gamekit-network"))
    implementation(project(":gamekit-infra-craftkit"))
    implementation(libs.boosted.yaml)

    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    testImplementation(libs.velocity.api)
    testImplementation(project(":gamekit-testkit"))
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("gamekit-velocity-plugin-${project.version}.jar")
        destinationDirectory.set(rootProject.layout.projectDirectory.dir("target"))

        manifest {
            attributes(
                "Implementation-Title" to "GameKit Velocity Plugin",
                "Implementation-Version" to project.version,
            )
        }
    }

    assemble {
        dependsOn(shadowJar)
    }
}
