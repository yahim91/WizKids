package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Config {

	private File configFile;
	private File sharedDocFile;
	private String path = "";
	private String users_folder = "users_folder/";
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document doc;

	private String username = "";
	private String logPropertiesFile = "";
	private Integer port;
	private String address;
	private IMediator mediator;

	public Config(String username, IMediator med) {
		this.mediator = med;
		try {
			this.username = username;
			configFile = new File(path + username + ".xml");
			sharedDocFile = new File(users_folder + username + ".txt");
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(configFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readConfigFile() {
		NodeList nList = doc.getElementsByTagName("app");
		Element node = (Element) nList.item(0);
		port = Integer.parseInt(node.getElementsByTagName("port").item(0)
				.getTextContent());
		address = node.getElementsByTagName("address").item(0).getTextContent();
		logPropertiesFile = node.getElementsByTagName("logproperties").item(0).getTextContent();
		
	}

	public String getUsername() {
		return username;
	}

	public Integer getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}
	
	public void getFiles() {
		/* TODO to remove after database implementation */
		ArrayList<String> users = new ArrayList<String>();
		users.add("duffy");
		users.add("sam");
		users.add("bugs");
		try {
			mediator.addUser(username, new ArrayList<String>());
			BufferedReader buffer = new BufferedReader(new FileReader(sharedDocFile));
			ArrayList<String> files = new ArrayList<String>();
			String file = "";
			while((file = buffer.readLine()) != null) {
				files.add(file);
			}
			mediator.updateUserFiles(username, files);
			for(String user : users) {
				if (user.equals(username))
					continue;
				if (user.equals("duffy")) {
					port = 8888;
				} else if (user.equals("bugs")){
					port = 9999;
				} else {
					port = 7777;
				}
				mediator.addUser(user, new ArrayList<String>());
				mediator.updateUserInfo(user, port, "localhost");
				buffer.close();
				buffer = new BufferedReader(new FileReader(new File(users_folder + user + ".txt")));
				files.clear();
				while((file = buffer.readLine()) != null) {
					files.add(file);
				}
				mediator.updateUserFiles(user, files);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getLogFileName() {
		return logPropertiesFile;
	}
}
