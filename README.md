# Sock-Talk

**Java Socket 기반 사내 메신저 프로그램**

팀 **모루**

관리자가 계정을 생성할 때 지정한 부서에 따라 로그인하면 해당 부서 단체방으로 자동 연결되고, 다른 부서와는 1:1 통신만 허용하는 폐쇄형 협업 메신저입니다.

## 팀원

| 이름 | 담당 |
|---|---|
| 박지수 |  서버 (다중 접속, 로그인·계정 파일, 방 관리·브로드캐스트, 1:1·예외 처리, 관리자 화면), `main` 병합 |
| 정채원 | 클라이언트 (Swing 화면 설계·구현, 수신 스레드 처리, 이모티콘·UI 보완) |

공동: 프로토콜 설계, 테스트, 보고서, 발표

## 주요 기능

- 다중 접속 처리
- 기존 계정 로그인 (부서 정보 포함)
- 로비 전체 채팅과 접속자 목록
- 1:1·멀티 채팅 (부서 단체방 자동 연결, 타 부서와는 1:1만 허용)
- 이모티콘, 파일 전송
- 입장/퇴장 메시지, 시간 표시, 비정상 종료 처리
- 채팅방 공지
- 활동상태 (온라인/오프라인/자리비움 등)
- 방 만들기
- 관리자 화면(계정 추가·부서 이동)

<!-- 추가시: 다른 사원 정보 조회, 즐겨찾기, 금칙어 필터링 -->

## 기술 스택

- Java (JDK 21)
- Java Socket (TCP), 멀티스레드
- Swing
- Eclipse, Git/GitHub

## 실행 방법

> **서버를 먼저 실행합니다. 서버 실행시 관리 창이 함께 열립니다. 그 다음 클라이언트를 실행합니다.**

1. 저장소를 clone 하고 Eclipse에서 `Import → Existing Projects into Workspace`로 불러옵니다.
2. 인코딩을 **UTF-8**로 설정합니다. (`Window → Preferences → General → Workspace → Text file encoding`)
3. `src/socktalk/server/ServerMain.java`를 실행합니다.
4. `src/socktalk/client/ClientMain.java`를 실행합니다. 여러 명을 테스트하려면 여러 번 실행합니다.

**테스트 계정** (`data/accounts.txt`, 예시이며 실제 값은 파일을 따릅니다)

| 아이디 | 비밀번호 | 이름 | 부서 |
|---|---|---|---|
| test1 | 1234 | 테스트1 | 개발 |
| test2 | 1234 | 테스트2 | 영업 |

## 폴더 구조

```
sock-talk/
├─ README.md
├─ .gitignore
├─ .gitattributes
├─ lib/
├─ docs/
│  ├─ protocol.md
│  ├─ convention.md
│  ├─ ui/
│  ├─ proposal/
│  └─ meeting/
├─ resources/
│  ├─ emoticons/
│  └─ icons/
├─ data/
│  ├─ accounts.txt               # 아이디|비밀번호|이름|부서
│  # 추가시: favorites.txt (즐겨찾기)
│  # 추가시: banned-words.txt (금칙어 필터링)
└─ src/socktalk/
   ├─ common/
   │  ├─ Protocol.java           # 필수 / 추가시 구역 분리
   │  └─ Message.java
   ├─ server/                    # 박지수
   │  ├─ ServerMain.java
   │  ├─ ClientHandler.java
   │  ├─ AccountStore.java       # 로그인 검증, 계정 추가, 부서 이동
   │  ├─ RoomManager.java        # 부서 단체방, 1:1·멀티, 방 만들기
   │  ├─ DepartmentPolicy.java   # 타 부서와는 1:1만 허용
   │  ├─ PresenceManager.java    # 활동상태 (대화가능, 자리비움 등)
   │  ├─ NoticeManager.java      # 채팅방 공지
   │  ├─ FileTransferHandler.java
   │  ├─ admin/                  # 관리자 화면 (서버 창, 이 패키지만 Swing 허용)
   │  │  ├─ AdminFrame.java
   │  │  └─ UserTablePanel.java
   │  # 추가시: FavoriteStore.java (즐겨찾기)
   │  # 추가시: BannedWordFilter.java (금칙어 필터링)
   └─ client/                    # 정채원
      ├─ ClientMain.java
      ├─ Theme.java
      ├─ net/
      │  └─ ConnectionManager.java
      └─ ui/
         ├─ LoginFrame.java
         ├─ MainFrame.java            # 내 프로필, 접속자·부서 구성원, 활동상태
         ├─ ChatListPanel.java        # 대화목록
         ├─ ChatFrame.java            # 대화창
         ├─ CreateRoomDialog.java     # 방 만들기
         ├─ StatusMenu.java           # 활동상태 선택 메뉴
         ├─ ProfileDialog.java        # 내 정보
         ├─ component/                # BubblePanel, EmoticonPicker 등
         # 추가시: UserInfoDialog.java (다른 사원 정보 조회)
```

## 화면 구성

| 화면 | 설명 |
|---|---|
| 로그인 | 아이디·비밀번호 입력 |
| 메인화면 | 내 프로필, 부서 구성원, 접속 상태 |
| 대화목록 | 참여 중인 대화방 목록 |
| 대화창 | 말풍선, 이모티콘, 파일 전송, 공지 |
| 내 정보 | 프로필 확인 |
| 방 만들기 | 초대할 구성원 목록 선택 |
| 관리자 화면 | 구성원 관리(부서, 비밀번호 변경 등) |

> 완성 후 화면 캡처를 `docs/ui/`에 추가하고 이 표에 연결합니다.

**색상 팔레트**

| 용도 | 색상 |
|---|---|
| 배경 | `#E5E8E2` |
| 보조 | `#CDE4E3` |
| 강조 | `#3AABD1` |
| 주요색 | `#668CAF` |

## 문서

- 메시지 명세 (`docs/protocol.md`)
- 개발 컨벤션: `docs/convention.md`

## 협업 규칙

- `main` 브랜치는 **박지수만** 병합합니다.
- 각자 작업 브랜치(`feature/...`)를 만들어 **본인 담당 파트**에 한해 작업합니다.
- 작업이 끝나면 PR을 만들고, 이상이 없으면 본인이 서브브랜치(예: `dev`)에 직접 병합합니다.
- 병합 후 연결된 이슈는 **작성자가 직접 close** 합니다.
- 공용 파일(`Protocol.java`, `docs/protocol.md`)은 변경 전에 상대방에게 알립니다.

## 일정

개발 기간: 2026년 10월 말 ~ 12월 중순

| 마일스톤 | 마감 | 내용 |
|---|---|---|
| M1 기반 | 11/15 | 다중 접속, 로그인, 화면 틀 |
| M2 핵심 | 11/22 | 로비 채팅, 접속자 목록, 활동상태, 부서 단체방 연결, 활동상태 |
| M3 기능 | 11/29 | 1:1·멀티 채팅, 권한 제어, 이모티콘·파일 전송, 공지, 방만들기, 관리자 화면 |
| M4 마무리 | 12/08 | 예외 처리, 통합 테스트, 보고서·발표 |