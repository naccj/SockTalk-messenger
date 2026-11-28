package socktalk.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import socktalk.common.Message;
import socktalk.common.Protocol;

/**
 * 채팅방을 관리. 
 * 현재는 부서 단체방만 구현함
 * 접속이 끊겨도 방에 남는다(기존(~11.28) : 접속이 끊기면 방에서 퇴장하도록)
 */
public class RoomManager {
	private static final Logger logger = Logger.getLogger(RoomManager.class.getName());

	public static final String TYPE_DEPT = "DEPT";
	
	private static final int MAX_HISTORY = 50; // 방마다 남겨두는 최근 대화 수, 재접속시 보내줌
	
	private static class Room {
		String id;
		String name;
		String type;
		Vector<String> members = new Vector<String>();
		Vector<String[]> history = new Vector<String[]>(); // {송신자ID, 시간, 내용}
		
		Room(String id, String name, String type) {
			this.id = id;
			this.name = name;
			this.type = type;
		}
	}
	
	private ServerMain server;
	private Vector<Room> rooms = new Vector<Room>();
	
	public RoomManager(ServerMain server) {
		this.server = server;
	}
	
	/*
	 * 로그인한 사람을 부서 방에 넣고 ROOM_LIST, CHAT(최근 대화)를 보냄
	 */
	public void joinDeptRoom(ClientHandler handler) {
		synchronized (server) {
			Room room = findRoom(deptRoomID(handler.getDept()));
			
			if (room == null) {
				room = new Room(deptRoomID(handler.getDept()), handler.getDept() + "단체방", TYPE_DEPT);
				rooms.add(room);
				logger.info("부서 단체방 생성: " + room.id);
			}
			
			if (!room.members.contains(handler.getUserID())) {
				room.members.add(handler.getUserID());
			}
			handler.send(makeRoomList(handler.getUserID()));
			sendHistory(handler);
		}
	}
	
	/*
	 * CHAT|방ID|내용 -> 방 멤버 전체에게 CHAT|방ID|송신자ID|시간|내용
	 * 오프라인인인 멤버는 건너뛰고 다음에 접속시 기록으로 받음
	 * 내용이 비었거나 방이 없거나 그 방의 멤버가 아닌 경우 false 반환 
	 */
	public boolean chat(ClientHandler handler, String roomID, String text) {
		if (text.trim().isEmpty()) {
			return false;
		}
		synchronized (server) {
			Room room = findRoom(roomID);
			
			if(room == null || !room.members.contains(handler.getUserID())) {
				return false;
			}
			
			String time = new SimpleDateFormat("HH:mm").format(new Date());
			
			room.history.add(new String[] { handler.getUserID(), time, text });
			if (room.history.size() > MAX_HISTORY) {
				room.history.remove(0);
			}
			
			Message msg = new Message(Protocol.CHAT, room.id, handler.getUserID(), time, text);
			
			for (String id : room.members) {
				ClientHandler c = server.findUser(id);
				
				if (c != null) {
					c.send(msg);					
				}
			}
		}
		return true;
	}
	
	/* 멤버인 방의 지난 대화를 CHAT 형식 그대로 오래된 것부터 보냄 */
	private void sendHistory(ClientHandler handler) {
		for (Room r : rooms) {
			if (!r.members.contains(handler.getUserID())) {
				continue;
			}
			for (String[] h : r.history) {
				handler.send(new Message(Protocol.CHAT, r.id, h[0], h[1], h[2]));
			}
		}
		
	}
	
	/* ROOM_LIST|n|(방ID|이름|종류)×n. 이 사람이 멤버인 방만 담는다. */
	private Message makeRoomList(String userID) {
		List<String> fields =  new ArrayList<String>();
		int count = 0;
		
		for (Room r : rooms) {
			if (r.members.contains(userID)) {
				fields.add(r.id);
				fields.add(r.name);
				fields.add(r.type);
				count++;
			}
		}
		
		fields.add(0, String.valueOf(count));
		return new Message(Protocol.ROOM_LIST, fields.toArray(new String[0]));
	}
	
	private Room findRoom(String roomID) {
		for (Room r : rooms) {
			if (r.id.equals(roomID)) {
				return r;
			}
		}
		return null;
	}
	
	private String deptRoomID(String dept) {
		return "DEPT_" + dept;
	}
}

