plugins {
    val kotlinVersion = "2.2.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion apply false

    id("org.springframework.boot") version "3.5.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false

    id("nu.studer.jooq") version "10.1" apply false
}

group = "msa.hotel"
version = "1.0"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

allprojects {
    repositories { mavenCentral() }

    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")

        val kotestVersion = "5.9.1"
        testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    val springProjects = setOf(":modules:web", ":modules:jwt")

    if (path.startsWith(":services:") || path in springProjects) {
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")

        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-web")
            implementation("org.springframework.boot:spring-boot-starter-validation")

            val kotlinLoggingVersion = "7.0.11"
            implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingVersion")
            implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

            val dotenvVersion = "6.5.1"
            implementation("io.github.cdimascio:dotenv-kotlin:$dotenvVersion")

            testImplementation("org.springframework.boot:spring-boot-starter-test") {
                exclude(group = "org.junit.vintage")
                exclude(group = "org.mockito")
                exclude(group = "org.assertj")
                exclude(group = "org.hamcrest")
            }
            testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
        }
    }

    val dbProjects = setOf(":services:auth")
    if (path in dbProjects) {
        apply(plugin = "nu.studer.jooq")

        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-jooq")

            implementation("org.flywaydb:flyway-core")
            implementation("org.flywaydb:flyway-mysql")

            runtimeOnly("com.mysql:mysql-connector-j")
        }

        sourceSets.main
            .get()
            .kotlin
            .srcDirs("src/main/generated")
    }
}
