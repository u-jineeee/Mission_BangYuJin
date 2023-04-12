## Title: [2Week] Bang YuJin

### 📄 미션 요구사항 분석 & 체크리스트

---
- 호감 표시 예외 처리 케이스 3가지
  - Case1: 중복된 유저에게 호감 표시 불가능
    ☑️로그인된 유저의 호감 표시 목록에서 호감 표시 등록할 유저와 인스타계정이 동일한 데이터가 있다면 `rq.historyBack` 실행
  - Case2: 호감 표시할 수 있는 최대 인원은 10명으로 제한
    ☑️새로운 호감 표시 유저 등록 시 로그인된 유저의 호감 표시 목록 `size`를 받아와 10명 이상이라면 `rq.historyBack` 실행
  - Case3: 중복된 유저에게 호감 표시 시 호감 정보가 다를 경우 수정으로 처리
    ☑️새롭게 호감 표시할 유저의 인스타 계정은 같으나 호감 정보가 다르다면 원래 등록했던 유저의 호감 정보를 수정하여 처리

- OAuth2 클라이언트를 이용한 네이버 로그인 구현
  ☑️https://developers.naver.com/main/ 에서 `Client ID`와 `Client Secret` 생성
  ☑️`Client ID`와 `Client Secret`은 `application-secret.yml`에 저장
  ☑️`application.yml`에 네이버 oauth2 설정

### 🦁 2주차 미션 요약

---

**[접근 방법]**
- 필수 미션 - 호감 표시 예외 처리 3가지
  - Case1: 중복된 유저에게 호감 표시 불가능
    - `LikeablePersonService`의 `like` 메서드에 접근.
    - 로그인된 유저가 호감 표시한 리스트를 저장해둔 `FromLikeablePeople` 변수에 접근하여 새로 등록할 인스타 계정과 동일한 데이터가 있는지 검사.
    - 동일한 데이터가 있다면 실패 메시지를 띄우고 `rq.historyBack`을 실행.
  - Case2: 호감 표시할 수 있는 최대 인원은 10명으로 제한
    - 호감 표시 유저 등록 시 로그인된 유저의 호감 표시한 리스트의 size를 받아와 10개 이상의 데이터가 있는지 검사.
    - 데이터의 개수가 10개 이상이라면 실패 메시를 띄우고 `rq.historyBack`을 실행.
  - Case3: 중복된 유저에게 호감 표시 시 호감 정보가 다를 경우 수정으로 처리
    - Case1에서 검사 시 인스타 계정은 같지만 호감 정보가 다르다면 로그인된 유저의 호감 목록의 데이터를 수정.
    - `setAttractiveTypeCode` 메서드를 사용해 입력받은 호감 정보로 바꾼 후 수정되었다는 메시지를 띄움.

- 선택 미션 - 네이버 로그인
  - 로그인 API를 받아오기 위해서 https://developers.naver.com/main/ 에서 `Client ID`와 `Client Secret`을 생성.
  - 네이버는 spring security를 공식적으로 지원하지 않기 때문에 `application.yml`에 `registration` 과 `provider`를 수동으로 설정.
  - 구글, 카카오 로그인과 같이 식별자 아이디를 받아 왔으나 ex) `{"id": 2731659195}` 으로 표현되어 `CustomOAuth2UserService` 에서 `substring`을 이용해 `id` 부분만 잘라내어 저장.