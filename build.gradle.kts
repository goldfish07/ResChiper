plugins {
    id("java")
    id("maven-publish")
    id("signing")
}

group = "io.github.goldfish07.reschiper"
version = "0.1.0-rc5"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets["main"].allJava)
    archiveClassifier.set("sources")
}

val javadocJar by tasks.registering(Jar::class) {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(gradleApi())
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("com.android.tools.build:gradle:8.8.0")
    implementation("com.android.tools.build:bundletool:1.17.2")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("io.grpc:grpc-protobuf:1.59.1")
    implementation("com.android.tools.build:aapt2-proto:8.8.0-12006047")
    implementation("commons-codec:commons-codec:1.16.0")
    implementation("commons-io:commons-io:2.15.1")
    implementation("org.dom4j:dom4j:2.1.4")
    implementation("com.google.auto.value:auto-value:1.5.4")
    annotationProcessor("com.google.auto.value:auto-value:1.5.4")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = rootProject.group.toString()
            version = rootProject.version.toString()
            artifactId = "plugin"
            description = "AAB Resource Obfuscation Tool"
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                packaging = "jar"
                name.set("ResChiper")
                description.set("A tool for obfuscating Android AAB resources")
                url.set("https://github.com/goldfish07/reschiper")

                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set(project.findProperty("ossrhUsername").toString())
                        name.set(project.findProperty("devSimpleName").toString())
                        email.set(project.findProperty("devMail").toString())
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/goldfish07/reschiper.git")
                    developerConnection.set("scm:git:ssh://github.com/goldfish07/reschiper.git")
                    url.set("https://github.com/goldfish07/reschiper")
                }
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername").toString()
                password = project.findProperty("ossrhPassword").toString()
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}