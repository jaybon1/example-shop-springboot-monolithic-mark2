# AGENTS 작업 컨벤션

## 1. 문서 목적
- 이 문서는 본 레포지토리에서 에이전트가 수행해야 할 기본 원칙과 작업 흐름을 정리한다.
- Spring Boot 모놀리식 애플리케이션(`example-shop-springboot-monolithic-mark2`) 유지보수 및 기능 개발 시 아래 규칙을 우선 준수한다.

## 2. 개발 환경 및 핵심 명령
- Java 17 + Spring Boot 기반. Gradle Wrapper(`./gradlew`)만 사용한다.
- 주요 명령
  - `./gradlew clean build` : 전체 빌드, 테스트, API 문서 재생성.
  - `./gradlew test` : 테스트만 실행. PR 전 반드시 수행.
  - `./gradlew bootRun` : 기본 프로필(H2 + seed data)로 서버 기동.
  - `./gradlew setDocs` : REST Docs & OpenAPI 산출물(`build/api-spec`) 최신화.
- 빌드 실패 혹은 테스트 누락이 의심될 경우 로컬에서 즉시 재확인한다.

## 3. 프로젝트 구조 원칙
- 소스 루트: `src/main/java/com/example/shop`.
- 도메인 단위 수직 슬라이스 구조 유지 (`<domain>.domain|application|presentation`).
- 공통 인프라/설정은 `global.infrastructure`, `global.common`에 위치.
- 리소스: `src/main/resources` → Spring 설정, SQL 스크립트, 생성된 문서. 빌드시 `build/resources/main/static/springdoc` 로 동기화된다.
- 테스트: `src/test/java`에서 프로덕션 패키지를 그대로 미러링하고, 공용 픽스처는 `global` 하위에 둔다.

## 4. 작업 절차
1. 최신 `main` 반영 후 브랜치 생성.
2. 변경 범위 정의 → 필요시 `update_plan` 도구로 작업 목록 공유.
3. 구현 중 빈번히 `./gradlew test` 로 회귀 검증.
4. 변경된 파일의 불필요한 diff 여부 확인 후 커밋.
5. PR 작성 시 변경 요약, 테스트 결과, 관련 이슈 링크를 남긴다.

## 5. 코딩 컨벤션

### 5.1 공통 스타일
- 들여쓰기 4칸. Lombok 빌더/게터를 활용하며 수동 접근자 작성 금지.
- DTO, 서비스, 컨트롤러는 버전 suffix (`V1`) 일치. 예: `ProductServiceV1`, `ReqCreateProductDtoV1`.
- REST 컨트롤러 응답은 `ResponseEntity<ApiDto<...>>` 형태로 감싼다. 본문은 `ApiDto.builder().data(...).build()`로 구성.

### 5.2 패키지 및 모듈
- 패키지명은 도메인 우선(`product.presentation.controller`)으로 구성한다.
- 설정/보안 관련 로직은 `global.infrastructure.config.security` 내 기존 필터 및 설정을 재활용한다.

### 5.3 엔티티 및 DTO
- 엔티티 클래스명은 `*Entity` suffix 사용. 예: `OrderEntity`.
- 기본 키는 `UUID`.
- 요청 DTO: `Req<Method><URI>DtoV1`, 응답 DTO: `Res<Method><URI>DtoV1`.
- 하위 엔티티/로그 엔티티 명명 패턴:
  - 로그: `*LogEntity`
  - 하위 엔티티: `FileEntity`, `FileSubEntity`, `FileMasterEntity` 등 직관적으로 작성.

### 5.4 변수, 컬렉션, Optional
```
// 의미가 분명한 이름만 사용 (실험용 코드 제외)
int workCount;                    // O
Optional<ProductEntity> productEntityOptional;   // Optional 타입 명시
List<OrderEntity> orderEntityList;               // 제네릭 타입 suffix 명시
Page<UserEntity> userEntityPage;

// 시간 컬럼은 createdAt / updatedAt 통일
LocalDateTime createdAt;
LocalDateTime updatedAt;

// Boolean
boolean running;
Boolean slotDeleted;

// 목적을 나타내는 접미어 사용
ProductEntity forSavingProductEntity;
ProductEntity savedProductEntity;
ProductEntity forCheckingDuplicateRequestProductEntity;
```
- 컬렉션 변수명은 제네릭 타입을 suffix로 덧붙이고 복수형 변형은 지양한다.
- 다차원 컬렉션/배열은 차원을 명시한다(`phone2DList`, `index2DArray`).

### 5.5 함수/메서드 명명
```
// 불필요하게 짧은 이름 금지
checkRemainCountByUrl();
findByMaintainCookieDateAfterOrderByLastWorkDateAsc();

// 테스트 메서드: shouldDoSomethingWhenCondition 형태
void shouldReturnProductsWhenUserRequestsList();

// React 생명주기 등은 동사원형 선호 (mount, update 등)
```
- 테스트 클래스 이름은 `XxxTests`.
- 기능이 하나뿐인 서비스 메서드라면 `save`, `delete` 등 동사원형 사용 가능.

## 6. API·문서 관리
- API 스펙 변경 시 REST Docs 스니펫 재생성 필요 → `./gradlew setDocs`.
- 문서 산출물은 `build/api-spec` 및 `build/resources/main/static/springdoc` 확인.
- 변경된 API는 PR 본문에 영향도를 명시한다 (예: 필드 추가/삭제).

## 7. 테스트 가이드라인
- 컨트롤러 단위: `@WebMvcTest`, 서비스/리포지토리 통합 흐름: `@SpringBootTest`.
- 테스트 패키지 구조는 프로덕션 패키지와 동일하게 유지.
- 테스트 클래스 내부 메서드는 `shouldDoXWhenY` 패턴을 따른다.
- MockMvc 선호. 필요 시 Test Fixture는 `global` 하위 헬퍼 재사용.

## 8. 커밋 및 PR 규칙
- Conventional Commits 적용: `feat:`, `fix:`, `chore:`, `test:`, `docs:` 등.
- 커밋 메시지는 명령형, 72자 제한.
- 하나의 PR은 한 가지 논리적 변경만 포함. 테스트 결과와 관련 이슈를 본문에 첨부.
- PR 전 `./gradlew clean build` 또는 최소 `./gradlew test` 수행 결과를 확인한다.

## 9. 보안·설정
- 비밀정보는 환경변수 또는 `application-*.yml` 프로필 파일을 사용한다. 깃에 커밋 금지.
- 기본 프로필은 인메모리 H2를 사용. 다른 데이터소스를 사용하려면 README/문서에 설정 방법을 기록한다.
- 인증/인가 필터 추가 시 기존 `global.infrastructure.config.security` 구성요소를 우선 검토 후 확장한다.

## 10. 참고 예시 (Naming & 함수)
```
// 변수 예시
List<ProductEntity> forFilteringCategoryProductEntityList;
List<ProductEntity> filteredProductEntityList;

// 함수 예시
void checkRemainCountByUrl();
void testSaveSuccessByNoMemo();
void testSaveFailByBadUrl();
```

---
- 본 문서에 없는 규칙은 기존 코드 스타일을 참고하여 일관성을 유지한다.
- 불명확한 사항은 PR이나 커밋 전 유지보수자, 문서 작성자에게 확인한다.
