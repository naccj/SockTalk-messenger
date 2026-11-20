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


/**
 *  접속자 한명을 담당, 한 줄씩 받아 message로 해석 및 처리
 */
public class ClientHandler extends Thread {
	private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

	private Socket socket;
	private ServerMain server;
	private BufferedReader in;
	private PrintWriter out;
	
	private volatile String userID = null; // 로그인 정보가 없을 때. 중복 검사(volatile)

	public ClientHandler(Socket socket, ServerMain server) throws IOException {
		this.socket = socket;
		this.server = server;
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
	}
	
	public String getUserID() {
		return userID;
	}
	
	public void setUserID(String userID) {
		this.userID = userID;
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
		
		// TODO : 로그인 이후 메시지는 M2부터 추가
		logger.info("수신 "+ userID + ": " + msg);
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

		if (!server.login(this, account.id)) {
			send(new Message(Protocol.LOGIN_FAIL, "이미 접속 중인 계정입니다."));
			return;
		}

		send(new Message(Protocol.LOGIN_OK, account.id, account.name, account.dept));
		logger.info("로그인: " + userID);
	}
}
