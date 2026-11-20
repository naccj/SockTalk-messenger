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
import java.util.logging.Level;
import java.util.logging.Logger;

import socktalk.common.Message;

public class ConnectionManager {
	private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());
	private Socket socket;
	
	private String serverAddress;
	private int serverPort;
	
	private BufferedReader in;
	private PrintWriter out;
	
	private MessageListener listener;
	private volatile boolean connected = false;
	
	public ConnectionManager(String serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}
	
	public void setMessageListener(MessageListener listener) {
		this.listener = listener;
	}
	
	public boolean isConnected() {
		return connected && socket != null && !socket.isClosed();
	}
	
	
	public void connectToServer() throws UnknownHostException, IOException{
		socket = new Socket();
		SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
		socket.connect(sa, 3000);
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
		connected = true;
		
		startReceiveThread();
	}
	
	private void startReceiveThread() {
		Thread receiveThread = new Thread(() -> {
			try {
				String line;
				while (connected && (line = in.readLine()) != null) {
					Message message = Message.decode(line);
					if(message != null && listener != null) {
						listener.onMessage(message);
					}
			}
		} catch (IOException e) {
			if(connected) {
				logger.log(Level.WARNING, "서버 연결이 끊어졌습니다.", e);
			}
		} finally {
			boolean wasConnected = connected;
			disconnect();
			if (wasConnected && listener != null) {
				listener.onDisconnected();
			}
		}
	}, "Client-Receive-Thread");
		
	receiveThread.setDaemon(true);
	receiveThread.start();
}
	
	public synchronized void send(Message message) {
		if (!isConnected() || out == null || message == null) {
			return;
		}
		out.println(message.encode());
	}
	
	public void disconnect() {
		connected = false;
		try {
			if(in != null) in.close();
		}catch(IOException ignored) {}
		if (out != null) {
			out.close();
		}
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch(IOException ignored) {}
	}

}
