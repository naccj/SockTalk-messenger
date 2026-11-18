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

	public ClientHandler(Socket socket, ServerMain server) throws IOException {
		this.socket = socket;
		this.server = server;
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
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
		// TODO : 로그인과 다음 메시지 처리를 여기에 추가
		// Message.toString은 필드 내용을 출력하지 않으므로 비밀번호가 로그에 남지 않음
		logger.info("수신 "+socket.getInetAddress() + ": " + msg);
	}

}
