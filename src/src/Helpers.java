
import java.io.IOException;
import java.net.ServerSocket;

public class Helpers {

	public final static int FREE_SERVER_PORT = 20016;
	public final static int MAX_SERVER_PORT = 29999;

	static ServerSocket findFreePort() throws IOException {
	    for (int port = FREE_SERVER_PORT; port < MAX_SERVER_PORT; port++) {
	        try {
	            return new ServerSocket(port);
	        } catch (IOException ex) {
	            continue;
	        }
	    }
	    throw new IOException("no free port found");
	}
	
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}
