## Title: [1Week] Bang YuJin

### 📄 미션 요구사항 분석 & 체크리스트

---
☑️ 호감 상대 삭제 구현

☑️ OAuth2 클라이언트를 이용한 구글 로그인 기능 구현

### 🦁 1주차 미션 요약

---

**[접근 방법]**
 - 필수미션 - 호감 상대 삭제
   - 호감 상대를 삭제하기 위해서는 호감 목록의 페이지를 관리하는 LikeablePersonController에 접근해야 한다.
   - 삭제 버튼을 클릭했을 때 url 주소가 /delete/id=? 인 것을 확인하고 LikeablePersonController에 "/delete/{id}"를 매핑해야겠다고 생각했다. 
   - id는 likeable_person 테이블의 id를 나타낸다.
   - LikeablePersonService에 findById 메서드를 구현해 해당 id를 가진 LikeablePerson의 데이터를 받는다.
   - 그리고 LikeablePersonService에 delete 메서드를 구현하여 해당 데이터를 LikeableRepository.delete를 이용해 삭제할 수 있다.
   - rq.redirectWithMsg 메서드를 통해 데이터가 잘 삭제되었다면 "{username}이 삭제되었습니다"라고 알리고 호감 목록 페이지로 돌아간다.
   - 만약 로그인된 사용자의 인스타아이디와 해당 데이터의 from_insta_member_id에 연결된 사용자가 다르다면 "삭제 권한이 없습니다."라고 경고한다. 
 <br></br>
 - 선택미션 - 구글 로그인
   - 구현되어 있는 kakao 로그인 구현 방법을 참고한다.
   - google 로그인 기능은 https://console.cloud.google.com/ 통해서 client를 생성할 수 있다.
   - google은 kakao와 달리 로그인을 구현하기 위한 provider가 spring security의 라이브러리에 내장되어 있어 application.yml에 따로 저장할 필요 없다.

[참고] 

https://deeplify.dev/back-end/spring/oauth2-social-login