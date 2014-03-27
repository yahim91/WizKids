package worker;

import java.util.List;

import javax.swing.SwingWorker;

public class FileDownloaderWorker extends SwingWorker<Integer, Integer> {

	public FileDownloaderWorker() {

	}
	
	@Override
	protected Integer doInBackground() throws Exception {
		  return 0;
	}
	
	  protected void process(List<Integer> chunks) {
	  }

	  @Override
	  protected void done() {
		/*System.out.println("Crt th" + Thread.currentThread());
		  
	    if (isCancelled())
	      System.out.println("Cancelled !");
	    else
	      System.out.println("Done !");*/
	  }
}
