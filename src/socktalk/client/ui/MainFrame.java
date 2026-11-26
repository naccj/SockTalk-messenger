package socktalk.client.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import socktalk.client.Theme;
import socktalk.client.net.ConnectionManager;
import socktalk.client.net.MessageListener;
import socktalk.common.Message;
import socktalk.common.Protocol;

public class MainFrame extends JFrame{
	private ConnectionManager cm;
	private String myId;
	private String myName;
	private String myDepartment;
	
	// <유저 아이디, 유저 정보>
	private Map<String, UserItem> userMap = new LinkedHashMap<>();
	// <방 ID, 방 정보>
	private Map<String, RoomItem> roomMap = new LinkedHashMap<>();
	// <방 ID, 현재 열려 있는 채팅방>
	private Map<String, ChatFrame> openChatFrames = new LinkedHashMap<>();
	// <방 ID, 채팅방 열기 전 도착한 메시지를 포함한 누적 내역>
	private Map<String, List<ChatRecord>> chatHistoryMap = new LinkedHashMap<>();
	
	private StatusMenu statusMenu;
	private CardLayout cardLayout;
	private JPanel contentPanel;
	private JPanel profilePanel;
	private JLabel titleLabel;
	
	private JButton userTabBtn;
	private JButton chatTabBtn;
	
	private DefaultListModel<UserItem> userListModel;
	private DefaultListModel<RoomItem> chatRoomListModel;
	
