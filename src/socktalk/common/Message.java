package socktalk.common;

/**
 * 메시지 한 줄(TYPE|값1|값2|...)을 만들고 해석한다.
 * BufferedReader/PrintWriter로 한 줄씩 주고받으므로, 이스케이프는 이 클래스에서만 처리한다.
 *
 * 이스케이프 규칙 (protocol.md 1번)
 *   \  -> \\
 *   |  -> \p
 *   줄바꿈 -> \n
 *   \r -> 제거
 *
 * 한 번 만들면 내용을 바꾸지 않으므로 스레드 사이에 넘겨도 안전하다.
 */
public class Message {
	private static final char SEPARATOR = '|';

	private String type;
	private String[] fields;

	public Message(String type, String... fields) {
		if (type == null || type.isEmpty()) {
			throw new IllegalArgumentException("type must not be empty");
		}
		this.type = type;

		if (fields == null) {
			this.fields = new String[0];
		}
		else {
			this.fields = new String[fields.length];
			for (int i = 0; i < fields.length; i++) {
				this.fields[i] = (fields[i] == null) ? "" : fields[i];
			}
		}
	}

	/* 보낼 한 줄을 만든다 */
	public String encode() {
		StringBuilder sb = new StringBuilder(escape(type));

		for (int i = 0; i < fields.length; i++) {
			sb.append(SEPARATOR);
			sb.append(escape(fields[i]));
		}
		return sb.toString();
	}

	/* 받은 한 줄을 Message로 해석 */
	public static Message decode(String line) {
		if (line == null || line.isEmpty()) {
			return null;
		}

		String[] parts = line.split("\\|", -1);

		String type = unescape(parts[0]);
		if (type.isEmpty()) {
			return null;
		}

		String[] values = new String[parts.length - 1];
		for (int i = 1; i < parts.length; i++) {
			values[i - 1] = unescape(parts[i]);
		}
		return new Message(type, values);
	}

	public String getType() {
		return type;
	}

	/* 필드 하나를 가져온다. 범위를 벗어나면 빈 문자열 (size()로 개수를 먼저 확인한다) */
	public String get(int index) {
		if (index < 0 || index >= fields.length) {
			return "";
		}
		return fields[index];
	}

	public int size() {
		return fields.length;
	}

	/* 로그용 */
	@Override
	public String toString() {
		return "Message[" + type + ", fields=" + fields.length + "]";
	}

	private static String escape(String s) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == '\\') {
				sb.append("\\\\");
			}
			else if (c == '|') {
				sb.append("\\p");
			}
			else if (c == '\n') {
				sb.append("\\n");
			}
			else if (c == '\r') {
				continue;
			}
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/* 앞에서부터 한 글자씩 읽어 되돌린다 */
	private static String unescape(String s) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == '\\' && i + 1 < s.length()) {
				char n = s.charAt(++i);

				if (n == '\\') {
					sb.append('\\');
				}
				else if (n == 'p') {
					sb.append('|');
				}
				else if (n == 'n') {
					sb.append('\n');
				}
				else {
					sb.append('\\').append(n);	// 모르는 이스케이프는 그대로 둔다
				}
			}
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}