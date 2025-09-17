dependencies {
    implementation(project(":modules:web"))
    implementation(project(":modules:id-generator"))
    implementation(project(":modules:jwt"))

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    testImplementation(testFixtures(project(":modules:web")))

    jooqGenerator("com.mysql:mysql-connector-j")
}

group = "msa.hotel.services"

val dbSchema = "auth-db"
val dbUrl = project.findProperty("db.url") as? String ?: "jdbc:mysql://localhost:30306/$dbSchema"
val dbUser = project.findProperty("db.user") as? String ?: "root"
val dbPassword = project.findProperty("db.password") as? String ?: "root"

val jooqPackagePath = "msa.hotel.services.auth.jooq"
val jooqGeneratorOutputDir = "src/main/generated"

// jOOQ 코드 생성 설정
jooq {
    version.set(dependencyManagement.importedProperties["jooq.version"]) // SpringBoot 관리하는 jOOQ 버전을 사용

    configurations.create("main").apply {
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
                    inputSchema = dbSchema // 생성할 스키마 지정
                    excludes = "flyway_schema_history|batch_.*" // 생성에서 제외할 테이블 (정규식 사용 가능)
                }

                target.apply {
                    packageName = jooqPackagePath
                    directory = jooqGeneratorOutputDir
                }

                generate.apply {
                    isRecords = true
                    isDaos = false // DAO 클래스는 생성하지 않음
                    isPojos = true
                    isFluentSetters = true
                    isJavaTimeTypes = true // 날짜/시간 타입을 Java 8+ Time API로 매핑

                    // Kotlin NotNull 속성을 non-nullable 타입으로 생성
                    isKotlinNotNullPojoAttributes = true
                    isKotlinNotNullRecordAttributes = true
                    isKotlinNotNullInterfaceAttributes = true

                    isPojosAsKotlinDataClasses = true
                }
            }
        }
    }
}
