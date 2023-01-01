plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    `java-library`
    id("org.sonarqube") version "3.5.0.2730"
    id("jacoco")
}

repositories {
    mavenCentral()
}

val ktorVersion: String by project

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    implementation("io.github.nsk90:kstatemachine:0.17.0")

    implementation("com.beust:klaxon:5.6")

    implementation("software.pando.crypto:salty-coffee:1.1.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.assertj:assertj-core:3.23.1")
}

jacoco {
    toolVersion = "0.8.8"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(false)
    }
    dependsOn(tasks.test)
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

sonar {
    properties {
        property("sonar.projectKey", "least-authority_wormhole-client_AYVEgPFpIb_tSwskweLP")
        property("sonar.qualitygate.wait", true)
    }
}