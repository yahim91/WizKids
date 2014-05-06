package tests;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import app.Main;

public class TestGUI {

	static public void main(String[] argv) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final Main main = new Main(new MockupMediator("wizkid"));
				final ArrayList<Work> toBeProcessed = new ArrayList<Work>();
				toBeProcessed.add(new Work() {
					@Override
					public void execute() {
						try {
							System.out
									.println("Test: Adding 3 users with 5s delays!!!");
							main.getMediator().addUser(
									"gigix1",
									new ArrayList<String>(Arrays.asList("ana1",
											"are1", "mere2")), "localhost", 9999);
							Thread.sleep(5000);
							main.getMediator().addUser(
									"gigix2",
									new ArrayList<String>(Arrays.asList("ana2",
											"are2", "mere2")),  "localhost", 8888);
							Thread.sleep(5000);
							main.getMediator().addUser(
									"gigix3",
									new ArrayList<String>(Arrays.asList("ana3",
											"are3", "mere3")),  "localhost", 7777);
						} catch (InterruptedException e) {
						}
					}
				});

				toBeProcessed.add(new Work() {

					@Override
					public void execute() {
						System.out
								.println("Test: Updating files for user 2!!!");
						main.getMediator().updateUserFiles(
								"gigix2",
								new ArrayList<String>(Arrays.asList("ana3",
										"are3", "mere2", "si pere2")));
					}
				});

				new SwingWorker<Integer, Integer>() {

					@Override
					protected Integer doInBackground() throws Exception {
						for (Work work : toBeProcessed) {
							work.execute();
							Thread.sleep(5000);
						}
						return null;
					}
				}.execute();
			}
		});
	}
}
