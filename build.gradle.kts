plugins {
    id("java")
}

group = "io.github.goldfish07.reschiper"
version = "0.1.0-rc1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(gradleApi())
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("com.android.tools.build:gradle:4.2.2")
    implementation("com.android.tools.build:bundletool:1.0.0")
    implementation("com.google.guava:guava:30.0-jre")
    implementation("com.android.tools.build:aapt2-proto:0.4.0")
    implementation("commons-codec:commons-codec:1.16.0")
    implementation("commons-io:commons-io:2.13.0")
    implementation("org.dom4j:dom4j:2.1.0")
    implementation("com.google.auto.value:auto-value:1.5.4")
    annotationProcessor("com.google.auto.value:auto-value:1.5.4")
}

tasks.test {
    useJUnitPlatform()
}