package socktalk.client.ui;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JComboBox;

import socktalk.client.Theme;
import socktalk.client.net.ConnectionManager;
import socktalk.common.Message;
import socktalk.common.Protocol;

public class StatusMenu extends JComboBox<String> {
	private static final String[] LABELS = {"대화가능", "자리비움", "다른용무중", "방해금지"};
	private static final String[] CODES = {"AVAILABLE", "AWAY", "BUSY", "DND"};
	
	public StatusMenu(ConnectionManager cm) {
		super(LABELS);
		setFont(new Font(Theme.FONT, Font.PLAIN, 11));
		setBackground(Theme.BG);
		setForeground(Theme.TEXT);
		setPreferredSize(new Dimension(110, 28));
		
		addActionListener(e -> {
			if (cm == null) {
				return;
			}
			int idx = getSelectedIndex();
			if (idx >= 0 && idx < CODES.length) {
				cm.send(new Message(Protocol.STATUS, CODES[idx]));
			}
		});
	}
	
	public String getSelectedStatusCode() {
		int idx = getSelectedIndex();
		return (idx >= 0 && idx < CODES.length) ? CODES[idx] : "AVAILABLE"; 
	}

}
