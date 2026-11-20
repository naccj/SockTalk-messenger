package socktalk.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import socktalk.common.Protocol;

/**
 * 서버 메인 : 접속을 받아 접속자 마다 ClientHandler 스레드를 만듦
 * 관리창은 M3에서 작업하므로 현재는 콘솔로그로 구현
 */
public class ServerMain {
	private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
	
	private static final String ACCOUNT_FILE = "data/Accounts.txt";
	
	// 접속 중인 사용자, Vector로 구현
	private Vector<ClientHandler> users = new Vector<ClientHandler>();
	
	private AccountStore accounts = new AccountStore(ACCOUNT_FILE);
	
	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(Protocol.PORT)) {
			logger.info("서버가 시작되었습니다. 포트: " + Protocol.PORT);
			
			while (true) {
				Socket clientSocket = serverSocket.accept();
				logger.info("클라이언트가 연결되었습니다: " + clientSocket.getInetAddress());
				
				try {
					ClientHandler handler = new ClientHandler(clientSocket, this);
					users.add(handler);
					logger.info("현재 접속자 수: "+ users.size());
					handler.start();
				} catch (Exception e) {
					logger.log(Level.WARNING, "클라이어트 처리 시작 실패", e);
					clientSocket.close();
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "서버 소켓 오류", e);
		}
	}
	
	public AccountStore getAccounts() {
		return accounts;
	}
	
	/*
	 * 같은 아이디가 접속 중인지 검사, 아니면 해당 핸들러에 아이디를 붙임
	 * 검사와 설정을 synchronized로 한 번에 처리한다.
	 * */
	public synchronized boolean login(ClientHandler handler, String id) {
		for (ClientHandler c : users) {
			if (c != handler && id.equals(c.getUserID())) {
				return false;
			}
		}
		handler.setUserID(id);
		return true;
	}
	
	/* 연결이 끝난 핸들러를 목록에서 제거 */
	public void remove(ClientHandler handler) {
		users.remove(handler);
		logger.info("현재 접속자 수: " + users.size());
	}
	
	public static void main(String[] args) {
		new ServerMain().start();
	}
	
}
