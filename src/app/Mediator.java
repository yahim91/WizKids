package app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;

import network.Network;
import network.WebServerClient;
import statusbar.StatusBar;
import table.FileDownloaderWorker;
import table.ProgressTableModel;
import table.RowData;

public class Mediator implements IMediator {

	private DefaultListModel<UserFiles> uf;
	private DefaultListModel<String> files;
	private ProgressTableModel tm;
	private StatusBar sb;
	private Network network;
	private Config config;
	private WebServerClient webServerClient;
	private ArrayList<String> ownFileNames;
	
	public Mediator(String username) {
	}

	@Override
	public void addNewOwnFile(String fileName) {
		ownFileNames.add(fileName);
	}
	
	@Override
	public void registerStatusBar(StatusBar sb) {
		this.sb = sb;
	}

	@Override
	public void registerProgressTableModel(ProgressTableModel tm) {
		this.tm = tm;
	}

	public void registerUserListModel(DefaultListModel<UserFiles> uf) {
		this.uf = uf;
	}

	public void registerFilesModel(DefaultListModel<String> files) {
		this.files = files;
		this.ownFileNames = config.getInitFiles();
	}

	public void considerUser(UserFiles uf) {
		ArrayList<String> files_i;

		if (uf.getName().equals(config.getUsername())) {
			files_i = uf.getFiles();
		} else {
			files_i = this.webServerClient.requestFiles(uf.getName());
			uf.updateFiles(files_i);
		}

		files.removeAllElements();
		for (String fl : files_i)
			files.addElement(fl);
	}

	public void considerFile(int indexU, int indexF) {
		String fileName = files.get(indexF);
		UserFiles user = uf.get(indexU);
		if (user.getName() == config.getUsername()) {
			return;
		}
		sb.incStarted();

		tm.addRow(new RowData(user.getName(), config.getUsername(), fileName));
		final int index = tm.getRowCount() - 1;
		FileDownloaderWorker fileDownloader = new FileDownloaderWorker(
				user.getAddress(), user.getListeningPort(), fileName,
				config.getUsername(), this, index);
		fileDownloader.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progress")) {
					tm.updateStatus(index, (Integer) evt.getNewValue());
					if ((Integer) evt.getNewValue() == 100) // download complete
						sb.incFinished();
				}
			}
		});
		fileDownloader.execute();
	}

	@Override
	public void addUser(String name, ArrayList<String> files, String address,
			Integer port) {
		/*
		 * Boolean found = false; Enumeration<UserFiles> uf_enum =
		 * uf.elements(); while (uf_enum.hasMoreElements()) { UserFiles entry =
		 * uf_enum.nextElement(); if (entry.getName().equals(name)) { found =
		 * true; break; } } if (!found)
		 */
		uf.addElement(new UserFiles(name, files, port, address, this));
	}

	@Override
	public void updateUsersList(ArrayList<String> names,
			ArrayList<Integer> ports, ArrayList<String> addresses) {
		uf.clear();
		
		config.addOwnUserToGUI(ownFileNames);
		
		for (int i = 0; i < names.size(); ++i) {
			addUser(names.get(i), new ArrayList<String>(), addresses.get(i), ports.get(i));
		}
	}

	@Override
	public void updateUserFiles(String name, ArrayList<String> files) {
		Enumeration<UserFiles> uf_enum = uf.elements();
		while (uf_enum.hasMoreElements()) {
			UserFiles entry = uf_enum.nextElement();
			if (entry.getName().equals(name)) {
				entry.updateFiles(files);
				break;
			}
		}
	}

	@Override
	public void registerNetwork(Network network) {
		this.network = network;
		this.network.setIP(this.config.getAddress());
		this.network.setPort(this.config.getPort());
	}

	@Override
	public void registerConfig(Config config) {
		this.config = config;
		this.config.readConfigFile();
	}

	@Override
	public void updateUserInfo(String name, Integer port, String address) {
		Enumeration<UserFiles> uf_enum = uf.elements();
		while (uf_enum.hasMoreElements()) {
			UserFiles entry = uf_enum.nextElement();
			if (entry.getName().equals(name)) {
				entry.setAddress(address);
				entry.setListeningPort(port);
				break;
			}
		}
	}

	@Override
	public ProgressTableModel getTableModel() {
		return this.tm;
	}

	@Override
	public String getUserName() {
		return config.getUsername();
	}

	@Override
	public ArrayList<String> getUserFiles(String name) {
		Enumeration<UserFiles> uf_enum = uf.elements();
		while (uf_enum.hasMoreElements()) {
			UserFiles entry = uf_enum.nextElement();
			if (entry.getName().equals(name)) {
				return entry.getFiles();
			}
		}
		return null;
	}

	@Override
	public void displayMessage(String message) {
		sb.displayMessage(message);
	}

	@Override
	public void registerWebClient(WebServerClient s) {
		this.webServerClient = s;
	}

	@Override
	public void publishUser() {
		this.webServerClient.publishUser();
		config.addOwnUserToGUI(ownFileNames);
		considerUser(uf.get(0));
	}

	@Override
	public String getOwnFiles() {
		ArrayList<String> files = ownFileNames;
		String fileString = "";
		for (String file : files) {
			fileString += "file=" + file + "&";
		}
		return fileString;
	}

	@Override
	public void unpublishUser() {
		this.webServerClient.unpublishUser();
	}

	@Override
	public Integer getPort() {
		return config.getPort();
	}

	@Override
	public void updateUsers() {
		this.webServerClient.updateUsers();
	}

	@Override
	public String getAddress() {
		return config.getAddress();
	}

	@Override
	public void sendUpdateFiles() {
		this.webServerClient.sendUpdateFiles();
	}
}
