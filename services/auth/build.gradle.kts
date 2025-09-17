import org.jooq.meta.jaxb.ForcedType

dependencies {
    implementation(project(":modules:id-generator"))
    implementation(project(":modules:jwt"))

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    jooqGenerator("com.mysql:mysql-connector-j")
}

val dbSchema = "auth-db"
val dbUrl = project.findProperty("db.url") as? String ?: "jdbc:mysql://localhost:30306/$dbSchema"
val dbUser = project.findProperty("db.user") as? String ?: "root"
val dbPassword = project.findProperty("db.password") as? String ?: "root"

jooq { // jOOQ 코드 생성 설정
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
