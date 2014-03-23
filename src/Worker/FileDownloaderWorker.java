package Worker;

import java.util.List;

import javax.swing.SwingWorker;

public class FileDownloaderWorker extends SwingWorker<Integer, Integer> {

	public FileDownloaderWorker() {
		
		this.execute();
	}
	
	@Override
	protected Integer doInBackground() throws Exception {
		  //System.out.println("Crt th" + Thread.currentThread());
		  
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
	
	  protected void process(List<Integer> chunks) {
		  //System.out.println("Crt th" + Thread.currentThread());
		  publish(chunks.get(0));
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
