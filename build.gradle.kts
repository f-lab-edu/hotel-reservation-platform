plugins {
    kotlin("jvm") version "1.9.22" // Kotlin JVM 버전을 Java 21에 맞춤
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21)) // Java 21 설정
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
    }
}
