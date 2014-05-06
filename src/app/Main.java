package app;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import network.Network;
import network.WebServerClient;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import statusbar.StatusBar;
import table.ProgressCellRender;
import table.ProgressTableModel;

public class Main extends JFrame {

	private static final long serialVersionUID = -3681307336265782813L;

	private final JList<String> jfiles;
	private final JList<UserFiles> jusers;
	private final JTable jt;
	private final JLabel statusLabel;
	private final JButton refreshB;
	private static Network network = null;
	private static Config config;
	private static Logger logger;

	private static WebServerClient webServerClient;
	private final IMediator med;

	public Main(final IMediator med) {

		super("IDP Project");

		// init mediator
		this.med = med;

		// init frame
		// this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit
		// application
		// when window
		// is closed
		
		refreshB = new JButton();
		refreshB.setText("refresh users");
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int confirm = JOptionPane.showOptionDialog(null,
						"Are You Sure to Close Application?",
						"Exit Confirmation", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, null, null);

				if (confirm == 0) {
					med.unpublishUser();
					System.exit(0);
				}
			}
		});
		this.setVisible(true); // show it
		this.setSize(new Dimension(800, 600));
		this.setLocationRelativeTo(null); // center it
		this.setVisible(true);

		// init table
		ProgressTableModel ptm = new ProgressTableModel(this.med);
		jt = new JTable(ptm);
		jt.getColumn("Progress").setCellRenderer(new ProgressCellRender());
		jt.setShowGrid(false);
		jt.setShowHorizontalLines(false);
		jt.setShowVerticalLines(false);
		jt.setFillsViewportHeight(true);

		// init status bar
		statusLabel = new JLabel("Initialized ...", SwingConstants.LEFT);
		new StatusBar(statusLabel, this.med);

		// create jPanel containing the table and the status bar
		JPanel ts = new JPanel();
		LayoutManager layout = new BoxLayout(ts, BoxLayout.Y_AXIS);
		ts.setLayout(layout);
		ts.add(new JScrollPane(jt));
		//ts.add(new JScrollPane(statusLabel));
		ts.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				refreshB, statusLabel));

		// init user list
		DefaultListModel<UserFiles> uf = new DefaultListModel<UserFiles>();
		med.registerUserListModel(uf);

		//TODO :buton
		// 
		
		jusers = new JList<UserFiles>(uf);
		jusers.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if (e.getValueIsAdjusting()) {
					med.considerUser(jusers.getSelectedValue());
				}
			}
		});

		refreshB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				med.updateUsers();
			}
		});
		
		// init files list, for current selected user
		DefaultListModel<String> files = new DefaultListModel<String>();
		med.registerFilesModel(files);
		jfiles = new JList<String>(files);
		jfiles.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					int indexF = jfiles.getSelectedIndex();
					int indexU = jusers.getSelectedIndex();
					if (indexU < 0) {
						return;
					}
					med.considerFile(indexU, indexF);
				}
			}
		});

		// create a Vertical Split panel between files list and table + status
		// bar
		JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				new JScrollPane(jfiles), ts);
		splitPane1.setOneTouchExpandable(true);
		splitPane1.setResizeWeight(0.7);

		// create horizontal split between above pane and the user list
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				splitPane1, new JScrollPane(jusers));
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);

		// add everything to frame
		this.getContentPane().add(splitPane);

		med.publishUser();
		med.updateUsers();
	}

	public IMediator getMediator() {
		return med;
	}

	static public void main(String[] argv) {
		final String username = argv[0];
		final IMediator mediator = new Mediator(username);
		network = new Network("users_folder/" + username, mediator);
		webServerClient = new WebServerClient(mediator);
		config = new Config(username, mediator);
		mediator.registerConfig(config);
		mediator.registerNetwork(network);
		mediator.registerWebClient(webServerClient);

		logger = Logger.getLogger(Main.class);
		PropertyConfigurator.configure(config.getLogFileName());

		logger.info("hello");

		// run on EDT (event-dispatching thread), not on main thread!
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Main(mediator);
			}
		});

		network.startListening();
	}
}
