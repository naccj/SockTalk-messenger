package socktalk.common;

public class Protocol {
    public static final int PORT = 54321;
    public static final String SEP = "|";

    // ===== 필수구현 =====
    // 클라이언트 -> 서버
    public static final String LOGIN = "LOGIN";          // LOGIN|아이디|비밀번호

    // 서버 -> 클라이언트
    public static final String LOGIN_OK = "LOGIN_OK";    // LOGIN_OK|이름|부서
    public static final String LOGIN_FAIL = "LOGIN_FAIL";// LOGIN_FAIL|사유

    // ===== 추가시 =====
}