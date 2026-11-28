package socktalk.server;

import java.util.logging.Logger;

import socktalk.common.Message;
import socktalk.common.Protocol;

/**
 * 로비 공지를 관리, 공지는 서버에 하나만 있고 서버를 끄면 사라짐
 * 공지가 있는 동안 새로 로그인한 사람에게도 현재 공지를 보냄
 */

public class NoticeManager {
	private static final Logger logger = Logger.getLogger(NoticeManager.class.getName());
	private ServerMain server;
	
	private String content = "";
	private String writer = "";
	
	public NoticeManager(ServerMain server) {
		this.server = server;
	}

	/* 
	 * LOBBY_NOTICE_SET|내용 -> 모두에게 LOBBY_NOTICE|게시자ID|내용 
	 * 내용이 비었거나 공백일 경우 공지 삭제
	 * */
	public void setLobbyNotice(ClientHandler handler, String text) {
		boolean isDelete = text.trim().isEmpty();
		
		synchronized (server) {
			if (isDelete) {
				content = "";
				writer = "";
			}
			else {
				content = text;
				writer = handler.getUserID();
			}
			server.broadcast(new Message(Protocol.LOBBY_NOTICE, handler.getUserID(), content), null);
		}
		logger.info("로비 공지 " + (isDelete ? "삭제" : "등록") + ": " + handler.getUserID());
	}

	/* 로그인한 사람에게 현재 공지를 보냄, 공지가 없으면 보내지 않음 login()이 호출 */
	public void sendLoobyNotice(ClientHandler handler) {
		synchronized (server) {
			if (content.isEmpty()) {
				return;
			}
			handler.send(new Message(Protocol.LOBBY_NOTICE, writer, content));
		}
		
	}

}
