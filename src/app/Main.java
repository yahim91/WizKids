package app;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import StatusBar.StatusBar;
import table.ProgressCellRender;
import table.ProgressTableModel;

public class Main extends JFrame {
	
	private static final long serialVersionUID = -3681307336265782813L;
	
	private final JList<String> jfiles;
	private final JList<UserFiles> jusers;
	private final JTable jt;
	private final JLabel statusLabel;
	
	private final Mediator med;
	
	public Main() {
		
		super("IDP Project");

		// init mediator
		med = new Mediator();
		
		// init frame
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit application when window is closed
		this.setVisible(true); // show it
		this.setSize(new Dimension(800, 600));
		this.setLocationRelativeTo(null); //center it
		this.setVisible(true);
		
		// init table
		ProgressTableModel ptm = new ProgressTableModel(med);
		jt = new JTable(ptm);
		jt.getColumn("Progress").setCellRenderer(new ProgressCellRender());
		jt.setShowGrid(false);
		jt.setShowHorizontalLines(false);
        jt.setShowVerticalLines(false);
        jt.setFillsViewportHeight(true);
		
        // init status bar
        statusLabel = new JLabel("Initialized ...", SwingConstants.LEFT);
        new StatusBar(statusLabel, med);
        
        // create jPanel containing the table and the status bar
	    JPanel ts = new JPanel();
	    LayoutManager layout = new BoxLayout(ts, BoxLayout.Y_AXIS);
	    ts.setLayout(layout);
	    ts.add(new JScrollPane(jt));
	    ts.add(new JScrollPane(statusLabel));
        
	    // init user list
	    DefaultListModel<UserFiles> uf = new DefaultListModel<UserFiles>();
		med.registerUserListModel(uf);
		
	    jusers = new JList<UserFiles>(uf);
	    jusers.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if (e.getValueIsAdjusting()){
					int index = jusers.getSelectedIndex();
					med.considerUser(index);
				}
			}
		});
	    
	    // init files list, for current selected user
	    DefaultListModel<String> files = new DefaultListModel<String>();
	    med.registerFilesModel(files);
	    jfiles = new JList<String>(files);
		jfiles.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if (e.getValueIsAdjusting()){
					int indexF = jfiles.getSelectedIndex();
					int indexU = jusers.getSelectedIndex();
					med.considerFile(indexU, indexF);
				}
			}
		});
		
		// create a Vertical Split panel between files list and table + status bar
		JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(jfiles), ts);
		splitPane1.setOneTouchExpandable(true);
		splitPane1.setResizeWeight(0.7);
		
		// create horizontal split between above pane and the user list
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane1, new JScrollPane(jusers));
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);
		
		// add everything to frame
		this.getContentPane().add(splitPane);
	}

	static public void main(String[] argv) {
		// run on EDT (event-dispatching thread), not on main thread!
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Main();
			}
		});
	}
}
