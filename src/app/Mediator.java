package app;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.DefaultListModel;

import statusbar.StatusBar;
import table.ProgressTableModel;


public class Mediator implements IMediator {

	DefaultListModel<UserFiles> uf;
	DefaultListModel<String> files;
	ProgressTableModel tm;
	StatusBar sb;
	Config config;
	
	public Mediator() {
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

	public void registerUserListModel(DefaultListModel<UserFiles> uf) {
		this.uf = uf;
	}

	public void registerFilesModel(DefaultListModel<String> files) {
		this.files = files;
	}
	
	
	public void considerUser(UserFiles uf) {
		ArrayList<String> files_i = uf.getFiles();
		files.removeAllElements();
		
		for (String fl : files_i) 
			files.addElement(fl);
	}
	
	public void considerFile(int indexU, int indexF) {
		
	}

	@Override
	public void addUser(String name, ArrayList<String> files) {
		uf.addElement(new UserFiles(name, files, this));
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
