package socktalk.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/* 
 * 계정파일을 읽어 로그인 검증
 * 파일 형식(한 줄에 계정 하나, UTF-8): 아이디|비밀번호|이름|부서
 */
public class AccountStore {
	private static final Logger logger = Logger.getLogger(AccountStore.class.getName());
	
	/* 계정 1개 */
	public static class Account {
		public final String id;
		public final String password;
		public final String name;
		public final String dept;
		
		public Account(String id, String password, String name, String dept) {
			this.id = id;
			this.password = password;
			this.name = name;
			this.dept = dept;
		}
	}
	
	private Vector<Account> accounts = new Vector<Account>();
	
	public AccountStore(String path) {
		load(path);
	}
	
	/* 파일이 없거나 형식이 틀려도 서버가 죽지 않게 로그만 남김 */
	private void load(String path) {
		File file = new File(path);
		
		if (!file.exists()) {
			logger.severe("계정 파일이 없습니다: " + file.getAbsolutePath());
			return;	
		}
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			String line;
			int lineNo = 0;
			
			while ((line = br.readLine()) != null) {
				lineNo++;
				// 윈도우 메모장이 UTF-8로 저장하면 맨 앞에 붙는 BOM 제거
				if (lineNo == 1 && line.startsWith("\uFEFF")) {
					line = line.substring(1);
				}

				if (line.trim().isEmpty() || line.startsWith("#")) {
					continue;
				}

				String[] p = line.split("\\|", -1);
				if (p.length != 4 || p[0].trim().isEmpty()) {
					logger.warning(lineNo + "번째 줄 형식 오류, 건너뜀");
					continue;
				}

				accounts.add(new Account(p[0].trim(), p[1], p[2].trim(), p[3].trim()));
			}

			logger.info("계정 " + accounts.size() + "개를 읽었습니다.");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "계정 파일 읽기 오류", e);
		}
	}

	/* 아이디와 비밀번호가 맞으면 계정을, 아니면 null을 반환한다. */
	public Account login(String id, String password) {
		for (Account a : accounts) {
			if (a.id.equals(id) && a.password.equals(password)) {
				return a;
			}
		}
		return null;
	}
}
