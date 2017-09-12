package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Heartbeat extends Thread{
	
	Server server;
	
	String serverHostName;
	String targetHostName;
	
	int targetServerListenerPortNumber;
	int targetHeartbeatPortNumber;
	int targetMasterListenerPortNumber;
	
	public Heartbeat(Server server, String hostName, int targetServerPortNumber, int targetMasterPortNumber) {
		this.server = server;
		this.serverHostName = Server.HOSTNAME;
		this.targetHostName = hostName;
		
		this.targetServerListenerPortNumber = targetServerPortNumber;
		this.targetMasterListenerPortNumber = targetMasterPortNumber;
	}
	
	//Returns true if a server has been discovered
	public boolean discoverNewServer(String targetHostName, int targetPortNumber) { 
		try {
			//S1 checks to see if S2 is in its list
			//S1 discovers S2
			//S1 sends S2 a NEW SERVER call
			//S1 sends S2 its masterPort and serverPort
			//S2 creates new thread and new port
			//S2 sends S1 a port to conect heartbeat to
			//S1 records masterPort
			//If it already exists, we don't want to create a new heartbeat listener, so just quit this thread out
			if (this.server.serverExists(this.targetMasterListenerPortNumber)) return false;
			
			Socket discover = new Socket(targetHostName, targetPortNumber);
			PrintWriter pw = new PrintWriter(discover.getOutputStream(), true);
			pw.println(Server.NEW_SERVER);
			pw.println(Server.HOSTNAME);
			pw.println(this.server.masterListenerPortNumber);
			pw.println(this.server.serverListenerPortNumber);
			//So we send the other server our Hostname and listenerportnubmer so they can find us
			//We receive an open port to connect
			
			InputStream is = discover.getInputStream();				
			String command = Helpers.convertStreamToString(is);
			String[] commands = command.split("\r\n");
			this.targetHeartbeatPortNumber = Integer.parseInt(commands[0]);
			
			discover.close();
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
		return false;
	}
	
	//Heartbeats are for server-server connection
	//The initial connect is done using the serverListenerPortNumber
	//The subsequent connects are created from a new Socket
	@Override
	public void run() { 	
			if (discoverNewServer(this.targetHostName, this.targetServerListenerPortNumber)) {
				//If we discover a server at this listener port number, we have contacted it and received a heartbeat port to connect to
				//Then we actually form the heartbeat
				this.server.addServer(this.targetMasterListenerPortNumber);
				while (true) {
					try {
						Socket heartbeat = new Socket(targetHostName, targetHeartbeatPortNumber);
						System.out.println("Sending from " + this.server.serverListenerPortNumber + " to " + this.targetHeartbeatPortNumber);
						PrintWriter pw = new PrintWriter(heartbeat.getOutputStream(), true);
						pw.println(Server.HEARTBEAT);
						pw.println(this.serverHostName);
						pw.println(this.server.serverListenerPortNumber);
						heartbeat.close();
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						System.out.println("Reaching the IO Exception within Heartbeat.java");
						break;
					}
				}
				this.server.removeServer(this.targetMasterListenerPortNumber);
			}
	}
}
