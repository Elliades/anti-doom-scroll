plugins {
    kotlin("jvm") version "1.9.21"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("plugin.spring") version "1.9.21"
    kotlin("plugin.jpa") version "1.9.21"
}

group = "app.antidoomscroll"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core:10.0.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2") // for local profile (run without Docker/PostgreSQL)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Show JVM stdout/stderr so startup errors are visible when bootRun exits with code 1
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    standardOutput = System.out
    errorOutput = System.err
}

// Run the built JAR so you see the full Spring Boot log (use when bootRun fails with exit code 1)
tasks.register<Exec>("runJar") {
    group = "application"
    description = "Run the built JAR (use to see full startup error when bootRun fails)"
    dependsOn("bootJar")
    val jarFile = tasks.bootJar.get().archiveFile.get().asFile
    commandLine("java", "-jar", jarFile.absolutePath)
}
