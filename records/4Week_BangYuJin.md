## Title: [4Week] Bang YuJin

### 📄 미션 요구사항 분석 & 체크리스트

---
- 네이버 클라우드 플랫폼을 통한 배포, 도메인, HTTPS 적용
  - ☑️ www.ujineeee.site 접속 가능
  - ☑️ 소셜 로그인, 인스타 아이디 연결

- 내가 받은 호감 리스트(/usr/likeablePerson/toList)에서 성별 필터링 기능 구현
    - ☑️ 특정 성별을 가진 사람들만 필터링 하기

- 내가 받은 호감 리스트(/usr/likeablePerson/toList)에서 호감 정보 필터링 기능 구현
    - ☑️ 특정 호감 정보를 가진 사람들만 필터링 하기

### 🦁 3주차 미션 요약

---

**[접근 방법]**
- 필수 미션 - 네이버 클라우드 플랫폼을 통한 배포, 도메인, HTTPS 적용
  - SSL 도입해 HTTPS 적용
  - `https://www.gabia.com/` 에서 도메인 `ujineeee.site` 구입
  - NPM 설치 후 `www.ujineeee.site` 요청 `172.17.0.1:8080` 으로 넘기기
  - `Dockerfile` 생성, `gram` 도커 이미지 생성 후 실행
  - https://www.ujineeee.site 접속

- 필수 미션 - 내가 받은 호감 리스트에서 성별 필터링 기능
  - Stream의 `filter()` 메소드를 이용해 성별을 필터링함.

- 선택 미션 - 내가 받은 호감 리스트에서 호감 정보 필터링 기능
  - Stream의 `filter()` 메소드를 이용해 호감 정보를 필터링함.