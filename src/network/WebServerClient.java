package network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import app.IMediator;

public class WebServerClient {
	private static final String WEB_URL = "http://localhost:8888/SharixWebServer/sharix";
	private URL url;
	private HttpURLConnection con;
	private IMediator mediator;

	public WebServerClient(IMediator med) {
		this.mediator = med;
		try {
			url = new URL(WEB_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public String sendPost(String urlParam) throws Exception {
		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setDoOutput(true);

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParam);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		con.disconnect();
		return response.toString();
	}

	public void publishUser() {
		try {
			sendPost("type=newuser&username=" + mediator.getUserName() + "&"
					+ mediator.getOwnFiles() + "&port=" + mediator.getPort()
					+ "&address=" + mediator.getAddress());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void unpublishUser() {
		System.out.println("*unpublish user");
		try {
			sendPost("type=removeuser&username=" + mediator.getUserName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateUsers() {
		try {
			String users = sendPost("type=requestusers&username="
					+ mediator.getUserName());
			if (users.length() == 0) {
				System.out.println("no users");
				return;
			}
			String[] up = users.split("&");
			ArrayList<String> names = new ArrayList<String>();
			ArrayList<Integer> ports = new ArrayList<Integer>();
			ArrayList<String> addresses = new ArrayList<String>();
			for (String u : up) {
				String username = u.split(":")[0];
				Integer port = Integer.valueOf(u.split(":")[1]);
				String address = u.split(":")[2];
				names.add(username);
				ports.add(port);
				addresses.add(address);
			}
			mediator.updateUsersList(names, ports, addresses);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> requestFiles(String username) {
		ArrayList<String> ret = new ArrayList<String>();
		try {
			String files = sendPost("type=requestuserfiles&requesteduser="
					+ username);
			if (files.length() == 0) {
				System.out.println("no users");
				return ret;
			}
			String[] _files = files.split("&");
			ret = new ArrayList<String>(Arrays.asList(_files));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public void sendUpdateFiles() {
		try {
			sendPost("type=update&username=" + mediator.getUserName() + "&"
					+ mediator.getOwnFiles());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
