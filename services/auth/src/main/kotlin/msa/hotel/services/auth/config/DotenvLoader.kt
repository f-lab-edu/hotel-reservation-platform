package msa.hotel.services.auth.config

import io.github.cdimascio.dotenv.Dotenv
import java.io.File

/**
 * 로컬 실행 환경과 Docker 컨테이너 환경을 모두 감지하여
 * .env 또는 .env.local 파일을 로드하고 시스템 프로퍼티로 설정하는 유틸리티 객체
 */
object DotenvLoader {
    fun load() {
        // 1. 먼저 현재 위치(./)에서 .env 파일을 찾습니다.
        //    (Docker 컨테이너 안에서는 이 경로가 사용됩니다.)
        var envFile = File(".env")
        var sampleFile = File(".env.local")

        // 2. 만약 현재 위치에 파일이 없다면, 하위 폴더 구조로 다시 찾습니다.
        //    (로컬에서 프로젝트 루트 폴더에서 실행할 때 이 경로가 사용됩니다.)
        if (!envFile.exists() && !sampleFile.exists()) {
            envFile = File("services/auth/.env")
            sampleFile = File("services/auth/.env.local")
        }

        // 3. 최종적으로 존재하는 파일 경로를 결정합니다.
        val envFileName =
            when {
                envFile.exists() -> envFile.path
                sampleFile.exists() -> sampleFile.path
                else -> null // .env와 .env.local 둘 다 없을 경우
            }

        // 4. 파일이 존재하면, 내용을 읽어 시스템 프로퍼티로 설정합니다.
        if (envFileName != null) {
            val dotenv =
                Dotenv
                    .configure()
                    .filename(envFileName)
                    .ignoreIfMissing() // 혹시 모를 상황에 대비
                    .load()

            dotenv.entries().forEach { entry ->
                System.setProperty(entry.key, entry.value)
            }
            println("✅ Loaded environment variables from: $envFileName")
        } else {
            // 파일이 전혀 없을 경우 경고 메시지를 출력합니다.
            println("⚠️ .env or .env.local file not found. Running with default properties.")
        }
    }
}
