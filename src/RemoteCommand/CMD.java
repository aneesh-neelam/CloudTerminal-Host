package RemoteCommand;

/**
 * 
 * 
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Logs.CMDLogs;

/**
 * @author Aneesh Neelam
 * 
 */
public class CMD {
	private int SystemID;
	CMDLogs cmdlogs;
	private String username;
	private String password;

	public CMD() throws NumberFormatException, IOException, ParseException {
		cmdlogs = new CMDLogs();
		File sys = new File("SysID.dat");
		if (sys.exists()) {
			JSONParser parser = new JSONParser();
			// Scanner sc = new Scanner(System.in);
			Object obj = parser.parse(new FileReader("SysID.dat"));
			JSONArray jsonarr = (JSONArray) obj;
			SystemID = Integer.parseInt((String) jsonarr.get(0));
			username = (String) jsonarr.get(1);
			System.out.println("Stored SystemID: " + SystemID);
			System.out.println("Stored Username: " + username);
			// System.out.println("Enter Password: ");
			// password = sc.nextLine();
			// sc.close();
		} else {
			System.out.println("SystemID not found. Continue with Signup. ");
			cmdlogs.Write("SystemID not found");
			Signup();
		}
		Cron();

	}

	private void Cron() {
		Timer t = new Timer();
		t.scheduleAtFixedRate(timer, 1000, 10000);
	}

	private void Signup() throws IOException, ParseException {
		String SignupUrl = "http://s3cur3command.appspot.com/IDChallenge";
		URL url = null;
		url = new URL(SignupUrl);
		URLConnection conn = null;
		InputStream is = null;
		conn = url.openConnection();
		is = conn.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read;
		byte[] input = new byte[4];
		while (-1 != (read = is.read(input))) {
			buffer.write(input, 0, read);
		}
		input = buffer.toByteArray();
		String str = null;
		str = new String(input, "UTF-8");
		SystemID = Integer.parseInt(str);
		System.out.println("Got SystemID: " + str);
		is.close();
		Scanner sc = new Scanner(System.in);
		System.out.println("Create Username: ");
		String username = sc.nextLine();
		System.out.println("Create Password: ");
		String pass1 = sc.nextLine();
		System.out.println("Confirm Password: ");
		String pass2 = sc.nextLine();
		if (pass1.equals(pass2)) {
			password = pass1;
			String AuthUrl = "http://s3cur3command.appspot.com/Authentication/"
					+ SystemID + "/" + username + "/" + password;
			url = new URL(AuthUrl);
			conn = url.openConnection();
			is = conn.getInputStream();
			buffer = new ByteArrayOutputStream();
			input = new byte[4096];
			while (-1 != (read = is.read(input))) {
				buffer.write(input, 0, read);
			}
			input = buffer.toByteArray();
			String jsonstr = new String(input, "UTF-8");
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(jsonstr);
			JSONArray jsonarr = (JSONArray) obj;
			SystemID = Integer.parseInt((String) jsonarr.get(0));
			username = (String) jsonarr.get(1);
			System.out.println("SystemID: " + SystemID);
			System.out.println("Username: " + username);
			try {

				FileWriter file = new FileWriter("SysID.dat");
				file.write(jsonarr.toString());
				file.flush();
				file.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out
					.println("Successfully registered and wrote SystemID to file. ");
			cmdlogs.Write("New Username/Password registered, SysID written");
		} else {
			System.out
					.println("Invalid username/password entered. Try again. ");
			cmdlogs.Write("Invalid Username/Password for new user");
		}
		sc.close();
	}

	TimerTask timer = new TimerTask() {
		@Override
		public void run() {
			System.out.println("\nFetching tasks from server. ");
			String urlString = "http://s3cur3command.appspot.com/CommandDisplayer/"
					+ SystemID;
			URL url = null;
			InputStream is = null;
			try {
				url = new URL(urlString);
			} catch (MalformedURLException e1) {
				System.out.println("Invalid URL. ");
				e1.printStackTrace();
			}
			URLConnection conn = null;
			try {
				conn = url.openConnection();
			} catch (IOException e2) {
				System.out.println("Network Error. ");
				e2.printStackTrace();
			}
			try {
				is = conn.getInputStream();
			} catch (IOException e1) {
				System.out.println("I/O Error. ");
				e1.printStackTrace();
			}
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] input = new byte[4096];
			int read;
			String jsonstr = null;
			try {
				while (-1 != (read = is.read(input))) {
					buffer.write(input, 0, read);
				}
			} catch (IOException e1) {
				System.out.println("Cannot read from HTTP page. ");
				e1.printStackTrace();
			}
			input = buffer.toByteArray();
			try {
				jsonstr = new String(input, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				System.out.println("Unsupported Encoding. ");
				e.printStackTrace();
			}
			if (jsonstr.equals("empty")) {
				System.out.println("No pending tasks to execute. Waiting. ");
			} else {
				JSONParser parser = new JSONParser();
				Object obj = null;
				try {
					obj = parser.parse(jsonstr);
				} catch (ParseException e1) {
					System.out.println("Cannout parse JSON from HTTP page. ");
					e1.printStackTrace();
				}
				try {
					jsonstr = new String(input, "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				parser = new JSONParser();
				try {
					obj = parser.parse(jsonstr);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				JSONArray jsonarr = (JSONArray) obj;
				Iterator<String> iterator = jsonarr.iterator();
				while (iterator.hasNext()) {
					final Runtime rt = Runtime.getRuntime();
					String cmd = iterator.next();
					System.out.println("Attempting to Executing: " + cmd);
					try {
						rt.exec(cmd);
						System.out.println("Executed: " + cmd);
					} catch (IOException e) {
						System.out.println("Could not execute: " + cmd);
						e.printStackTrace();
					}
				}
			}
		}
	};
}
