plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.ivdnt"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Spring
    // Versions controlled by Spring Boot plugin
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // kotlin
    // Versions controlled by Kotlin jvm plugin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // logging
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.5.0")

    // yaml
    implementation("org.yaml:snakeyaml:2.4")

    // json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // xml
    implementation("com.fasterxml:aalto-xml:1.3.3")

    // cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.2")

    // reading microsoft word docx
    implementation("org.apache.poi:poi-ooxml:5.4.1")

    // reading pdf
    implementation("com.itextpdf:itextpdf:5.5.13.4")

    // immutable arrays
    // implementation("com.danrusu.pods4k:pods4k:0.7.0")
}

tasks.withType<Test> {
    environment(mapOf("profile" to "dev"))
    systemProperty("line.separator", "\n")
    useJUnitPlatform()
}
