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
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
	
	private Map<String, UserItem> userMap = new LinkedHashMap<>();
	
	private StatusMenu statusMenu;
	private CardLayout cardLayout;
	private JPanel contentCardPanel;
	private JPanel myProfilePanel;
	private JLabel headerTitleLabel;
	
	private JButton tabUserBtn;
	private JButton tabChatBtn;
	
	private DefaultListModel<UserItem> userListModel;
	private DefaultListModel<RoomItem> chatRoomListModel;
	
	public MainFrame(ConnectionManager cm, String userId, String userName, String department) {
		this.cm = cm;
		this.myId = userId;
		this.myName = userName;
		this.myDepartment = department;
		
		initFrame();
		initComponents();
		setupMessageListener();
	}
	
	private void initFrame() {
		setTitle("SockTalk");
		setSize(400, 680);
		setMinimumSize(new Dimension(360, 560));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
	}
	
	private void initComponents() {
		JPanel sideBar = new JPanel(new BorderLayout());
		sideBar.setPreferredSize(new Dimension(64, 0));
		sideBar.setBackground(Theme.BG);
		sideBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.SUB));
		
		JPanel topMenuPanel = new JPanel(new GridLayout(2, 1, 0, 12));
		topMenuPanel.setOpaque(false);
		topMenuPanel.setBorder(BorderFactory.createEmptyBorder(20, 8, 0, 8));
		
		tabUserBtn = createSideTabButton("USER", true);
		tabChatBtn = createSideTabButton("CHAT", false);
		
		tabUserBtn.addActionListener(e -> switchTab("USER", "직원"));
		tabChatBtn.addActionListener(e -> switchTab("CHAT", "채팅"));
		
		topMenuPanel.add(tabUserBtn);
		topMenuPanel.add(tabChatBtn);
		sideBar.add(topMenuPanel, BorderLayout.NORTH);
		
		add(sideBar, BorderLayout.WEST);
		
		JPanel mainContainer = new JPanel(new BorderLayout());
		mainContainer.setBackground(Theme.BG);
		
		JPanel topSection = new JPanel(new BorderLayout());
		topSection.setBackground(Theme.BG);
		
		headerTitleLabel = new JLabel("직원");
		headerTitleLabel.setFont(new Font(Theme.FONT, Font.BOLD, 18));
		headerTitleLabel.setForeground(Theme.TEXT);
		headerTitleLabel.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));
		topSection.add(headerTitleLabel, BorderLayout.NORTH);
		
		myProfilePanel = new JPanel(new BorderLayout(12, 0));
		myProfilePanel.setBackground(Theme.SUB);
		myProfilePanel.setBorder(BorderFactory.createCompoundBorder(
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
		
		myProfilePanel.add(myAvatar, BorderLayout.WEST);
		myProfilePanel.add(myInfoBox, BorderLayout.CENTER);
		myProfilePanel.add(statusMenu, BorderLayout.EAST);
		
		topSection.add(myProfilePanel, BorderLayout.CENTER);
		mainContainer.add(topSection, BorderLayout.NORTH);
		
		cardLayout = new CardLayout();
		contentCardPanel = new JPanel(cardLayout);
		contentCardPanel.setBackground(Theme.BG);
		
		userListModel = new DefaultListModel<>();
		JList<UserItem> userList = new JList<>(userListModel);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userList.setCellRenderer(new UserListRenderer());
		userList.setFixedCellHeight(56);
		preventEmptySelection(userList);
		
		JScrollPane userScroll = new JScrollPane(userList);
		userScroll.setBorder(null);
		userScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		contentCardPanel.add(userScroll, "USER");
		
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
		
		JScrollPane chatScroll = new JScrollPane(chatRoomList);
		chatScroll.setBorder(null);
		chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		contentCardPanel.add(chatScroll, "CHAT");
		
		mainContainer.add(contentCardPanel, BorderLayout.CENTER);
		add(mainContainer, BorderLayout.CENTER);
	}
	
	private void preventEmptySelection(JList<?> list) {
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = list.locationToIndex(e.getPoint());
				if (index == -1 || !list.getCellBounds(index, index).contains(e.getPoint())) {
					list.clearSelection();
				}
			}
		});
	}
	
	private JButton createSideTabButton(String type, boolean active) {
		JButton btn = new JButton() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getForeground());
				
				int cx = getWidth() / 2;
				int cy = getHeight() / 2;
				
				if("USER".equals(type)) {
					g2.fillOval(cx - 5, cy - 10, 10, 10);
					g2.fillArc(cx - 10, cy + 1, 20, 16, 0, 180);
				}else {
					g2.fillRoundRect(cx - 10, cy - 9, 20, 15, 7, 7);
					int[] xPoints = {cx - 5, cx - 1, cx - 7};
					int[] yPoints = {cy + 5, cy + 5, cy + 10};
					g2.fillPolygon(xPoints, yPoints, 3);
				}
				g2.dispose();
			}
		};
		
		btn.setToolTipText("USER".equals(type) ? "직원" : "채팅");
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setPreferredSize(new Dimension(48, 46));
		styleTabButton(btn, active);
		return btn;
	}
	
	private void styleTabButton(JButton btn, boolean active) {
		if(active) {
			btn.setBackground(Theme.PRIMARY);
			btn.setForeground(Color.WHITE);
		}else {
			btn.setBackground(Theme.BG);
			btn.setForeground(Theme.TEXT);
		}
	}
	
	private void switchTab(String cardName, String title) {
		cardLayout.show(contentCardPanel, cardName);
		headerTitleLabel.setText(title);
		myProfilePanel.setVisible("USER".equals(cardName));
		styleTabButton(tabUserBtn, "USER".equals(cardName));
		styleTabButton(tabChatBtn, "CHAT".equals(cardName));
	}
	
	private void loadDommyRooms() {
		chatRoomListModel.addElement(new RoomItem("DEPT", myDepartment + "단체방", "공지", "13:42"));
	}
	
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
						case Protocol.PRESENCE -> handlePresence(msg);
						default -> System.out.println("[MainFrame] 미처리 메시지: " + type);
					}
				});
			}

			@Override
			public void onDisconnected() {
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(MainFrame.this, "서버와의 연결이 끊어졌습니다.", "연결 끊김", JOptionPane.WARNING_MESSAGE);
					
				});
			}
			
		});
	}
	
	// USER_LIST|n|(아이디|이름|부서|상태)xn
	private void handleUserList(Message msg) {
		if (msg.size() < 1) {
			return;
		}
		try {
			int n = Integer.parseInt(msg.get(0));
			userMap.clear();
			for (int i = 0; i < n; i++) {
				int base = 1 + i * 4;
				if (base + 3 >= msg.size()) {
					break;
				}
				String id = msg.get(base);
				String name = msg.get(base + 1);
				String dept = msg.get(base + 2);
				String status = msg.get(base + 3);
				if(!id.equals(myId)) {
					userMap.put(id, new UserItem(id, name, dept, status));
				}
			}
			refreshUserList();
		}catch (NumberFormatException e) {
			System.err.println("USER_LIST 파싱 오류: " + e.getMessage());
		}
	}
	
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
	
	private void handleUserLeave(Message msg) {
		if (msg.size() < 1) {
			System.err.println("[MainFrame] USER_LEAVE 필드 부족: " + msg.size());
			return;
		}
		
		String id = msg.get(0);
		UserItem user = userMap.get(id);
		if(user != null) {
			user.status = "OFFLINE";
			refreshUserList();
		}
	}
	
	// PRESENCE|아이디|상태
	private void handlePresence(Message msg) {
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
	
	private void refreshUserList() {
		userListModel.clear();
		List<UserItem> sorted = new ArrayList<>(userMap.values());
		sorted.sort(Comparator
				.comparing((UserItem u) -> !u.dept.equals(myDepartment))
				.thenComparing(u -> u.dept)
				.thenComparing(u -> u.name));
		
		String currentDept = null;
		for(UserItem u : sorted) {
			if(!u.dept.equals(currentDept)) {
				currentDept = u.dept;
				userListModel.addElement(UserItem.header(currentDept));
			}
			userListModel.addElement(u);
		}
	}
	
	private static class UserItem{
		String name, dept, status;
		boolean isHeader;
		
		UserItem(String id, String name, String dept, String status){
			this.name = name;
			this.dept = dept;
			this.status = status;
			this.isHeader = false;
		}
		
		static UserItem header(String dept) {
			UserItem item = new UserItem("", "", dept, "");
			item.isHeader = true;
			return item;
		}
	}

	private static class RoomItem{
		String type, title, lastMsg, time;
		RoomItem(String type, String title, String lastMsg, String time){
			this.type = type;
			this.title = title;
			this.lastMsg = lastMsg;
			this.time = time;
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
			g2.setColor(Color.WHITE);
			g2.setFont(new Font(Theme.FONT, Font.BOLD, 14));
			int strW = g2.getFontMetrics().stringWidth(initial);
			g2.drawString(initial, (34 - strW) / 2, 24);
			g2.dispose();
		}
	}
	
	private static class UserListRenderer extends DefaultListCellRenderer{
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
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
			case "AVAILABLE" -> { statusText = "대화가능"; statusColor = Theme.PRIMARY; }
			case "AWAY" -> { statusText = "자리비움"; statusColor = Theme.ACCENT; }
			case "BUSY" -> { statusText = "다른용무중"; statusColor = Theme.ACCENT; }
			case "DND" -> { statusText = "방해금지"; statusColor = Theme.TEXT; }
			default -> {statusText = "오프라인"; statusColor = Theme.TEXT; }
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
	
	private static class RoomListRenderer extends DefaultListCellRenderer{
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			RoomItem item = (RoomItem) value;
			JPanel row = new JPanel(new BorderLayout(12, 0));
			row.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
			row.setBackground(isSelected ? Theme.SUB : Color.WHITE);
			
			Color badgeColor = "DEPT".equals(item.type) ? Theme.ACCENT : "GROUP".equals(item.type) ? Theme.PRIMARY
					: new Color(135, 150, 165);
			AvatarPanel avatar = new AvatarPanel(item.title.substring(0, 1), badgeColor);
			
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
