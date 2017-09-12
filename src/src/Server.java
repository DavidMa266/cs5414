package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

	public final static String HOSTNAME = "localhost";
	public final static String NEW_SERVER 			= "--new-server--";
	public final static String HEARTBEAT 			= "--heartbeat--";
	public final static String GET 					= "--get--";
	public final static String ALIVE 				= "--alive--";
	public final static String BROADCAST 			= "--broadcast--";

	public final static int PORT = 30000;
	public final static int MAX_PORT = PORT + 4;

	int masterListenerPortNumber;
	int serverListenerPortNumber;
	int serverListeningPort;
	ServerSocket serverListeningSocket;
	ServerSocket masterListeningSocket;
	Map<Integer, Boolean> activeServers;
	List<String> messages;

	public Server(int portNumber) {
		this.masterListenerPortNumber = portNumber;
		this.serverListenerPortNumber = portNumber - 10000;
		this.activeServers = new HashMap<Integer, Boolean>();
		
		//Server starts
		//Create two threads for the two listening sockets
		//Once the master is listening, attempt to notify all other servers of existence
		//If a server is found
		//Connect to it
		//Say hey I am a new Server, here is my Hostname, Master port, and the port you should connect to to set up your own heartbeat 
		//Other server goes: here I created a new port here
		//You can connect here.
		try {
			this.masterListeningSocket = new ServerSocket(this.masterListenerPortNumber);
			this.masterListeningSocket.setReuseAddress(true);
			this.serverListeningSocket = new ServerSocket(this.serverListenerPortNumber);
			this.serverListeningSocket.setReuseAddress(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Listener master = new Listener(this, this.masterListeningSocket);
		Listener server = new Listener(this, this.serverListeningSocket);
		
		master.start();
		server.start();
		
		while(true) {
			try {
				Socket discovery = new Socket(HOSTNAME, this.serverListenerPortNumber);
				break;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
			}
		}
		notifyServers();
	}
	
	public int createServerSocket() {
		try {
			ServerSocket ss = Helpers.findFreePort();
			return ss.getLocalPort();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
		
	}
	
	//Attempt to contact all servers and set up a heartbeat
	public void notifyServers() {
		for (int i = PORT; i < MAX_PORT; i++) {
			if (i == this.masterListenerPortNumber) continue;
			Heartbeat hb = new Heartbeat(this, HOSTNAME, i - 10000, i);
			hb.start();
		}
	}

	public void createHeartbeat(String hostName, int targetServerPortNumber, int targetMasterPortNumber) {
		Heartbeat hb = new Heartbeat(this, hostName, targetServerPortNumber, targetMasterPortNumber);
		hb.start();
	}
	
	public synchronized boolean serverExists(int portNumber) {
		return this.activeServers.get(portNumber) != null;
	}
	
	public synchronized void addServer(int portNumber) {
		System.out.println("Adding " + portNumber + " to " + this.masterListenerPortNumber);
		this.activeServers.put(portNumber, true);
	}
	
	public synchronized void removeServer(int portNumber) {
		System.out.println("Removing " + portNumber + " from " + this.masterListenerPortNumber);
		this.activeServers.remove(portNumber);
	}


	public static void main(String[] args) {
		int arg = Integer.parseInt(args[0]);
		Server s = new Server(arg);
	}
}
