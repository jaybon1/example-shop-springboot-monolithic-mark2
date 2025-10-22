# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/example/shopmark2` follows a vertical slice layout; each domain (`product`, `user`, `order`, `payment`) groups its `domain`, `application`, and `presentation` packages by responsibility.
- `src/main/resources` stores Spring configuration, SQL assets, and generated docs mirrored to `build/resources/main/static/springdoc`.
- Keep tests in `src/test/java`, mirroring production packages; share reusable fixtures under the `global` helpers.

## Build, Test, and Development Commands
- `./gradlew clean build` cleans output, compiles with Java 17, runs unit/integration tests, and regenerates API docs.
- `./gradlew bootRun` boots the application with the default profile (H2 + seed data) for local verification.
- `./gradlew test` executes the test suite without rebuilding docs; run before opening a pull request.
- `./gradlew setDocs` refreshes REST Docs and OpenAPI artifacts into `build/api-spec` for reviewers.

## Testing Guidelines
- Prefer Spring Boot + JUnit 5 with MockMvc; scope tests using `@WebMvcTest` for controllers and `@SpringBootTest` for full flows.
- Mirror production packages under `src/test/java`; end test classes in `Tests` with methods in `shouldDoXWhenY` format.
- Regenerate REST Docs snippets when API contracts shift so the published OpenAPI spec stays trustworthy.

## Commit & Pull Request Guidelines
- Follow Conventional Commits (`feat:`, `fix:`, `chore:`); keep the subject imperative and under 72 characters.
- Limit each pull request to one logical change; include a summary, test evidence, and linked issue or ticket.
- Capture breaking API or UI updates with screenshots or refreshed docs, and verify `./gradlew build` locally before review.

## Security & Configuration Notes
- Keep secrets in environment variables or profile-specific `application-*.yml` files; never commit credentials.
- Authentication logic resides under `global.infrastructure.config.security`; reuse existing filters before adding new ones.
- The default profile uses in-memory H2; document any datasource overrides needed for other environments.

