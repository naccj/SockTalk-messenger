package socktalk.client.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Placeholder{
	
	public static void setPlaceholder(JTextField field, String placeholder) {
		field.setText(placeholder);
		field.setForeground(Color.GRAY);
		
		field.addFocusListener(new FocusListener() {
			// 클릭이 감지되면 플레이스홀더를 지우고 검정색으로 작성 준비
			@Override
			public void focusGained(FocusEvent e) {
				if (field.getText().equals(placeholder)) {
					field.setText("");
					field.setForeground(Color.BLACK);
				}
			}
			
			// 작성하지 않은 채로 나가면(다른 곳을 클릭하면) 다시 플레이스홀더 회색으로 나타내기
			@Override
			public void focusLost(FocusEvent e) {
				if(field.getText().trim().isEmpty()) {
					field.setText(placeholder);
					field.setForeground(Color.GRAY);
				}
			}
		});
	}
	
	public static void setPlaceholder(JPasswordField field, String placeholder) {
		char defaultEcho = field.getEchoChar();
		field.setEchoChar((char) 0); // 안내 문구가 보이도록 비밀번호 기호 해제
		field.setText(placeholder);
		field.setForeground(Color.GRAY);
		
		field.addFocusListener(new FocusListener() {
			// 클릭이 감지되면 플레이스 홀더를 지우고 기호로 보이게 작성 준비
			@Override
			public void focusGained(FocusEvent e) {
				String currentPw = new String(field.getPassword());
				if(currentPw.equals(placeholder)) {
					field.setText("");
					field.setForeground(Color.BLACK);
					field.setEchoChar(defaultEcho); // 입력 시 비밀번호 기호 다시 보이기
				}
			}
			// 작성하지 않은 채로 나가면(다른 곳을 클릭하면) 플레이스 홀더를 회색으로 나타내고 
			// 기호로 보이는 것을 글자로 보이기
			@Override
			public void focusLost(FocusEvent e) {
				String currentPw = new String(field.getPassword());
				if(currentPw.trim().isEmpty()) {
					field.setText(placeholder);
					field.setForeground(Color.GRAY);
					field.setEchoChar((char) 0);
				}
			}
			
		});
	}

}
