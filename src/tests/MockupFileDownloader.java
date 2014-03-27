package tests;

import javax.swing.SwingWorker;

public class MockupFileDownloader extends SwingWorker<Integer, Integer> {

	@Override
	protected Integer doInBackground() throws Exception {
		int DELAY = 1000;
		  int count = 10;
		  int i     = 0;
		  try {
			  while (i < count) {
				  i++;
				  Thread.sleep(DELAY);
				  this.setProgress(i * 10);	// update task percent (not task bar,
				  							// the actual computational task)
			  }
		  } catch (InterruptedException e) {
			  e.printStackTrace();
		  }
		return 0;
	}

}
