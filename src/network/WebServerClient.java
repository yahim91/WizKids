package network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
			try {
				con = (HttpURLConnection) url.openConnection();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void sendPost(String urlParam) throws Exception {
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setDoOutput(true);
		
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParam);
		wr.flush();
		wr.close();
		
		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	}
	
	public void publishUser() {
		try {
			sendPost("newuser=" + mediator.getUserName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
