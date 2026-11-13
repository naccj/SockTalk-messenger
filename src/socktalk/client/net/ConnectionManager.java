package socktalk.client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class ConnectionManager {
	private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());
	private Socket socket;
	
	private String serverAddress;
	private int serverPort;
	
	private BufferedReader in;
	private PrintWriter out;
	
	public ConnectionManager(String serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}
	
	public void connectToServer() throws UnknownHostException, IOException{
		socket = new Socket();
		SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
		socket.connect(sa, 3000);
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}
	
	private void startReceiveThread() {
		
	}
	
	public synchronized void send(String message) {
		
	}
	
	public void disconnect() {
		
	}
	
	
	
	
}
