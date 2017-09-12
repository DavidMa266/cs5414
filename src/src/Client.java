package src;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	int portNumber;
	
	
	public Client(String machineName, int portNumber) {
		try {
			Socket s = new Socket(machineName, portNumber);
			
			PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
			pw.println("Hello from the client");
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	
	public static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public static void main(String[] args) {
		String name = args[0];
		int arg = Integer.parseInt(args[1]);
		Client s = new Client(name, arg);
	}
	
}