package socktalk.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import socktalk.common.Message;
import socktalk.common.Protocol;


/*
 *  접속자 한명을 담당, 한 줄씩 받아 message로 해석 및 처리
 */
public class ClientHandler extends Thread {
	private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
	
	public static final String STATUS_AVAILABLE = "AVAILABLE";
	public static final String STATUS_OFFLINE = "OFFLINE";	
	public static final String STATUS_AWAY = "AWAY";	
	public static final String STATUS_BUSY = "BUSY";	
	public static final String STATUS_DND = "DND";	
	
	private Socket socket;
	private ServerMain server;
	private BufferedReader in;
	private PrintWriter out;
	
	// 로그인 정보가 없을 때. 중복 검사(volatile)
	private volatile String userID = null; 
	private volatile String userName = null; 
	private volatile String dept = null; 
	private volatile String status = STATUS_OFFLINE; 

	public ClientHandler(Socket socket, ServerMain server) throws IOException {
		this.socket = socket;
		this.server = server;
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
	}
	
	public String getUserID() {
		return userID;
	}

	public String getUserName() {
		return userName;
	}
	
	public String getDept() {
		return dept;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	/* 로그인 성공시 ServerMain이 호출 - 처음상태 : 대화가능 */
	public void setUser(String userID, String userName, String dept) {
		this.userID = userID;
		this.userName = userName;
		this.dept = dept;
		this.status = STATUS_AVAILABLE;
	}

	/* 여러 스레드가 같은 소켓을 쓸 수 있음 synchronized */
	public synchronized void send(Message msg) {
		out.println(msg.encode());
	}
	
	@Override
	public void run() {		
		try {
			String line;
			
			while ((line = in.readLine()) != null) {
				Message msg = Message.decode(line);
				
				if (msg == null) {
					logger.warning("해석할 수 없는 줄을 무시했습니다.");
					continue;
				}
				
				if (msg.getType().equals(Protocol.LOGOUT)) {
					break;
				}
				
				handle(msg);
				
			}
		} catch (IOException e) {
			// 연결이 끊긴 경우 안내
			logger.info("연결이 끊어졌습니다: "+ socket.getInetAddress());
		} finally {
			server.remove(this);
			try {
				socket.close();
			} catch (IOException e){
				logger.log(Level.WARNING, "소켓 닫기 오류", e);
			}
		}
	}
	
	private void handle(Message msg) {
		// 로그인 전에는 LOGIN 외의 메시지를 처리하지 않음
		if (userID == null) {
			if (msg.getType().equals(Protocol.LOGIN)) {
				handleLogin(msg);
			}
			else {
				logger.warning("로그인 전 메시지를 무시했습니다: " + msg);
			}
		return;
		}
		
		if (msg.getType().equals(Protocol.STATUS)) {
			handleStatus(msg);
		}
		else {
			// TODO : 방과 채팅 메세지는 다음 이슈에서 추가
			logger.info("수신 "+ userID + ": " + msg);			
		}
	}
	
	/* STATUS|상태 -> 모두에게 STATUS_CHANGED|아이디|상태 */
	private void handleStatus(Message msg) {
		if (msg.size() != 1 || !server.getStatusManager().change(this, msg.get(0))) {
			logger.warning("잘못된 활동상태 요청을 무시했습니다: " + userID);
		}
	}
	
	/* LOGIN|아이디|비밀번호 -> LOGIN_OK|아이디|이름|부서 또는 LOGIN_FAIL|사유	 */
	private void handleLogin(Message msg) {
		if (msg.size() < 2) {
			send(new Message(Protocol.LOGIN_FAIL, "잘못된 요청입니다."));
			return;
		}
		
		AccountStore.Account account = server.getAccounts().login(msg.get(0), msg.get(1));
		if (account == null) {
			// 아이디와 비밀번호 중 어느 쪽이 틀렸는지는 알려 주지 않는다.
			send(new Message(Protocol.LOGIN_FAIL, "아이디 또는 비밀번호가 올바르지 않습니다."));
			return;
		}

		if (!server.login(this, account)) {
			send(new Message(Protocol.LOGIN_FAIL, "이미 접속 중인 계정입니다."));
			return;
		}

		logger.info("로그인: " + userID);
	}


}
