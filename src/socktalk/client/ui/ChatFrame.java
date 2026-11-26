package socktalk.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import socktalk.client.Theme;
import socktalk.client.net.ConnectionManager;
import socktalk.client.ui.component.BubblePanel;
import socktalk.common.Message;
import socktalk.common.Protocol;

public class ChatFrame extends JFrame{
	private ConnectionManager cm;
	private String roomId;
	private String roomTitle;
	private String roomType;
	private String myId;
	
	private JPanel messageListPanel;
	private JScrollPane scrollPane;
	private JTextField inputField;
	private JButton sendBtn;
	
	public ChatFrame(ConnectionManager cm, String roomId, String roomTitle, String roomType, String myId) {
		this.cm = cm;
		this.roomId = roomId;
		this.roomTitle = roomTitle;
		this.roomType = roomType;
		this.myId = myId;
		
		initFrame();
		initComponent();
	}
	
	public String getRoomId() {
		return roomId;
	}
	
	private void initFrame() {
		setTitle(roomTitle);
		setSize(420, 620);
		setMinimumSize(new Dimension(360, 480));
		// 대화창만 닫기(메인은 유지)
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
	}
	
	private void initComponent() {
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(Theme.SUB);
		headerPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.PRIMARY),
				BorderFactory.createEmptyBorder(12, 16, 12, 16)
		));
		
		JLabel titleLabel = new JLabel(roomTitle);
		titleLabel.setFont(new Font(Theme.FONT, Font.BOLD, 16));
		titleLabel.setForeground(Theme.TEXT);
		
		String typeText = "DEPT".equals(roomType) ? "부서 단체방" : "GROUP".equals(roomType) ? "그룹 채팅" : "1:1 채팅";
		JLabel typeLabel = new JLabel(typeText);
		typeLabel.setFont(new Font(Theme.FONT, Font.PLAIN, 11));
		typeLabel.setForeground(Theme.TEXT);
		
		headerPanel.add(titleLabel, BorderLayout.WEST);
		headerPanel.add(typeLabel, BorderLayout.EAST);
		add(headerPanel, BorderLayout.NORTH);
		
		// 메시지 부분
		messageListPanel = new JPanel();
		messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
		messageListPanel.setBackground(Theme.BG);
		messageListPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		
		// 메시지 처음 시작 부분은 위부터 쌓이게
		JPanel messageWrapper = new JPanel(new BorderLayout());
		messageWrapper.setBackground(Theme.BG);
		messageWrapper.add(messageListPanel, BorderLayout.NORTH);
		
		scrollPane = new JScrollPane(messageWrapper);
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);
		
		// 입력창
		JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
		inputPanel.setBackground(Color.WHITE);
		inputPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.SUB),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)
		));
		
		inputField = new JTextField();
		inputField.setFont(new Font(Theme.FONT, Font.PLAIN, 13));
		inputField.setForeground(Theme.TEXT);
		inputField.addActionListener(e -> sendMessage());
		
		sendBtn = new JButton("전송");
		sendBtn.setFont(new Font(Theme.FONT, Font.BOLD, 13));
		sendBtn.setBackground(Theme.PRIMARY);
		sendBtn.setForeground(Theme.TEXT_ON_COLOR);
		sendBtn.setFocusPainted(false);
		sendBtn.setBorderPainted(false);
		sendBtn.setPreferredSize(new Dimension(68, 34));
		sendBtn.addActionListener(e -> sendMessage());
		
		inputPanel.add(inputField, BorderLayout.CENTER);
		inputPanel.add(sendBtn, BorderLayout.EAST);
		add(inputPanel, BorderLayout.SOUTH);
	}
	
	private void sendMessage() {
		String text = inputField.getText().trim();
		// 입력칸이 비어있으면 전송 x
		if (text.isEmpty()) {
			return;
		}
		if (cm != null && cm.isConnected()) {
			cm.send(new Message(Protocol.CHAT, roomId, text));
		}
		inputField.setText("");
		inputField.requestFocusInWindow();
	}
	
	public void appendMessage(String senderId, String senderName, String time, String content) {
		BubblePanel bubbleRow = myId.equals(senderId) 
				? BubblePanel.myMessage(content, time) : BubblePanel.otherMessage(senderName, content, time);
		
		messageListPanel.add(bubbleRow);
		messageListPanel.add(Box.createVerticalStrut(8));
		messageListPanel.revalidate();
		messageListPanel.repaint();
		
		SwingUtilities.invokeLater(() -> {
			JScrollBar bar = scrollPane.getVerticalScrollBar();
			bar.setValue(bar.getMaximum());
		});
	}	
}
