package app;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Config {

	private File configFile;
	private String path = "config.xml";
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document doc;

	private String username = "";
	private Integer port;
	private String address;

	public Config() {
		try {
			configFile = new File(path);
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
		NodeList nList = doc.getElementsByTagName("user");
		Element node = (Element) nList.item(0);
		username = node.getAttribute("username");
		port = Integer.parseInt(node.getElementsByTagName("port").item(0)
				.getTextContent());
		address = node.getElementsByTagName("address").item(0).getTextContent();
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
}