	private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());
	
	public MainFrame(ConnectionManager cm, String userId, String userName, String department) {
		this.cm = cm;
		this.myId = userId;
		this.myName = userName;
		this.myDepartment = department;
		
		// 프레임 -> UI 배치 -> 서버 메시지 수신 연결
		initFrame();
		initComponents();
		setupMessageListener();
	}
	
	// 프레임
	private void initFrame() {
		setTitle("SockTalk");
		setSize(400, 680);
		setMinimumSize(new Dimension(360, 560));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
	}
	
	// UI 배치
	private void initComponents() {
		JPanel sideBar = new JPanel(new BorderLayout());
		sideBar.setPreferredSize(new Dimension(64, 0));
		sideBar.setBackground(Theme.BG);
		sideBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.SUB));
		
		JPanel topMenuPanel = new JPanel(new GridLayout(2, 1, 0, 12));
		topMenuPanel.setOpaque(false); // 패널 배경 투명하게
		topMenuPanel.setBorder(BorderFactory.createEmptyBorder(20, 8, 0, 8)); // 안쪽 여백
		
		userTabBtn = createSideBarButton("USER", true);
		chatTabBtn = createSideBarButton("CHAT", false);
		
		userTabBtn.addActionListener(e -> switchTab("USER", "직원"));
		chatTabBtn.addActionListener(e -> switchTab("CHAT", "채팅"));
		
		topMenuPanel.add(userTabBtn);
		topMenuPanel.add(chatTabBtn);
		sideBar.add(topMenuPanel, BorderLayout.NORTH);
		
		JPanel bottomMenuPanel = new JPanel(new GridLayout(1, 1));
		bottomMenuPanel.setOpaque(false);
		bottomMenuPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 16, 4));
		
		JButton logoutBtn = createLogoutButton();
		logoutBtn.addActionListener(e -> handleLogout());
		
		bottomMenuPanel.add(logoutBtn);
		sideBar.add(bottomMenuPanel, BorderLayout.SOUTH);
		
		add(sideBar, BorderLayout.WEST);
		
		JPanel mainContainer = new JPanel(new BorderLayout());
		mainContainer.setBackground(Theme.BG);
		
		JPanel topSection = new JPanel(new BorderLayout());
		topSection.setBackground(Theme.BG);
		
		titleLabel = new JLabel("직원");
		titleLabel.setFont(new Font(Theme.FONT, Font.BOLD, 18));
		titleLabel.setForeground(Theme.TEXT);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));
		topSection.add(titleLabel, BorderLayout.NORTH);
		
		profilePanel = new JPanel(new BorderLayout(12, 0));
		profilePanel.setBackground(Theme.SUB);
		profilePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.SUB),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));
		
		AvatarPanel myAvatar = new AvatarPanel(myName.substring(0, 1), Theme.PRIMARY);
		
		JPanel myInfoBox = new JPanel(new GridLayout(2, 1, 0, 2));
		myInfoBox.setOpaque(false);
		JLabel nameLabel = new JLabel(myName);
		nameLabel.setFont(new Font(Theme.FONT, Font.BOLD, 14));
		nameLabel.setForeground(Theme.TEXT_ON_COLOR);
		
		JLabel deptLabel = new JLabel(myDepartment);
		deptLabel.setFont(new Font(Theme.FONT, Font.PLAIN, 12));
		deptLabel.setForeground(Theme.TEXT);
		
		myInfoBox.add(nameLabel);
		myInfoBox.add(deptLabel);
		
		statusMenu = new StatusMenu(cm);
		
		profilePanel.add(myAvatar, BorderLayout.WEST);
		profilePanel.add(myInfoBox, BorderLayout.CENTER);
		profilePanel.add(statusMenu, BorderLayout.EAST);
		
		topSection.add(profilePanel, BorderLayout.CENTER);
		mainContainer.add(topSection, BorderLayout.NORTH);
		
		cardLayout = new CardLayout();
		contentPanel = new JPanel(cardLayout);
		contentPanel.setBackground(Theme.BG);
		
		userListModel = new DefaultListModel<>();
		JList<UserItem> userList = new JList<>(userListModel);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userList.setCellRenderer(new UserListRenderer());
		userList.setFixedCellHeight(56);
		preventEmptySelection(userList);
		
		JScrollPane userScroll = new JScrollPane(userList);
		userScroll.setBorder(null);
		userScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		contentPanel.add(userScroll, "USER");
		
		chatRoomListModel = new DefaultListModel<>();
		JList<RoomItem> chatRoomList = new JList<>(chatRoomListModel) {
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
		};
		chatRoomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chatRoomList.setCellRenderer(new RoomListRenderer());
		chatRoomList.setFixedCellHeight(64);
		preventEmptySelection(chatRoomList);
		chatRoomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = chatRoomList.locationToIndex(e.getPoint());
					if (index != -1 && chatRoomList.getCellBounds(index, index).contains(e.getPoint())) {
						RoomItem selected = chatRoomListModel.getElementAt(index);
						openChatRoom(selected);
					}
				}
			}
		});
		
		JScrollPane chatScroll = new JScrollPane(chatRoomList);
		chatScroll.setBorder(null);
		chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		contentPanel.add(chatScroll, "CHAT");
		
		mainContainer.add(contentPanel, BorderLayout.CENTER);
		add(mainContainer, BorderLayout.CENTER);
	}
	
	// 빈 공간 클릭 시 선택 해제
	// 리스트 타입 상관 없이 다 받을 수 있도록 와일드카드(?) 사용
	private void preventEmptySelection(JList<?> list) {
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = list.locationToIndex(e.getPoint());
				// 그 항목이 실제로 차지하는 사각형 영역 안에 마우스 클릭 좌표가 들어있는지 검사
				if (index == -1 || !list.getCellBounds(index, index).contains(e.getPoint())) {
					list.clearSelection(); // 항목이 없거나 실제로 칸 바깥을 눌렀다면 선택 상태 해제
				}
			}
		});
	}
	
	
	private ImageIcon loadScaledIcon(String path, int width, int height) {
		URL imgUrl = getClass().getResource(path);
		if (imgUrl == null) {
			LOGGER.warning("아이콘 리소스를 찾을 수 없음: " + path);
			return null;
		}
		ImageIcon origin = new ImageIcon(imgUrl);
		Image scaled = origin.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}
	
	// 사이드바 아이콘 버튼 생성
	private JButton createSideBarButton(String type, boolean active) {
		String iconPath = "USER".equals(type) ? "/images/user.png" : "/images/chat.png";
		JButton btn = new JButton(loadScaledIcon(iconPath, 24, 24));
				
		btn.setToolTipText("USER".equals(type) ? "직원" : "채팅");
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(true);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setPreferredSize(new Dimension(48, 46));
		styleTabButton(btn, active);
		return btn;
	}
	
	private JButton createLogoutButton() {
		JButton btn = new JButton(loadScaledIcon("/images/logout.png", 22, 22));
		btn.setToolTipText("로그아웃");
		btn.setBackground(Theme.SUB);
		btn.setFocusPainted(false); // 버튼 눌렀을 때 주변에 생기는 점선 테두리 제거
		btn.setBorderPainted(false);
		btn.setPreferredSize(new Dimension(56, 30));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 마우스 올리면 손가락 모양 커서로 변경
		return btn;
	}
	
	private void handleLogout() {
		int result = JOptionPane.showConfirmDialog(
					this,
					"로그아웃 하시겠습니까?",
					"로그아웃",
					JOptionPane.YES_NO_OPTION
		);
		if (result != JOptionPane.YES_OPTION) {
			return;
		}
		if (cm != null) { // 서버 연결 객체가 있을 때만 로그아웃 가능
			// 리스너를 먼저 해제하지 않고 바로 연결을 끊으면 onDisconnected 반응
			cm.setMessageListener(null);
			cm.send(new Message(Protocol.LOGOUT));
			cm.disconnect();
		}
		
		new LoginFrame(cm).setVisible(true);
		closeAllChatFrames();
		dispose();
	}
	
	// 선택되었는지에 따라 색 변경
	private void styleTabButton(JButton btn, boolean active) {
		if(active) {
			btn.setBackground(Theme.PRIMARY);
			btn.setForeground(Theme.TEXT_ON_COLOR);
		}else {
			btn.setBackground(Theme.BG);
			btn.setForeground(Theme.TEXT);
		}
	}
	
	// 화면 전환
	private void switchTab(String cardName, String title) {
		cardLayout.show(contentPanel, cardName);
		titleLabel.setText(title);
		profilePanel.setVisible("USER".equals(cardName));
		styleTabButton(userTabBtn, "USER".equals(cardName));
		styleTabButton(chatTabBtn, "CHAT".equals(cardName));
	}
	
	// 서버 메시지 수신 등록
	private void setupMessageListener() {
		if (cm == null) {
			return;
		}
		
		cm.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				String type = msg.getType();
				SwingUtilities.invokeLater(() -> {
					switch(type) {
						case Protocol.USER_LIST -> handleUserList(msg);
						case Protocol.USER_JOIN -> handleUserJoin(msg);
						case Protocol.USER_LEAVE -> handleUserLeave(msg);
						case Protocol.STATUS_CHANGED -> handleStatusChanged(msg);
						case Protocol.ROOM_LIST -> handleRoomList(msg);
						case Protocol.CHAT -> handleChat(msg);
						default -> LOGGER.info("미처리 메시지: " + type);
					}
				});
			}

			@Override
			public void onDisconnected() {
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(
							MainFrame.this, 
							"서버와의 연결이 끊어졌습니다.", 
							"연결 끊김", 
							JOptionPane.WARNING_MESSAGE
					);
					closeAllChatFrames();
					dispose();
				});
			}
			
		});
	}
	
	// 전체 접속자 목록 수신
	// USER_LIST|n|(아이디|이름|부서|상태)xn
	private void handleUserList(Message msg) {
		if (msg.size() < 1) {
			return;
		}
		try {
			int n = Integer.parseInt(msg.get(0)); // 총 직원 수
			userMap.clear(); // 전체 목록을 새로 받았으니 기존 맵에 있는 데이터 정리
			for (int i = 0; i < n; i++) {
				int base = 1 + i * 4;
				if (base + 3 >= msg.size()) {
					break;
				}
				String id = msg.get(base);
				String name = msg.get(base + 1);
				String dept = msg.get(base + 2);
				String status = msg.get(base + 3);
				// 내 정보는 따로 표시하고 있으니 다른 직원들만 맵에 저장
				if(!id.equals(myId)) {
					userMap.put(id, new UserItem(id, name, dept, status));
				}
			}
			refreshUserList(); // 화면 리스트 갱신
		}catch (NumberFormatException e) {
			LOGGER.warning("USER_LIST 파싱 오류: " + e.getMessage());
		}
	}
	
	// 새 직원 접속 처리
	// USER_JOIN|아이디|이름|부서|상태
	private void handleUserJoin(Message msg) {
		if (msg.size() < 4) {
			return;
		}
		String id = msg.get(0);
		if(id.equals(myId)) {
			return;
		}
		userMap.put(id, new UserItem(id, msg.get(1), msg.get(2), msg.get(3)));
		refreshUserList();
	}
	
	// 직원 퇴장 처리
	private void handleUserLeave(Message msg) {
		if (msg.size() < 1) {
			LOGGER.warning("USER_LEAVE 필드 부족: " + msg.size());
			return;
		}
		
		String id = msg.get(0);
		UserItem user = userMap.get(id);
		if(user != null) {
			user.status = "OFFLINE";
			refreshUserList();
		}
	}
	
	// 직원 활동 상태 변경 처리
	// STATUS_CHANGED|아이디|상태
	private void handleStatusChanged(Message msg) {
		if(msg.size() < 2) {
			return;
		}
		String id = msg.get(0);
		String status = msg.get(1);
		UserItem user = userMap.get(id);
		if(user != null) {
			user.status = status;
			refreshUserList();
		}
	}
	
	// 화면 갱신
	private void refreshUserList() {
		userListModel.clear();
		// 맵은 자체 정렬이 되지 않으므로 UserItem만 뽑아서 ArrayList에 담기
		List<UserItem> sorted = new ArrayList<>(userMap.values());
		sorted.sort(Comparator
				.comparing((UserItem u) -> !u.dept.equals(myDepartment)) // 오름차순 시 false가 true 앞
				.thenComparing(u -> u.dept) // 다른 부서들끼리 부서명 가나다순으로 정렬
				.thenComparing(u -> u.name)); // 같은 부서 안에서는 직원 이름 가나다순으로 정렬
		
		String currentDept = null;
		for(UserItem u : sorted) {
			if(!u.dept.equals(currentDept)) {
				currentDept = u.dept;
				userListModel.addElement(UserItem.header(currentDept));
			}
			userListModel.addElement(u);
		}
	}
	
	// 채팅방 제어
	private void openChatRoom(RoomItem room) {
		ChatFrame existing = openChatFrames.get(room.id);
		if(existing != null) {
			existing.toFront();
			existing.requestFocus();
			return;
		}
		
		ChatFrame chatFrame = new ChatFrame(cm, room.id, room.title, room.type, myId);
		chatFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				openChatFrames.remove(room.id);
			}
		});
		
		List<ChatRecord> history = chatHistoryMap.get(room.id);
		if (history != null) {
			for(ChatRecord r : history) {
				chatFrame.appendMessage(r.senderId, r.senderName, r.time, r.content);
			}
		}
		
		openChatFrames.put(room.id, chatFrame);
		chatFrame.setVisible(true);
	}
	
	private void closeAllChatFrames() {
		for(ChatFrame frame : new ArrayList<>(openChatFrames.values())) {
			frame.dispose();
		}
		openChatFrames.clear();
	}
	
	// 참여 중인 방 목록 수신
	// ROOM_LIST|n|(방ID|이름|종류)*n
	private void handleRoomList(Message msg) {
		if (msg.size() < 1) {
			return;
		}
		
		try {
			int n = Integer.parseInt(msg.get(0));
			roomMap.clear();
			for (int i = 0; i < n; i++) {
				int base = 1 + i * 3;
				if  (base + 2 >= msg.size()) {
					break;
				}
				String roomId = msg.get(base);
				String roomName = msg.get(base + 1);
				String roomType = msg.get(base + 2);
				roomMap.put(roomId, new RoomItem(roomId, roomType, roomName, "", ""));
			}
			refreshRoomList();
		}catch (NumberFormatException e) {
			LOGGER.warning("ROOM_LIST 파싱 오류: " + e.getMessage());
		}
	}
	
	// 채팅 수신
	// CHAT|방ID|송신자ID|시간|내용
	private void handleChat(Message msg) {
		if(msg.size() < 4) {
			LOGGER.warning("CHAT 필드 부족: " + msg.size());
			return;
		}
		
		String roomId = msg.get(0);
		String senderId = msg.get(1);
		String time = msg.get(2);
		String content = msg.get(3);
		
		String senderName;
		if(senderId.equals(myId)) {
			senderName = myName;
		} else {
			UserItem user = userMap.get(senderId);
			senderName = (user != null) ? user.name : senderId;
		}
		
		chatHistoryMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(new ChatRecord(senderId, senderName, time, content));
		
		RoomItem room = roomMap.get(roomId);
		if (room != null) {
			room.lastMsg = content;
			room.time = time;
			refreshRoomList();
		}
		
		ChatFrame frame = openChatFrames.get(roomId);
		if(frame != null) {
			frame.appendMessage(senderId, senderName, time, content);
		}
	}
	
	private void refreshRoomList() {
		chatRoomListModel.clear();
		for(RoomItem room : roomMap.values()) {
			chatRoomListModel.addElement(room);
		}
	}
	
	// 데이터 모음
	private static class UserItem{
		String id, name, dept, status;
		boolean isHeader;
		
		UserItem(String id, String name, String dept, String status){
			this.id = id;
			this.name = name;
			this.dept = dept;
			this.status = status;
			this.isHeader = false;
		}
		
		// 부서 구분
		static UserItem header(String dept) {
			UserItem item = new UserItem("", "", dept, "");
			item.isHeader = true;
			return item;
		}
	}

	// 데이터 모음
	private static class RoomItem{
		// 방 종류, 방 제목, 마지막 메시지, 시간
		String id, type, title, lastMsg, time;
		RoomItem(String id, String type, String title, String lastMsg, String time){
			this.id = id;
			this.type = type;
			this.title = title;
			this.lastMsg = lastMsg;
			this.time = time;
		}
	}
	
	private static class ChatRecord {
		// 보낸 사람 ID, 보낸 사람 이름, 시간, 내용
		String senderId, senderName, time, content;
		
		ChatRecord(String senderId, String senderName, String time, String content){
			this.senderId = senderId;
			this.senderName = senderName;
			this.time = time;
			this.content = content;
		}
	}
	
	private static class AvatarPanel extends JPanel{
		private final String initial;
		private final Color bgColor;
		
		AvatarPanel(String initial, Color bgColor){
			this.initial = initial;
			this.bgColor = bgColor;
			setPreferredSize(new Dimension(38, 38));
			setOpaque(false);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(bgColor);
			g2.fillRoundRect(0, 2, 34, 34, 14, 14);
			g2.setColor(Theme.TEXT_ON_COLOR);
			g2.setFont(new Font(Theme.FONT, Font.BOLD, 14));
			// 현재 폰트로 글자를 그렸을 때 가로 폭이 몇 px인지 미리 측정
			int strW = g2.getFontMetrics().stringWidth(initial);
			g2.drawString(initial, (34 - strW) / 2, 24);
			g2.dispose();
		}
	}
	
	// 직원 목록(한 줄) 그리기
	private static class UserListRenderer extends DefaultListCellRenderer{
		@Override
		public Component getListCellRendererComponent(
				JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			UserItem item = (UserItem) value;
			
			if(item.isHeader) {
				JPanel header = new JPanel(new BorderLayout());
				header.setBackground(Theme.BG);
				header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.SUB));
				JLabel lbl = new JLabel(item.dept);
				lbl.setFont(new Font(Theme.FONT, Font.BOLD, 11));
				lbl.setForeground(Theme.TEXT);
				lbl.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
				header.add(lbl, BorderLayout.WEST);
				return header;
			}
			// 직원 한 줄을 담는 패널
			JPanel row = new JPanel(new BorderLayout(12, 0));
			row.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
			row.setBackground(isSelected ? Theme.SUB : Color.WHITE);
			
			AvatarPanel avatar = new AvatarPanel(item.name.substring(0, 1), Theme.PRIMARY);
			
			JPanel textBox = new JPanel(new GridLayout(2, 1));
			textBox.setOpaque(false);
			
			JLabel nameLbl = new JLabel(item.name);
			nameLbl.setFont(new Font(Theme.FONT, Font.BOLD, 13));
			nameLbl.setForeground(Theme.TEXT);
			
			JLabel deptLbl = new JLabel(item.dept);
			deptLbl.setFont(new Font(Theme.FONT, Font.PLAIN, 11));
			deptLbl.setForeground(Theme.TEXT);
			
			textBox.add(nameLbl);
			textBox.add(deptLbl);
			
			String statusText;
			Color statusColor;
			switch (item.status) {
				case "AVAILABLE" -> { 
					statusText = "대화가능"; 
					statusColor = Theme.PRIMARY; 
				}
				case "AWAY" -> { 
					statusText = "자리비움"; 
					statusColor = Theme.ACCENT; 
				}
				case "BUSY" -> { 
					statusText = "다른용무중"; 
					statusColor = Theme.ACCENT; 
				}
				case "DND" -> { 
					statusText = "방해금지"; 
					statusColor = Theme.TEXT; 
				}
				default -> {
					statusText = "오프라인"; 
					statusColor = Theme.TEXT; 
				}
			}
			
			JLabel statusLbl = new JLabel(statusText);
			statusLbl.setFont(new Font(Theme.FONT, Font.PLAIN, 11));
			statusLbl.setForeground(statusColor);
			
			row.add(avatar, BorderLayout.WEST);
			row.add(textBox, BorderLayout.CENTER);
			row.add(statusLbl, BorderLayout.EAST);
			return row;
		}
	}
	
	// 채팅 목록(한 줄) 그리기
	private static class RoomListRenderer extends DefaultListCellRenderer{
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			RoomItem item = (RoomItem) value;
			JPanel row = new JPanel(new BorderLayout(12, 0));
			row.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
			row.setBackground(isSelected ? Theme.SUB : Color.WHITE);
			
			Color badgeColor = "DEPT".equals(item.type) ? Theme.ACCENT 
					: "GROUP".equals(item.type) ? Theme.PRIMARY
					: Theme.SUB;
			String initial = (item.title != null && !item.title.isEmpty()) ? item.title.substring(0, 1) : "?";
			AvatarPanel avatar = new AvatarPanel(initial, badgeColor);
			
			JPanel centerBox = new JPanel(new GridLayout(2, 1, 0, 2));
			centerBox.setOpaque(false);
			
			JLabel titleLbl = new JLabel(item.title);
			titleLbl.setFont(new Font(Theme.FONT, Font.BOLD, 13));
			titleLbl.setForeground(Theme.TEXT);
			
			JLabel msgLbl = new JLabel(item.lastMsg);
			msgLbl.setFont(new Font(Theme.FONT, Font.PLAIN, 12));
			msgLbl.setForeground(Theme.TEXT);
			
			centerBox.add(titleLbl);
			centerBox.add(msgLbl);
			
			JLabel timeLbl = new JLabel(item.time, SwingConstants.RIGHT);
			timeLbl.setFont(new Font(Theme.FONT, Font.PLAIN, 10));
			timeLbl.setForeground(Theme.TEXT);
			timeLbl.setVerticalAlignment(SwingConstants.TOP);
			
			row.add(avatar, BorderLayout.WEST);
			row.add(centerBox, BorderLayout.CENTER);
			row.add(timeLbl, BorderLayout.EAST);
			return row;
		}
	}
}
