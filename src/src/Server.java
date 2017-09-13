package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

	public final static String HOSTNAME 			= "localhost";
	public final static String ACK 					= "--ack--";
	public final static String NEW_SERVER 			= "--new-server--";
	public final static String HEARTBEAT 			= "--heartbeat--";
	public final static String MESSAGE 				= "--message--";
	public final static String GET 					= "get";
	public final static String ALIVE 				= "alive";
	public final static String BROADCAST 			= "broadcast";

	public final static int PORT = 20000;

	int n;
	int masterListenerPortNumber;
	int serverListenerPortNumber;
	int serverListeningPort;
	ServerSocket serverListeningSocket;
	ServerSocket masterListeningSocket;
	Map<Integer, Integer> activeServers;
	List<String> messages;

	public Server(int id, int n, int portNumber) {
		this.n = n;
		this.masterListenerPortNumber = portNumber;
		this.serverListenerPortNumber = PORT + id;
		this.activeServers = new HashMap<Integer, Integer>();
		this.activeServers.put(this.serverListenerPortNumber, this.masterListenerPortNumber);
		
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
		System.out.println( "" + masterListenerPortNumber + ": Started");
		
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

	//Attempt to contact all servers and set up a heartbeat
	//We can contact all other servers by going through the portnumbers
	public void notifyServers() {
		for (int i = PORT; i < PORT + this.n; i++) {
			if (i == this.serverListenerPortNumber) continue;
			System.out.println( "" + this.masterListenerPortNumber + ": Creating Heartbeat to " + (i));
			createHeartbeat(HOSTNAME, i);
		}
	}

	public void createHeartbeat(String hostName, int targetServerPortNumber) {
		Heartbeat hb = new Heartbeat(this, hostName, targetServerPortNumber);
		hb.start();
	}
	
	public synchronized void addMessage(String msg) {
		this.messages.add(msg);
	}
	
	public List<String> getMessages() {
		return this.messages;
	}
	
	public synchronized boolean serverExists(int portNumber) {
		return this.activeServers.get(portNumber) != null;
	}
	
	public synchronized void addServer(int serverPortNumber, int masterPortNumber) {
		System.out.println("Adding " + serverPortNumber + " to " + this.masterListenerPortNumber);
		this.activeServers.put(serverPortNumber, masterPortNumber);
	}
	
	public synchronized void removeServer(int serverPortNumber) {
		System.out.println("Removing " + serverPortNumber + " from " + this.masterListenerPortNumber);
		this.activeServers.remove(serverPortNumber);
	}

	public synchronized List<Integer> getMasters() {
		List<Integer> lst =  new ArrayList<Integer>(this.activeServers.values());
		Collections.sort(lst);
		return lst;
	}
	
	public synchronized List<Integer> getServers() {
		return new ArrayList<Integer>(this.activeServers.keySet());
	}

	public static void main(String[] args) {
		int id = Integer.parseInt(args[0]);
		int n = Integer.parseInt(args[1]);
		int port = Integer.parseInt(args[2]);
		Server s = new Server(id, n, port);
	}
}
