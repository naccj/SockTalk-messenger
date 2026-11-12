package socktalk.client;

import java.awt.EventQueue;

import socktalk.client.net.ConnectionManager;
import socktalk.client.ui.LoginFrame;

public class ClientMain {
	public static void mai(String[] args) {
		String serverAddress = "localhost";
		int serverPort = 54321;
		
		ConnectionManager cm = new ConnectionManager(serverAddress, serverPort);
		
		EventQueue.invokeLater(() -> {
			LoginFrame frame = new LoginFrame(cm);
			frame.setVisible(true);
		});
	}
}
