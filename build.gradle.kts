plugins {
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.6"
    java
}

group = "stablecoin"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories { mavenCentral() }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")
}

tasks.test {
    useJUnitPlatform()
}

val testPreflight by tasks.registering(Exec::class) {
    group = "verification"
    description = "Provision postgres, openbao, and solana-test-validator for integration tests."
    commandLine("./scripts/test-preflight.sh")
}

tasks.test {
    mustRunAfter(testPreflight)
}

tasks.register("testWithPreflight") {
    group = "verification"
    description = "Run test-preflight.sh, then the full test suite."
    dependsOn(testPreflight, tasks.test)
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Run integration tests after test-preflight.sh."
    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        includeTestsMatching("*IT")
    }
    dependsOn(testPreflight)
    shouldRunAfter(tasks.test)
}

tasks.register<Test>("withdrawalIntegrationTest") {
    group = "verification"
    description = "Run withdrawal integration tests after test-preflight.sh."
    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        includeTestsMatching("*Withdrawal*IT")
    }
    dependsOn(testPreflight)
    shouldRunAfter(tasks.test)
}
