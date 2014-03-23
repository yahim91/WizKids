import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import table.ProgressCellRender;

public class Main extends JFrame implements ActionListener {

	private final Mediator med;
	
	public Main() {
		
		super("IDP Project");

		med = new Mediator();
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit application when window is closed
		this.setVisible(true); // show it
		this.setSize(new Dimension(800, 600));
		this.setLocationRelativeTo(null); //center it
		this.setVisible(true);
		
		JTable jt = new JTable(med.getTableModel());
		jt.getColumn("Progress").setCellRenderer(new ProgressCellRender());
		jt.setShowGrid(false);
		jt.setShowHorizontalLines(false);
        jt.setShowVerticalLines(false);
        jt.setFillsViewportHeight(true);
		
		final JList<String> jfiles = new JList<String>(med.getFilesModel());
		final JList<UserFiles> jusers = new JList<UserFiles>(med.getUserListModel());
		
		jusers.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				int index = ((JList<UserFiles>)e.getSource()).getSelectedIndex();
				med.considerUser(index);
			}
		});
		
		jfiles.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if (e.getValueIsAdjusting()){
					int index = ((JList<UserFiles>)e.getSource()).getSelectedIndex();
					int indexU = jusers.getSelectedIndex();
					med.considerFile(indexU, index);
				}
			}
		});
		
		JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(jfiles), new JScrollPane(jt));
		splitPane1.setOneTouchExpandable(true);
		splitPane1.setResizeWeight(0.5);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane1, new JScrollPane(jusers));
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);
		
		this.getContentPane().add(splitPane);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		// ((Command)e.getSource()).execute();	
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
