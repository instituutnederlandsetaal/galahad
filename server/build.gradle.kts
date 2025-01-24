plugins {
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.spring") version "2.0.21"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "org.ivdnt"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

dependencies {
	// Spring
	// Versions controlled by Spring Boot plygin
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// kotlin
	// Versions controlled by Kotlin jvm plugin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

	// swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

	// logging
	implementation("org.apache.logging.log4j:log4j-api-kotlin:1.5.0")

	// yaml
	implementation("org.yaml:snakeyaml:2.3")

	// json
	implementation("com.beust:klaxon:5.6")

	// cache
	implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

tasks.withType<Test> {
	environment(mapOf("profile" to "dev"))
	useJUnitPlatform()
}
