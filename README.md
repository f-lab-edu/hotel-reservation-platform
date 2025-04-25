# 🏨 호텔 예약 플랫폼 (Hotel Reservation Platform)

> Java/Spring 기반의 숙소 예약 플랫폼 백엔드 프로젝트입니다.  
> 고객, 업체(호스트), 관리자 세 가지 사용자 유형에 맞춘 멀티 모듈 아키텍처를 설계하고,  
**도메인 중심 설계와 인증/인가 기능, 커서 기반 페이징 등** 실전 감각을 기르기 위한 사이드 프로젝트입니다.

<br>

## 🛠️ 기술 스택

- Java 21
- Spring Boot, Spring Security
- JPA, MySQL
- Redis, JWT
- Gradle (멀티 모듈)
- Docker
- QueryDSL

---

## 🗺️ 모듈 설계

> “모듈은 기능이 아니라 **책임**으로 나눈다”를 원칙으로 설계했습니다.  
> `core ↔ client` 이중 계층을 통해 **공통 코드와 비즈니스 코드**를 물리적으로 격리합니다.

<br>

### 모듈 의존 관계도

#### Core <-> Client & File Upload

<img width="500" alt="image" src="etc/img/module-relation.png"/>

<br>

#### Core 모듈 내부

<img width="500" alt="image" src="etc/img/core-module-relation.png"/>

<br>

#### 전체 모듈 관계

<img width="500" alt="image" src="etc/img/all-module-relation.png"/>

<br>

### 1. 설계 원칙

| 원칙                   | 왜 필요한가?                                                              |
|----------------------|----------------------------------------------------------------------|
| 도메인 규칙과 저장소·프레임워크 분리 | 엔티티는 “데이터 구조”, 비즈니스 로직·정책은 클라이언트 모듈에서 책임<br/>→ 도메인을 필요 이상으로 오염시키지 않음 |
| core ↔ client 단방향 의존 | core 코드는 어떤 클라이언트도 몰라야 함.<br/>클라이언트가 core를 호출하되, 역의존은 없음             |
| 클라이언트 독립 배포          | admin, host, customer, batch, file-upload는 각각 개별 애플리케이션으로 배포         |
| 쿼리 구현 책임 하행          | 실제 쿼리가 필요한 곳(=클라이언트)에서만 JPA, QueryDSL 활용하여 구현                        |

<br>

### 2. 각 모듈 책임

| 모듈                 | 역할 / 포함 내용                                                                                            |
|--------------------|-------------------------------------------------------------------------------------------------------|
| core-domain        | 주요 도메인, 공통 식별자(Entity)                                                                                |
| core-auth          | JWT AccessToken & RefreshToken 발급, JwtAuthenticationFilter , SecurityFilter, @LoginUserId, @LoginUser |
| core-support       | 요청/응답 로깅, 전역 응답(ApiResponse), 글로벌 예외, ErrorCode 등                                                     |      
| core-query-support | 공통 PageableUtils, 공통 CursorUtils                                                                      | 
| client-admin       | 내부 관리자 API (약관 관리)                                                                                    |    
| client-host        | 숙박업체 API (로그인, 숙소 정보, 객실 정보 관리)                                                                       |    
| client-customer    | 일반 고객 API (회원 가입, 인증 번호 발송, 로그인, 숙소 예약 등)                                                             | 
| client-batch(예정)   | 웹 트래픽과 무관한 ‘백오피스 작업자’ 역할                                                                              |
| file-upload        | 파일 업로드 전용 API (S3 Mock), 외부 노출용 단독 서비스                                                                |

<br>

### 3. 의존성 규칙

- core-* 모듈은 절대 다른 core 모듈 외에는 의존하지 않는다.
- client-* 는 필요한 core 모듈만 선택해 의존한다.

<br>

### 4. 개발 가이드

#### 📚 새 도메인 추가 절차

1. Entity → core-domain
2. 필요한 비즈니스 쿼리 → 해당 client- 모듈에서 Repository 구현
3. 공통 상수·Enum이 필요하면 core-support 에서 관리

<br>

#### 🔐 보안 설정 확장

- 새로운 skipUrls 가 필요하면 JwtProperties.skipUrls(yaml) → SecurityConfig 자동 반영
- 외부 API 호출 시 CircuitBreaker + Retry 디자인은 core-auth → 공통 유틸 제공 예정

## 💡 주요 기능 및 설계

### ✅ 1. 약관 도메인 설계 및 커서 기반 페이징

- 고객 대상 약관 동의 기능 구현
- 정렬 필드를 Enum + Generic 구조로 추상화하여 QueryDSL 기반 Keyset 페이징 지원

### ✅ 2. 회원가입 및 인증 / 인가

- 인증번호 발송 및 검증 (Redis TTL 기반)
- JWT Access/Refresh Token 발급 및 보안 처리
- `@LoginMember` ArgumentResolver 직접 구현하여 유저 컨텍스트 주입 처리

### ✅ 3. Security 커스터마이징

- JwtFilter 등록 및 인가 처리 로직 명확화
- CustomAuthenticationEntryPoint 구현 및 AuthErrorType 설계
- 권한 기반 접근 제어 적용

### ✅ 4. 업체 숙박 정보 관리 - 숙소 등록 / 객실 정보 관리

- 숙소 등록 및 객실 정보 관리 기능 구현
- 숙소 이미지 업로드 -> 파일 업로드 모듈 분리
- 예약 가능한 기간 설정 및 예약 가능 여부 체크

<br />

## 🔗블로그

- [Blog Link](https://pablo7.tistory.com/)

---

## 📌 진행 상황

- ✅ 핵심 인증/인가 기능 구현 완료
- ✅ 약관 관리 기능 + Keyset 페이징 구현 완료
- ✅ 업체 숙소 등록 / 객실 관리 기능 구현 완료
- ⏳ 숙박 가용 정보 자동화 Batch 구현 진행 중
- ⏳ 일반 고객 숙소 검색 및 예약 구현 예정
- 🔄 커밋 이력 및 설계 과정은 GitHub + 블로그로 지속 기록 중입니다

---

## 🙋‍♂️ 프로젝트 목표

- 도메인 중심 아키텍처와 구조적 책임 분리를 직접 경험
- 사용자 유형에 따라 API를 분리하고, 공통 기능은 모듈화하여 의존 흐름을 명확하게 설계
- **대규모 트래픽을 고려한 실전 설계 훈련**
- **지속 가능한 설계 → 확장 가능한 코드**를 고민하며 성장 중입니다
