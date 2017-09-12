package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ServerLogic extends Thread{

	Server owner;
	InputStream input;
	PrintWriter output;
	
	public ServerLogic(Server owner, InputStream is, PrintWriter pw) {
		this.owner = owner;
		this.input = is;
		this.output = pw;
	}
	
	@Override
	public void run() {

		String command = Helpers.convertStreamToString(input);
		String[] commands = command.split("\r\n");
		String contents;
		
		switch(commands[0]) {
		case Server.NEW_SERVER:
			String hostName = commands[1];
			int masterPort = Integer.parseInt(commands[2]);
			int serverPort = Integer.parseInt(commands[3]);
			
			break;
		case Server.HEARTBEAT:
			this.output.println(Server.HEARTBEAT);
			String portSrc = commands[1];
			break;
		case Server.GET:
		case Server.ALIVE:
		case Server.BROADCAST:
		}

	}
}
