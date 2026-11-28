package socktalk.common;

public class Protocol {
    public static final int PORT = 54321;
    public static final String SEP = "|";

    // ===== 필수구현 =====
    // 클라이언트 -> 서버
    public static final String LOGIN = "LOGIN";		// LOGIN|아이디|비밀번호
    public static final String LOGOUT = "LOGOUT";

    public static final String STATUS = "STATUS";	// STATUS|상태

    public static final String LOBBY_NOTICE_SET = "LOBBY_NOTICE_SET";	// LOBBY_NOTICE_SET|내용 (빈 문자열 = 삭제)
    		
    // 서버 -> 클라이언트
    public static final String LOGIN_OK = "LOGIN_OK";    // LOGIN_OK|아이디|이름|부서
    public static final String LOGIN_FAIL = "LOGIN_FAIL";// LOGIN_FAIL|사유
    
    public static final String USER_LIST = "USER_LIST"; 	// USER_LIST|n|(아이디|이름|부서|상태)xn
    public static final String USER_JOIN = "USER_JOIN";		// USER_JOIN|아이디|이름|부서|상태
    public static final String USER_LEAVE = "USER_LEAVE";	// USER_LEAVE|아이디
    public static final String STATUS_CHANGED = "STATUS_CHANGED";	// STATUS_CHANGED|아이디|상태
    
    public static final String CHAT = "CHAT";			// CHAT|방ID|송신자ID|시간|내용
    public static final String ROOM_LIST = "ROOM_LIST"; // ROOM_LIST|n|(방ID|이름|종류)×n
    
    public static final String LOBBY_NOTICE = "LOBBY_NOTICE";	// LOBBY_NOTICE|게시자ID|내용
    // ===== 추가시 =====
}