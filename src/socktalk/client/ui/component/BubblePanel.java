package socktalk.client.ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import socktalk.client.Theme;

public class BubblePanel extends JPanel{
	private static final int MAX_BUBBLE_WIDTH = 220; // 줄바꿈용 변수
	
	private BubblePanel(boolean isMine) {
		super(new FlowLayout(isMine ? FlowLayout.RIGHT: FlowLayout.LEFT, 0, 0));
		setOpaque(false);
		setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
	}
	
	// 내가 보낸 메시지
	public static BubblePanel myMessage(String content, String time) {
		BubblePanel row = new BubblePanel(true);
		
		JPanel bubbleLine = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		bubbleLine.setOpaque(false);
		
		bubbleLine.add(createTimeLabel(time));
		bubbleLine.add(createTextBubble(content, true));
		
		row.add(bubbleLine);
		return row;
	}
	
	// 상대방이 보낸 메시지
	public static BubblePanel otherMessage(String senderName, String content, String time) {
		BubblePanel row = new BubblePanel(false);
		
		JPanel messageBox = new JPanel();
		messageBox.setLayout(new BoxLayout(messageBox, BoxLayout.Y_AXIS));
		messageBox.setOpaque(false);
		
		JLabel nameLbl = new JLabel(senderName);
		nameLbl.setFont(new Font(Theme.FONT, Font.BOLD, 11));
		nameLbl.setForeground(Theme.TEXT);
		nameLbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 3, 0));
		nameLbl.setAlignmentX(LEFT_ALIGNMENT);
		
		JPanel bubbleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		bubbleLine.setOpaque(false);
		bubbleLine.setAlignmentX(LEFT_ALIGNMENT);
		
		bubbleLine.add(createTextBubble(content, false));
		bubbleLine.add(createTimeLabel(time));
		
		messageBox.add(nameLbl);
		messageBox.add(bubbleLine);
		row.add(messageBox);
		return row;
	}
	
	// 텍스트 말풍선
	private static JPanel createTextBubble(String text, boolean isMine) {
		JPanel bubble = new JPanel(new BorderLayout());
		bubble.setBackground(isMine ? Theme.PRIMARY : Color.WHITE);
		bubble.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(isMine ? Theme.PRIMARY : Theme.SUB, 1, true),
				BorderFactory.createEmptyBorder(8, 12, 8, 12)
		));
		
		JTextArea textArea = new JTextArea(text);
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(new Font(Theme.FONT, Font.PLAIN, 13));
		textArea.setForeground(isMine ? Theme.TEXT_ON_COLOR : Theme.TEXT);
		
		// 긴 글일 때 MAX_BUBBLE_WIDTH로 자동 줄바꿈
		int maxLineWidth = 0;
		for (String line : text.split("\n", -1)) {
			int w = textArea.getFontMetrics(textArea.getFont()).stringWidth(line);
			if (w > maxLineWidth) {
				maxLineWidth = w;
			}
		}
		if (maxLineWidth > MAX_BUBBLE_WIDTH) {
			textArea.setSize(new Dimension(MAX_BUBBLE_WIDTH, Short.MAX_VALUE));
			textArea.setPreferredSize(new Dimension(MAX_BUBBLE_WIDTH, textArea.getPreferredSize().height));
		}
		
		bubble.add(textArea, BorderLayout.CENTER);
		return bubble;
	}
	
	// 시간 표시
	private static JLabel createTimeLabel(String time) {
		JLabel timeLbl = new JLabel(time);
		timeLbl.setFont(new Font(Theme.FONT, Font.PLAIN, 10));
		timeLbl.setForeground(Theme.TEXT);
		return timeLbl;
	}
}
