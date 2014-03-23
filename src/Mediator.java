import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JProgressBar;
import javax.swing.table.DefaultTableModel;

import table.FileDownloaderWorker;
import table.ProgressTableModel;
import table.RowData;


public class Mediator {

	DefaultListModel<UserFiles> uf;
	DefaultListModel<String> files;
	final ProgressTableModel tm;
	
	public Mediator() {
		
		//sample: pupulating user files, e.g. received from server
  		String[] s = {"ana", "are", "mere"};
  		String[] s1 = {"ana1", "are1", "mere1"};
  		Vector<String> v = new Vector<String>(Arrays.asList(s));
  		Vector<String> v1 = new Vector<String>(Arrays.asList(s1));
  		uf = new DefaultListModel<UserFiles>();
		uf.addElement(new UserFiles("gigel", v));
		uf.addElement(new UserFiles("gigel2", v1));
		uf.addElement(new UserFiles("gigel3", v));
		
		files = new DefaultListModel<String>();
		
		/*String[] cn = {"sursa", "destinatia", "numele fisierului", "progresul", "starea"};
		tm = new DefaultTableModel(cn, 0);
		tm.addRow(new Object[] {"_me_", "_you_", "file_name", new JProgressBar(0, 10), "Transfering.."});*/
		tm = new ProgressTableModel();
		/*tm.addRow(new RowData("_me_", "_you_", "file_name"));
		tm.addRow(new RowData("_me_", "_you_", "file_name"));
		tm.addRow(new RowData("_me_", "_you_", "file_name"));*/
	}
	
	public ProgressTableModel getTableModel() {
		return tm;
	}

	public DefaultListModel<UserFiles> getUserListModel() {
		return uf;
	}

	public DefaultListModel<String> getFilesModel() {
		return files;
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
		//TODO 3.5  - add property change listener to export task
		bgTh.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO Auto-generated method stub
				if (evt.getPropertyName().equals("progress")) {
					tm.updateStatus(index, (Integer)evt.getNewValue() * 10);
				}
			}
		});
	}
}
