import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.simple.parser.ParseException;

import RemoteCommand.CMD;

/**
 * @author Aneesh Neelam
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	static CMD instance = null;

	public static void main(String[] args) throws IOException,
			NumberFormatException, ParseException {
		checkserver();
	}

	public static void checkserver() throws IOException, NumberFormatException,
			ParseException {
		String SignupUrl = "http://s3cur3command.appspot.com";
		URL url = null;
		URLConnection conn = null;
		InputStream is = null;
		url = new URL(SignupUrl);
		conn = url.openConnection();
		is = conn.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read;
		byte[] input = new byte[4096];
		while (-1 != (read = is.read(input))) {
			buffer.write(input, 0, read);
		}
		input = buffer.toByteArray();
		is.close();
		String servermsg = null;
		servermsg = new String(input, "UTF-8");
		System.out.println(servermsg + "\n");
		if (servermsg.equals("Hello from Command Center!")) {
			instance = new CMD();
		} else {
			System.out.println("Server is down. Please try again later. \n");
		}
	}
}
