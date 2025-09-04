import org.jooq.meta.jaxb.ForcedType

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("nu.studer.jooq") version "9.0" // jOOQ 코드 생성을 위한 플러그인
}

group = "com.msa"
version = "0.0.1-SNAPSHOT"
description = "identity-service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.springframework.boot:spring-boot-starter-security")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")

	implementation("com.github.f4b6a3:tsid-creator:5.2.6")

	runtimeOnly("com.mysql:mysql-connector-j")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")


	jooqGenerator("com.mysql:mysql-connector-j")
}

val dbUrl = project.findProperty("db.url") as? String ?: "jdbc:mysql://localhost:30306/identity-db"
val dbUser = project.findProperty("db.user") as? String ?: "root"
val dbPassword = project.findProperty("db.password") as? String ?: "root"

jooq { // jOOQ 코드 생성 설정
	version.set(dependencyManagement.importedProperties["jooq.version"]) // SpringBoot 관리하는 jOOQ 버전을 사용

	configurations {
		create("main") {
			generateSchemaSourceOnCompilation.set(true) // 코드 생성을 빌드 시 자동으로 실행하고 싶지 않다면 false 설정

			jooqConfiguration.apply {
				jdbc.apply {
					driver = "com.mysql.cj.jdbc.Driver"
					url = dbUrl
					user = dbUser
					password = dbPassword
				}

				generator.apply {
					name = "org.jooq.codegen.KotlinGenerator" // Java 대신 Kotlin 코드를 생성하도록 설정

					database.apply {
						name = "org.jooq.meta.mysql.MySQLDatabase"
						inputSchema = "identity-db" // 생성할 스키마 지정
						excludes = "flyway_schema_history|batch_.*" // 생성에서 제외할 테이블 (정규식 사용 가능)

						// unsigned 타입을 강제로 Long으로 매핑
						forcedTypes = listOf(
							ForcedType()
								.withUserType("java.lang.Long")
								.withIncludeExpression(".*\\.UNSIGNED")
								.withIncludeTypes("BIGINT")
						)
					}

					target.apply {
						// 생성될 코드의 패키지 경로
						packageName = "com.msa.identityservice.jooq"
						// 생성될 코드의 디렉토리 경로
						directory = "src/main/generated"
					}

					generate.apply {
						isRecords = true // Record 클래스 생성
						isDaos = false // DAO 클래스는 생성하지 않음 (선택 사항)
						isPojos = true // POJO 클래스 생성
						isFluentSetters = true // Fluent Setter 생성
						isJavaTimeTypes = true // 날짜/시간 타입을 Java 8+ Time API로 매핑
						isKotlinNotNullPojoAttributes = true // Kotlin POJO에서 NotNull 속성을 non-nullable 타입으로 생성
					}
				}
			}
		}
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sourceSets.main.get().java.srcDirs("src/main/generated")

