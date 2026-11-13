package socktalk.client;

import javax.swing.SwingUtilities;

import socktalk.client.net.ConnectionManager;
import socktalk.client.ui.LoginFrame;

public class ClientMain {
	public static void main(String[] args) {
		String serverAddress = "localhost";
		int serverPort = 54321;
		
		ConnectionManager cm = new ConnectionManager(serverAddress, serverPort);
		
		SwingUtilities.invokeLater(() -> {
			LoginFrame frame = new LoginFrame(cm);
			frame.setVisible(true);
		});
	}
}
