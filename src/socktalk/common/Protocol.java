package socktalk.common;

public class Protocol {
    public static final int PORT = 54321;
    public static final String SEP = "|";

    // ===== 필수구현 =====
    // 클라이언트 -> 서버
    public static final String LOGIN = "LOGIN";          // LOGIN|아이디|비밀번호
    public static final String LOGOUT = "LOGOUT";

    public static final String STATUS = "STATUS";			// STATUS|상태

    // 서버 -> 클라이언트
    public static final String LOGIN_OK = "LOGIN_OK";    // LOGIN_OK|아이디|이름|부서
    public static final String LOGIN_FAIL = "LOGIN_FAIL";// LOGIN_FAIL|사유
    
    public static final String USER_LIST = "USER_LIST"; 	// USER_LIST|n|(아이디|이름|부서|상태)xn
    public static final String USER_JOIN = "USER_JOIN";		// USER_JOIN|아이디|이름|부서|상태
    public static final String USER_LEAVE = "USER_LEAVE";	// USER_LEAVE|아이디
    public static final String PRESENCE = "PRESENCE";		// PRESENCE|아이디|상태
    

    // ===== 추가시 =====
}