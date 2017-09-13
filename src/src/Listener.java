package src;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Listener extends Thread{

	Server server;
	ServerSocket socket;
	
	public Listener (Server server, ServerSocket socket) {
		this.server = server;
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				Socket connection = socket.accept();
				ServerLogic sl = new ServerLogic(server, connection);
				sl.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
