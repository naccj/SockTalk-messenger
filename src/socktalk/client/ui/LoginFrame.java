package socktalk.client.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import socktalk.client.Theme;
import socktalk.client.net.ConnectionManager;

public class LoginFrame extends JFrame {
	private ConnectionManager cm;
	private JPanel panel;
	private JTextField idField;
	private JPasswordField pwField;
	private JButton loginButton;
	
	public LoginFrame(ConnectionManager cm) {
		this.cm = cm;
		
		setTitle("SockTalk Login");
		setBounds(100, 100, 380, 620);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel();
		panel.setBackground(Theme.BG);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.setLayout(null);
		setContentPane(panel);
		
		// 제목
		JLabel titleLabel = new JLabel("Sock-Talk", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 26));
		titleLabel.setForeground(Theme.PRIMARY);
		titleLabel.setBounds(0, 120, 380, 35);
		
		// 부제
		JLabel subTitleLabel = new JLabel("xx회사 메신저", SwingConstants.CENTER);
		subTitleLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
		subTitleLabel.setForeground(Theme.TEXT);
		subTitleLabel.setBounds(0, 160, 380, 20);
		
		// 아이디 입력		
		idField = new JTextField();
		idField.setBounds(57, 240, 265, 35);
		Placeholder.setPlaceholder(idField, "아이디를 입력하세요");
		
		// 비밀번호 입력
		pwField = new JPasswordField();
		pwField.setBounds(57, 285, 265, 35);
		Placeholder.setPlaceholder(pwField, "비밀번호를 입력하세요");
		
		// 로그인 버튼
		loginButton = new JButton("로그인");
		loginButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
		loginButton.setBackground(Theme.PRIMARY);
		loginButton.setForeground(Theme.TEXT_ON_COLOR);
		loginButton.setFocusPainted(false);
		loginButton.setBounds(57, 335, 265, 42);
		
		panel.add(titleLabel);
		panel.add(subTitleLabel);
		panel.add(idField);
		panel.add(pwField);
		panel.add(loginButton);
		
		panel.setFocusable(true);
		panel.requestFocusInWindow();
		
		loginButton.setEnabled(false);
		loginListener();
	}
	
	private void loginListener() {
		loginButton.addActionListener(e -> tryLogin());
		pwField.addActionListener(e -> tryLogin());
		
		idField.getDocument().addDocumentListener(docListener);
		pwField.getDocument().addDocumentListener(docListener);
	}
	
	private void tryLogin() {
		if(!loginButton.isEnabled()) {
			return;
		}
		String id = idField.getText().trim();
		String pw = new String(pwField.getPassword()).trim();		
	}
	
	private void updateInputState() {
		String id = idField.getText().trim();
		String pw = new String(pwField.getPassword()).trim();
		
		boolean isIdField = !id.isEmpty() && !id.equals("아이디를 입력하세요");
		boolean isPwField = !pw.isEmpty() && !pw.equals("비밀번호를 입력하세요");
		
		loginButton.setEnabled(isIdField && isPwField);
	}
	
	private DocumentListener docListener = new DocumentListener() {
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateInputState();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateInputState();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateInputState();
		}
	};
	
	public static void main(String[] args) {
		LoginFrame frame = new LoginFrame(null);
		frame.setVisible(true);
	}

	

	

}
