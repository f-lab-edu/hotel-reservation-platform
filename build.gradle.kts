plugins {
    kotlin("jvm") version "1.9.22"
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
    apply(plugin = "checkstyle")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
    }

    configure<CheckstyleExtension> {
        toolVersion = "10.12.1"
        configFile = rootProject.file("config/checkstyle/naver-checkstyle-rules.xml")
        configProperties = mapOf("suppressionFile" to "${rootDir}/config/checkstyle/naver-checkstyle-suppressions.xml")
        isIgnoreFailures = false
        isShowViolations = true
    }

    tasks.withType<Checkstyle> {
        reports {
            xml.required.set(false)
            html.required.set(true)
        }
    }
}