## Coding Style & Naming Conventions
- Use 4-space indentation and Lombok builders/getters to keep entities and DTOs concise; avoid manual accessor code.
- Packages stay domain-first (`product.presentation.controller`, `global.infrastructure.config`); align DTOs and services with the same version suffix (`V1`).
- REST endpoints should return `ResponseEntity<ApiDto<...>>`, wrapping payloads with `ApiDto.builder().data(...)`.
- Name request/response DTOs using `Req...DtoV1` and `Res...DtoV1`, and model primary keys with `UUID`.
```
// 개발에서 변수명, 함수명 만큼은 짧은 것 보다 긴 것이 낫다
// 1년 뒤에 봤을 때에 바로 이해가 가능한 이름으로 짓기

// 일반 변수 및 지역 변수
// 의미 불명의 변수명은 테스트 용도 이외에는 지양
// num, aaa, bbb (X)
// workCount (O)                                       work_count
// NeedToWorkCount (O)                                 need_to_work_count

// 타입(Enum)
// CREATING, CREATED, WATING, PENDING, CANCELED, COMPLETED (O)

// 디비 컬럼명, 변수
// 기본적으로 시간은 아래와 같이 사용
LocalDateTime createdAt
// 나머지 컬럼은 동사도 명사처럼 사용가능하기 때문에 
// 과거형이나 명사형으로 변환하지 않아도 무방
// 예를들어 Order는 [주문하다] 라는 동사와 [주문]이라는 명사로 둘 다 사용가능
class ForkJoinPool
GetStaticIpRequest getStaticIpRequest
LocalDateTime createDateTime

// Boolean
boolean running;
Boolean slotDeleted;
Boolean initialized;

// 엔티티 (모델)
// suffix로 Entity를 붙이는 것을 권장
TempEntity tempEntity (O)                                  temp_entity
Temp temp (X)


// 엔티티 로그
TempLogEntity

// 엔티티 하위 엔티티
FileEntity / FileSubEntity (O)
FileMasterEntity / FileEntity (O)
SubFileEntity (사용해도 무방하지만 파일 정렬이 abc 순이라면 보기 어려울 수 있음)

// 객체 Instance
TempEntity tempEntity = tempRepository.findById(1);

// 단, 변수 뒤에 for, by, of, with 등으로 의미 추가하는 경우 영어 문법에 맞는 쪽으로 작성
// for와 by 뒤에는 명사나 동명사를 권장함(동사원형도 무방)
TempEntity forSavingTempEntity (DTO -> Entity)
TempEntity savedTempEntity = tempRepository.save(forSavingTempEntity);
TempEntity forCheckingTempEntity = tempRepository.findById(1);
TempEntity forCheckingDuplicateRequestTempEntity
TempEntity byFilteringTempEntity
TempEntity filteredTempEntity
TempEntity ofAdminTempEntity
TempDTO tempDTO

// 옵셔널
// 변수명 뒤에 제네릭 클래스명 작성
Optional<TempEntity> tempEntityOptional = tempRepository.findById(1);

// List / Map / Set 등 컬렉션
// 변수명 뒤에 제네릭 클래스명 작성
// 복수형 등 가변적인 suffix는 지양 (game -> games / fly -> flies / woman -> women (X))
List<TempEntity> tempEntityList = tempRepository.findAll();
List<TempEntity> forFilteringTempEntityList (필터링을 목적으로 생성한 리스트)
List<TempEntity> forFilteringStoreNameTempEntityList (스토어명 필터링을 목적으로 생성한 리스트)
List<TempEntity> byFilteringTempEntityList (필터링 한 리스트)
List<TempEntity> filteredTempEntityList (필터링 한 리스트)
List<TempEntity> byFilteringStoreNameTempEntityList (스토어명으로 필터링 한 리스트)
List<TempEntity> byFilterForSortTempEntityList (필터링 한 리스트이며 정렬 예정)

// 페이지 객체
// 변수명 뒤에 제네릭 클래스명 작성
Page<TempEntity> tempEntityPage = tempRepository.findAll();

// String, int, Integer와 같은 기본형, 래퍼형, String은
// 변수명에서 타입을 유추 가능한 경우 타입 생략
String storeName = "아름드리팬션";                    store_name = "아름드리팬션"
int maxCount = 99;                                    max_count = 99
Integer maxCount = 99;

// 변수명에서 타입을 유추하기 어려운 경우 타입 붙이기
String maxCount = "99"; (X)
String maxCountString = "99"; (O)

// 래퍼형, String을 타입으로 쓰는 제네릭의 경우
// 변수명에서 타입을 유추 가능한 경우 타입 생략
List<String> jsonList;                                     json_list
List<Integer> workCountList;                               work_count_list
Set<String> fruitSet;                                      fruit_set

// 제네릭 안에 제네릭이 있는 경우 내부에서 외부 순서로 작성
List<Map<String, Object>> jsonMapList;                     json_map_list
List<List<String>> phoneListList;

// 다차원 리스트를 표현할 때
List<List<String>> phone2DList;                            phone_2d_list
List<List<List<String>>> phone3DList;

// array
// 배열은 suffix으로 Array를 붙이거나 복수형 사용
TempEntity[] tempEntityArray

// 다차원 배열을 표현할 때
int[][] index2DArray                                        index_2d_array
int[][][] index3DArray
```
```
// 개발에서 변수명, 함수명 만큼은 짧은 것 보다 긴 것이 낫다
// 1년 뒤에 봤을 때에 바로 이해가 가능한 이름으로 짓기

// 의미 불명의 함수명은 테스트 용도 이외에는 지양
// a(), getWow() (X)
// save() (O) 특정 도메인의 Service에서 다른 작업을 하지 않고 엔티티 삽입만 할 경우
// checkRemainCountByUrl() (O)
// check_remain_count_by_Url()
// findByMaintainCookieDateAfterOrderByLastWorkDateAsc() (O)
// find_by_maintain_cookie_date_after_order_by_last_work_date_asc()

// 함수의 핵심 동작은 동사원형으로 작성하는 것을 선호
// testSaveSuccessByNoMemo() -> test
// test_save_success_by_no_memo()

// testSaveFailByBadUrl() -> test
// test_save_fail_by_bad_url()

// 리액트 생명주기 함수, componentMounted()가 아니라 동사원형을 쓰기 위해 did와 will을 사용함
// componentDidMount() -> mount
// componentWillMount() -> mount
```