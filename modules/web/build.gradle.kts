plugins {
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("jakarta.servlet:jakarta.servlet-api")
    testFixturesImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
