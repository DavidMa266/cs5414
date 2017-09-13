package src;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Heartbeat extends Thread {

	Server server;

	String serverHostName;
	String targetHostName;

	int targetServerListenerPortNumber;
	int targetId;

	public Heartbeat(Server server, String hostName, int targetServerPortNumber) {
		this.server = server;
		this.serverHostName = Server.HOSTNAME;
		this.targetHostName = hostName;
		this.targetServerListenerPortNumber = targetServerPortNumber;
		this.targetId = targetServerPortNumber - Server.PORT;
	}

	// Returns true if a server has been discovered
	public boolean discoverNewServer() {
		try {
			if (this.server.serverExists(this.targetId)) return false;
			Socket discover = new Socket(this.targetHostName, this.targetServerListenerPortNumber);
			this.server.addServer(targetId);
			PrintWriter pw = new PrintWriter(discover.getOutputStream(), true);
			pw.println(Server.NEW_SERVER + " " + Server.HOSTNAME + " " + this.server.serverListenerPortNumber);
			discover.close();
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("" + server.masterListenerPortNumber + ": Heartbeat failed, server "
					+ this.targetServerListenerPortNumber + " does not appear to be online");
		}
		return false;
	}

	@Override
	public void run() {
		if (discoverNewServer()) {
			while (true) {
				try {
					Socket heartbeat = new Socket(targetHostName, this.targetServerListenerPortNumber);
					/*
					PrintWriter pw = new PrintWriter(heartbeat.getOutputStream(), true);
					pw.println(Server.HEARTBEAT);
					*/
					heartbeat.close();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Lost connection to " + this.targetServerListenerPortNumber);
					break;
				}
			}
			this.server.removeServer(this.targetId);
		}
	}
}
