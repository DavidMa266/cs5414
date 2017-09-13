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
	int targetMasterListenerPortNumber;

	public Heartbeat(Server server, String hostName, int targetServerPortNumber) {
		this.server = server;
		this.serverHostName = Server.HOSTNAME;
		this.targetHostName = hostName;
		this.targetServerListenerPortNumber = targetServerPortNumber;
	}

	// Returns true if a server has been discovered
	public boolean discoverNewServer() {
		try {
			if (this.server.serverExists(this.targetServerListenerPortNumber)) return false;
			Socket discover = new Socket(this.targetHostName, this.targetServerListenerPortNumber);
			this.server.addServer(this.targetServerListenerPortNumber, -1);
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

	// Heartbeats are for server-server connection
	// The initial connect is done using the serverListenerPortNumber
	// The subsequent connects are created from a new Socket
	@Override
	public void run() {
		if (discoverNewServer()) {
			// If we discover a server at this listener port number, we have contacted it
			// and received a heartbeat port to connect to
			// Then we actually form the heartbeat

			this.server.addServer(this.targetServerListenerPortNumber, this.targetMasterListenerPortNumber);
			while (true) {
				try {
					Socket heartbeat = new Socket(targetHostName, this.targetServerListenerPortNumber);
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
			this.server.removeServer(this.targetServerListenerPortNumber);
		}
	}
}
