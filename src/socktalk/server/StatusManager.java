package socktalk.server;

import java.util.logging.Logger;

import socktalk.common.Message;
import socktalk.common.Protocol;


/*
 * 접속자의 활동상태 변경 처리
 * OFFLINE은 ServerMain.remove()로 처리함
 * */
public class StatusManager {
	private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

	private ServerMain server;
	
	public StatusManager(ServerMain server) {
		this.server = server;
	}
	
	/* 활동상태 값이 유효한지 확인 */
	public boolean isSelectable(String status) {
		return status.equals(ClientHandler.STATUS_AVAILABLE) || status.equals(ClientHandler.STATUS_AWAY)
				|| status.equals(ClientHandler.STATUS_BUSY) || status.equals(ClientHandler.STATUS_DND);
	}
	
	public boolean change(ClientHandler handler, String status) {
		if (!isSelectable(status)) {
			return false;
		}
		
		synchronized (server) {
			handler.setStatus(status);
			server.broadcast(new Message(Protocol.STATUS_CHANGED, handler.getUserID(), status), null);
		}
		logger.info("활동상태 변경: " + handler.getUserID() + "->" + status);
		return true;

	}
}
