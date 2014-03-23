package app;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.DefaultListModel;

import StatusBar.StatusBar;
import Worker.FileDownloaderWorker;
import table.ProgressTableModel;
import table.RowData;


public class Mediator {

	DefaultListModel<UserFiles> uf;
	DefaultListModel<String> files;
	ProgressTableModel tm;
	StatusBar sb;
	
	public Mediator() {
	}
	
	public void registerStatusBar(StatusBar sb) {
		this.sb = sb;
	}
	
	public void registerProgressTableModel(ProgressTableModel tm) {
		this.tm = tm;
	}

	public void registerUserListModel(DefaultListModel<UserFiles> uf) {
		this.uf = uf;
		
		// TODO: remove after implementing bind with server
		populateUsersAndFiles();
	}

	public void registerFilesModel(DefaultListModel<String> files) {
		this.files = files;
	}
	
	/* testing method: to populate local instance with users and associated files
	 * received from the server
	 */
	public void populateUsersAndFiles() {
		//sample: pupulating user files, e.g. received from server
  		String[] s = {"ana", "are", "mere"};
  		String[] s1 = {"ana1", "are1", "mere1"};
  		Vector<String> v = new Vector<String>(Arrays.asList(s));
  		Vector<String> v1 = new Vector<String>(Arrays.asList(s1));
  		uf.addElement(new UserFiles("gigel", v));
		uf.addElement(new UserFiles("gigel2", v1));
		uf.addElement(new UserFiles("gigel3", v));
	}
	
	public void considerUser(int index) {
		Vector<String> files_i = uf.get(index).getFiles();
		files.removeAllElements();
		
		for (String fl : files_i) 
			files.addElement(fl);
	}
	
	public void considerFile(int indexU, int indexF) {
		String fileName = files.get(indexF);
		String user = uf.get(indexU).getName();
		
		tm.addRow(new RowData(user, "_me", fileName));
		final int index = tm.getRowCount() - 1;
		FileDownloaderWorker bgTh = new FileDownloaderWorker();
		
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
	}
}
