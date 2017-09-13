package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ServerLogic extends Thread{

	Server server;
	Socket connection;
	
	public ServerLogic(Server server, Socket connection) {
		this.server = server;
		this.connection = connection;
	}
	
	@Override
	public void run() {
		
		try {
			InputStream input = connection.getInputStream();
			PrintWriter output = new PrintWriter(connection.getOutputStream(), true);

			String command = Helpers.convertStreamToString(input);
			String[] commands = command.split("\\s+");
			
			switch(commands[0]) {
				case Server.NEW_SERVER: {
					String hostName = commands[1];
					int serverPort = Integer.parseInt(commands[2]);
					server.createHeartbeat(hostName, serverPort);
					break;
				}
				case Server.HEARTBEAT: {
					break;
				}
				case Server.MESSAGE: {
					this.server.addMessage(command.substring(Server.MESSAGE.length()));
					break;
				}
				case Server.GET: {
					List<String> messages = this.server.getMessages();
					String result = "messages ";
					for(int i = 0; i < messages.size(); i++) {
						result += messages.get(i);
						if( i != messages.size()-1) result += ", ";
					}
					output.println(result);
					break;
				}
				case Server.ALIVE: {
					String result = "alive ";
					List<Integer> serverIds = this.server.getServers();
					for(int i = 0; i < serverIds.size(); i++) { 
						result += serverIds.get(i);
						if (i != serverIds.size() -1) result += ", ";
					}
					output.println(result);
					break;
				}
				case Server.BROADCAST:{
					String message = command.substring(Server.BROADCAST.length());
					List<Integer> serverIds = this.server.getServers();
					for(int i = 0; i < serverIds.size(); i++) {
						try {
							Socket broadcast = new Socket(Server.HOSTNAME, serverIds.get(i));
							PrintWriter pw = new PrintWriter(broadcast.getOutputStream(), true);
							pw.println(Server.MESSAGE + " " + message);
							broadcast.close();
						}
						catch(IOException e) {
							System.out.println("Couldn't connect, presumed dead, moving on");
						}
					}				
				}
			}

			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
