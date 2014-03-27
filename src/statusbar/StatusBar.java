package statusbar;

import javax.swing.JLabel;

import app.IMediator;

public class StatusBar {
	
	int started;
	int finished;
	JLabel label;
	IMediator med;
	
	public StatusBar(JLabel label, IMediator med) {
		started = 0;
		finished = 0;
		this.label = label;
		
		this.med = med;
		med.registerStatusBar(this);
	}
	
	public void incStarted() {
		++started;
		label.setText("Currently downloading " + (started - finished) + "/" + started + " files..");
	}
	
	public void incFinished() {
		++finished;
		if (started == finished)
			label.setText(finished + "/" + finished + " downloaded. Download complete.");
		else
			label.setText("Currently downloading " + (started - finished) + "/" + started + " files..");
	}
}
