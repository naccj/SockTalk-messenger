package socktalk.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import socktalk.common.Protocol;
import socktalk.common.Message;

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
	private StatusManager statusManager = new StatusManager(this);
	
	/* 로그인한 접속자 수 (단순연결 제외) */
	private int countUsers() {
		int count = 0;
		
		for (ClientHandler c : users) {
			if (c.getUserID() != null) {
				count++;
			}
		}
		return count;
	}
	
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
	
	public StatusManager getStatusManager() {
		return statusManager;
	}
	
	/*
	 * 같은 아이디가 접속 중인지 검사, 아니면 해당 핸들러에 아이디를 붙임
	 * 검사와 설정을 synchronized로 한 번에 처리한다.
	 * */
	public synchronized boolean login(ClientHandler handler, AccountStore.Account account) {
		for (ClientHandler c : users) {
			if (c != handler && account.id.equals(c.getUserID())) {
				return false;
			}
		}
		handler.setUser(account.id, account.name, account.dept);
		handler.send(new Message(Protocol.LOGIN_OK,account.id, account.name, account.dept));
		handler.send(makeUserList(account.dept));
		broadcast(new Message(Protocol.USER_JOIN, account.id, account.name, account.dept, handler.getStatus()), handler);
		logger.info("현재 접속자 수: " + countUsers());
		return true;
	}
	
	/* 연결이 끝난 핸들러를 목록에서 제거 */
	public void remove(ClientHandler handler) {
		users.remove(handler);
		broadcast(new Message(Protocol.USER_LEAVE, handler.getUserID()), null);
		logger.info("현재 접속자 수: " + countUsers());
	}
	
	private Message makeUserList(String dept) {
		List<String> fields = new ArrayList<String>();
		int count = 0;
		
		for (ClientHandler c : users) {
			if (c.getUserID() == null) {
				continue;
			}
			fields.add(c.getUserID());
			fields.add(c.getUserName());
			fields.add(c.getDept());
			fields.add(c.getStatus());
			count++;
		}
		
		for (AccountStore.Account a : accounts.getMembers(dept)) {
			if (!isLoggedIn(a.id)) {
				fields.add(a.id);
				fields.add(a.name);
				fields.add(a.dept);
				fields.add(ClientHandler.STATUS_OFFLINE);
				count++;
			}
		}
		
		fields.add(0, String.valueOf(count));
		return new Message(Protocol.USER_LIST, fields.toArray(new String[0]));
	}
	
	private boolean isLoggedIn(String id) {
		for (ClientHandler c : users) {
			if (id.equals(c.getUserID())) {
				return true;
			}
		}
		return false;
	}
	
	
	/* 나 제외 */
	public void broadcast(Message msg, ClientHandler except) {
		for (ClientHandler c : users) {
			if (c != except && c.getUserID() != null) {
				c.send(msg);
			}
		}
	}

	public static void main(String[] args) {
		new ServerMain().start();
	}
	
}
