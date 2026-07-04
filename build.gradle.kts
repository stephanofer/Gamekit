plugins {
    alias(libs.plugins.shadow) apply false
}

allprojects {
    group = "network.hera.gamekit"
    version = providers.gradleProperty("gamekit.version").get()
}

subprojects {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }

            withSourcesJar()
            withJavadocJar()
        }

        dependencies.add("testImplementation", dependencies.platform(libs.junit.bom))
        dependencies.add("testImplementation", libs.junit.jupiter)
        dependencies.add("testRuntimeOnly", libs.junit.platform.launcher)

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(25)
            options.encoding = "UTF-8"
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }

        plugins.withType<MavenPublishPlugin> {
            extensions.configure<PublishingExtension> {
                publications.register<MavenPublication>("mavenJava") {
                    from(components["java"])
                }
            }
        }
    }

    plugins.withType<JavaLibraryPlugin> {
        dependencies.add("compileOnlyApi", libs.jetbrains.annotations)
    }
}

tasks.register("publishGameKitLibrariesToMavenLocal") {
    group = "publishing"
    description = "Publishes all GameKit library modules and the BOM to Maven Local for plugin consumers."

    dependsOn(
        ":gamekit-bom:publishToMavenLocal",
        ":gamekit-core:publishToMavenLocal",
        ":gamekit-session:publishToMavenLocal",
        ":gamekit-lobby:publishToMavenLocal",
        ":gamekit-queue:publishToMavenLocal",
        ":gamekit-network:publishToMavenLocal",
        ":gamekit-arena:publishToMavenLocal",
        ":gamekit-match:publishToMavenLocal",
        ":gamekit-progression:publishToMavenLocal",
        ":gamekit-competitive-integrity:publishToMavenLocal",
        ":gamekit-admin:publishToMavenLocal",
        ":gamekit-infra-craftkit:publishToMavenLocal",
        ":gamekit-paper:publishToMavenLocal",
        ":gamekit-lobby-paper:publishToMavenLocal",
        ":gamekit-arena-paper:publishToMavenLocal",
        ":gamekit-admin-paper:publishToMavenLocal",
        ":gamekit-testkit:publishToMavenLocal",
    )
}

tasks.register("assembleGameKitDistributions") {
    group = "build"
    description = "Builds distributable GameKit artifacts, including the Velocity plugin jar in the root target directory."

    dependsOn(":gamekit-velocity-plugin:shadowJar")
}
