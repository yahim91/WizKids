package table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RowData {
//"sursa", "destinatia", "numele fisierului", "progresul", "starea"};
	private String source;
	private String dest;
	private String fileName;
	private int progress;
	private String status;
	
	public RowData(String source, String dest, String fileName) {
		super();
		this.source = source;
		this.dest = dest;
		this.fileName = fileName;
		status = "Receiving...";
		progress = 0;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getSource() {
		return source;
	}

	public String getDest() {
		return dest;
	}

	public String getFileName() {
		return fileName;
	}

	public int getProgress() {
		return progress;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
