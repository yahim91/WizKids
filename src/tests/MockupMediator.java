package tests;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListModel;

import statusbar.StatusBar;
import table.ProgressTableModel;
import table.RowData;
import worker.FileDownloaderWorker;
import app.Config;
import app.IMediator;
import app.UserFiles;

public class MockupMediator implements IMediator {

	private DefaultListModel<UserFiles> uf;
	private DefaultListModel<String> files;
	private ProgressTableModel tm;
	private StatusBar sb;
	private static int userid;
	private Config config;
	
	public MockupMediator() {
		config = new Config();
		config.readConfigFile();
	}
	
	@Override
	public void registerStatusBar(StatusBar sb) {
		this.sb = sb;
	}
	
	@Override
	public void registerProgressTableModel(ProgressTableModel tm) {
		this.tm = tm;
	}

	@Override
	public void registerUserListModel(DefaultListModel<UserFiles> uf) {
		this.uf = uf;
	}

	@Override
	public void registerFilesModel(DefaultListModel<String> files) {
		this.files = files;
	}
	
	@Override
	public void considerUser(UserFiles uf) {
		ArrayList<String> files_i = uf.getFiles();
		files.removeAllElements();
		
		for (String fl : files_i) 
			files.addElement(fl);
	}
	
	@Override
	public void considerFile(int indexU, int indexF) {
		String fileName = files.get(indexF);
		String user = uf.get(indexU).getName();
		
		tm.addRow(new RowData(user, "_me", fileName));
		final int index = tm.getRowCount() - 1;
		MockupFileDownloader bgTh = new MockupFileDownloader();
		
		sb.incStarted();
		
		bgTh.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				
				if (evt.getPropertyName().equals("progress")) {
					tm.updateStatus(index, (Integer)evt.getNewValue());
				
					if ((Integer)evt.getNewValue() == 100) // download complete
						sb.incFinished();
				}
			}
		});
		bgTh.execute();
	}

	@Override
	public void addUser(String name, ArrayList<String> files) {
		UserFiles new_entry = new UserFiles(name, files, this);
		new_entry.setId(userid++);
		uf.addElement(new_entry);
	}
	
	@Override
	public void updateUserFiles(Integer id, ArrayList<String> files) {
		Enumeration<UserFiles> uf_enum = uf.elements();
		while(uf_enum.hasMoreElements()) {
			UserFiles entry = uf_enum.nextElement();
			if (entry.getId() == id) {
				entry.updateFiles(files);
				break;
			}
		}
	}

	@Override
	public Config getConfig() {
		return config;
	}
	
	
}