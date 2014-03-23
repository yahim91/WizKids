package table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.SwingWorker;

public class FileDownloaderWorker extends SwingWorker<Integer, Integer> {

	public FileDownloaderWorker() {
		
		this.execute();
	}
	
	@Override
	protected Integer doInBackground() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Crt th" + Thread.currentThread());
		  
		  // TODO 3.2
		  int DELAY = 1000;
		  int count = 10;
		  int i     = 0;
		  try {
			  while (i < count) {
				  i++;
				  Thread.sleep(DELAY);
					
				  publish(i);
					
				  this.setProgress(i);	// update task percent (not task bar,
				  						// the actual computational task)
			  }
		  } catch (InterruptedException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		return 0;
	}
	
	  protected void process(List<Integer> chunks) {
		  
		  System.out.println("Crt th" + Thread.currentThread());
		  
	   // TODO 3.3 - print values received
		  System.out.println("Begin iteration");
		  for(Integer i : chunks) { 
			  System.out.println(i);
		  }
		  System.out.println("End iteration");
		  System.out.println(chunks);
	  }

	  @Override
	  protected void done() {
		
		  System.out.println("Crt th" + Thread.currentThread());
		  
	    if (isCancelled())
	      System.out.println("Cancelled !");
	    else
	      System.out.println("Done !");
	  }
}
