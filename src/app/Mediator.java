package app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;

import network.Network;
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

	public Mediator(String username) {
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
		config.getFiles();
	}

	public void considerUser(UserFiles uf) {
		ArrayList<String> files_i = uf.getFiles();
		files.removeAllElements();

		for (String fl : files_i)
			files.addElement(fl);
	}

	public void considerFile(int indexU, int indexF) {
		String fileName = files.get(indexF);
		UserFiles user = uf.get(indexU);
		sb.incStarted();

		tm.addRow(new RowData(user.getName(), config.getUsername(), fileName));
		final int index = tm.getRowCount() - 1;
		FileDownloaderWorker fileDownloader = new FileDownloaderWorker(
				user.getAddress(), user.getListeningPort(), fileName, config.getUsername());
		fileDownloader.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progress")) {
					tm.updateStatus(index, (Integer)evt.getNewValue());
					if ((Integer)evt.getNewValue() == 100) // download complete
						sb.incFinished();
				}
			}
		});
		fileDownloader.execute();
	}

	
	@Override
	public void addUser(String name, ArrayList<String> files) {
		uf.addElement(new UserFiles(name, files, this));
		considerUser(uf.get(0));
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
}
