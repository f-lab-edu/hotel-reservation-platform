plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "9.0" // jOOQ 코드 생성을 위한 플러그인
}

group = "msa"
version = "1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}
