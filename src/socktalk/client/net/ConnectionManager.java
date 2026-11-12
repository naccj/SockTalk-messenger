package socktalk.client.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import socktalk.client.Message;

public class ConnectionManager {
	private Socket socket;
	
	private String serverAddress;
	private int serverPort;
	
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public ConnectionManager(String serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}
	
	public void connectToServer() throws UnknownHostException, IOException{
		socket = new Socket();
		SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
		socket.connect(sa, 3000);
		
		out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		out.flush();
		in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
	}
	
	public void send(Message msg) {
		try {
			out.writeObject(msg);
			out.flush();
		} catch (IOException e) {
			System.err.println("클라이언트 일반 전송 오류> " + e.getMessage());
		}
	}
	
	
}
