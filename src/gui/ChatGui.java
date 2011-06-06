package gui;

import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import server.Listener;
import server.PeerSocket;
import tls.AlertException;
import tls.CipherSuite;
import tls.TLSEngine;

import common.Log;
import common.LogEvent;
import common.Tools;

import crypto.ICipher;
import crypto.ICompression;
import crypto.IKeyExchange;
import crypto.IHash;

/**
 * The graphical user interface
 * of the EduTLS application
 *
 * @author 	Eivind Vinje
 */
public class ChatGui extends JFrame implements tls.IApplication, Observer {

	private static final long serialVersionUID = -7578809751842248888L;
	public long SYSTEM_START;

	private JTextField txtChatSendMsg;
	private JTextField txtAddConnection;
	private DefaultListModel lstModelChat;
	private DefaultListModel lstModelLog;
	private DefaultListModel lstModelSessions;
	private TextArea txtLogInfo;
	//	private JList lstLog;
	private JList lstExistingSessions;	
	private JLabel lblSessionTimeout;
	private JButton btnSend;
	private JButton btnAddConnection;

	private TLSEngine engine;
//	private ArrayList<LogEvent> logevents;
	private JButton btnPerformance;
	private DefaultMutableTreeNode treeRootNode;
	private JTree lstLogTree;
	private DefaultTreeModel lstLogTreeModel;
	private JLabel lblActiveCipherSuite;
	
	/**
	 * The Constructor
	 */
	public ChatGui() { 
		super("EduTLS");
		SYSTEM_START = System.currentTimeMillis();
		setSize(800, 600);
		setResizable(false);
		setVisible(true);
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );  
		addWindowListener(new ClosingAdapter() );
		getContentPane().setLayout(null);
		Log.get().addObserver(this);
		initializeComponents();
	}

	/*
	 * Creates the gui components
	 */
	private void initializeComponents() {

		Border txtBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);

		JPanel pnlChatArea = new JPanel();
		pnlChatArea.setBounds(202, 374, 572, 177);
		pnlChatArea.setBorder(BorderFactory.createTitledBorder("Chat"));
		getContentPane().add(pnlChatArea);
		pnlChatArea.setLayout(null);

		JPanel pnlChatLogArea = new JPanel();
		pnlChatLogArea.setBounds(10, 21, 552, 114);
		pnlChatArea.add(pnlChatLogArea);

		lstModelChat = new DefaultListModel();
		JList lstChatLog = new JList(lstModelChat);
		lstChatLog.setBounds(0, 0, pnlChatLogArea.getWidth(), 50);
		lstChatLog.setPrototypeCellValue("Index 12345678901234567890123456789012345678901234567890123456789012345678901");
		lstChatLog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstChatLog.setBackground(getBackground());
		lstChatLog.setBorder(BorderFactory.createEmptyBorder());

		pnlChatLogArea.add(new JScrollPane(lstChatLog));

		txtChatSendMsg = new JTextField();
		txtChatSendMsg.setBorder(txtBorder);
		txtChatSendMsg.setBounds(10, 146, 453, 20);
		pnlChatArea.add(txtChatSendMsg);
		txtChatSendMsg.setColumns(10);

		btnSend = new JButton("Send");
		btnSend.setBounds(473, 146, 89, 23);
		btnSend.addActionListener(new ActionListenerImpl());
		pnlChatArea.add(btnSend);

		JPanel pnlLogArea = new JPanel();
		pnlLogArea.setBounds(202, 11, 572, 362);
		pnlLogArea.setBorder(BorderFactory.createTitledBorder("Log"));
		getContentPane().add(pnlLogArea);
		pnlLogArea.setLayout(null);

		JPanel pnlLogList = new JPanel();
		pnlLogList.setLayout(null);
		pnlLogList.setBounds(10, 21, 552, 149);
		pnlLogArea.add(pnlLogList);

		treeRootNode = new DefaultMutableTreeNode("Application startup");
		lstLogTree = new JTree(treeRootNode);
//		lstLogTree.setVisibleRowCount(8);
		lstLogTree.setRootVisible(true);
		
		lstLogTree.setBounds(0,0,pnlLogList.getWidth(), pnlLogList.getHeight()-5);
		lstLogTree.setSize(pnlLogList.getWidth(), pnlLogList.getHeight()-5);
		lstLogTree.addTreeSelectionListener(new TreeListAction());
		JScrollPane scrollPane = new JScrollPane(lstLogTree);
		lstLogTreeModel = (DefaultTreeModel) lstLogTree.getModel();
		scrollPane.setSize(pnlLogList.getWidth(), pnlLogList.getHeight());
		pnlLogList.add(scrollPane);

		JPanel pnlLogDetailedInfo = new JPanel();
		pnlLogDetailedInfo.setBounds(10, 172, 552, 179);
		pnlLogArea.add(pnlLogDetailedInfo);
		pnlLogDetailedInfo.setLayout(null);

		txtLogInfo = new TextArea("",5,50,TextArea.SCROLLBARS_VERTICAL_ONLY);
		txtLogInfo.setBackground(getBackground());
		txtLogInfo.setEditable(false);
		txtLogInfo.setBounds(10, 22, 532, 147);
		pnlLogDetailedInfo.add(txtLogInfo);


		JPanel pnlConnectionsArea = new JPanel();
		pnlConnectionsArea.setBorder(BorderFactory.createTitledBorder("Connections"));
		pnlConnectionsArea.setBounds(10, 11, 182, 309);
		getContentPane().add(pnlConnectionsArea);
		pnlConnectionsArea.setLayout(null);

		JLabel lblEnterHostname = new JLabel("Enter hostname:");
		lblEnterHostname.setBounds(10, 21, 162, 14);
		pnlConnectionsArea.add(lblEnterHostname);

		txtAddConnection = new JTextField();
		txtAddConnection.setBorder(txtBorder);
		txtAddConnection.setBounds(10, 46, 92, 20);
		pnlConnectionsArea.add(txtAddConnection);
		txtAddConnection.setColumns(10);

		btnAddConnection = new JButton("Add");
		btnAddConnection.setBounds(111, 45, 61, 23);
		btnAddConnection.addActionListener(new ActionListenerImpl());
		pnlConnectionsArea.add(btnAddConnection);

		JLabel lblExistingSessions = new JLabel("Existing sessions:");
		lblExistingSessions.setBounds(10, 90, 162, 14);
		pnlConnectionsArea.add(lblExistingSessions);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 77, 162, 2);
		pnlConnectionsArea.add(separator);

		JLabel lblChosenCipherSuite = new JLabel("Active cipher suite:");
		lblChosenCipherSuite.setBounds(10, 235, 162, 14);
		pnlConnectionsArea.add(lblChosenCipherSuite);
		lblChosenCipherSuite.setEnabled(false);

		lblActiveCipherSuite = new JLabel("");
		lblActiveCipherSuite.setEnabled(false);
		lblActiveCipherSuite.setBounds(10, 253, 162, 20);
		pnlConnectionsArea.add(lblActiveCipherSuite);

		lstModelSessions = new DefaultListModel();
		lstExistingSessions = new JList(lstModelSessions);
		lstExistingSessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//lstExistingSessions.setCellRenderer(new ListRenderer());
		lstExistingSessions.setBounds(10, 124, pnlConnectionsArea.getWidth()-20, 100);
		lstExistingSessions.setPrototypeCellValue("Index 12");
		lstExistingSessions.addMouseListener(new ListAction());
		pnlConnectionsArea.add((lstExistingSessions));

		JLabel lblSessionTimeoutInfo = new JLabel("Session timeout:");
		lblSessionTimeoutInfo.setBounds(10, 284, 110, 14);
		pnlConnectionsArea.add(lblSessionTimeoutInfo);

		lblSessionTimeout = new JLabel("");
		lblSessionTimeout.setBounds(130, 284, 46, 14);
		pnlConnectionsArea.add(lblSessionTimeout);

		JPanel pnlSettingsArea = new JPanel();
		pnlSettingsArea.setBorder(BorderFactory.createTitledBorder("Settings & Tools"));
		pnlSettingsArea.setBounds(10, 320, 182, 231);
		getContentPane().add(pnlSettingsArea);
		pnlSettingsArea.setLayout(null);

		JPanel pnlCipherSuites = new JPanel();
		pnlCipherSuites.setBounds(10, 20, 162, 166);
		pnlSettingsArea.add(pnlCipherSuites);
		pnlCipherSuites.setLayout(null);

		JLabel lblCipherSuites = new JLabel("Cipher suites:");

		lblCipherSuites.setBounds(0, 0, 142, 14);
		pnlCipherSuites.add(lblCipherSuites);
		btnPerformance = new JButton("Performance");
		btnPerformance.setBounds(10, 197, 162, 23);
		btnPerformance.addActionListener(new ActionListenerImpl());
		pnlSettingsArea.add(btnPerformance);

		int i = 17;
		for(CipherSuite s : TLSEngine.allCipherSuites) {
			JCheckBox cbxCipherSuite = new JCheckBox(s.getName());
			cbxCipherSuite.setToolTipText(s.getName());
			cbxCipherSuite.setBounds(0, i, 160, 20);
			cbxCipherSuite.setSelected(s.isEnabled());
			cbxCipherSuite.addActionListener(new ActionListenerImpl());
			pnlCipherSuites.add(cbxCipherSuite);
			i=i+20;
		}
		JLabel lblCompressionMethods = new JLabel("Compression methods:");
		lblCompressionMethods.setBounds(0, i, 160, 20);
		pnlCipherSuites.add(lblCompressionMethods);
		i=i+20;
		
		for(ICompression comp : ICompression.allCompressionMethods) {
			if(!comp.getName().equals("None")) {
				JCheckBox cbxCompression = new JCheckBox(comp.getName());
				cbxCompression.setSelected(comp.isEnabled());
				cbxCompression.addActionListener(new ActionListenerImpl());
				cbxCompression.setBounds(0, i, 160, 20);
				pnlCipherSuites.add(cbxCompression);
				i=i+20;
			}
		}
		addConnection("localhost",false);
		repaint();
	}


	private void testPerformance() {
		btnPerformance.setEnabled(false);
		LogEvent performanceTest = new LogEvent("Performance test","Tests performance of the various cryptographic components");
		Log.get().add(performanceTest);
		// TEST KEY EXCHANGE
		int numOfTests = 500;
		LogEvent logKeyExchange = new LogEvent("Performance test of key exchange algorithms","");
		performanceTest.addLogEvent(logKeyExchange);
		logKeyExchange.addDetails("Performing " + numOfTests + " tests of key exchange algorithms and one key pair generation");
		long testStart = System.currentTimeMillis();
		for(IKeyExchange ke : IKeyExchange.allKeyExchangeAlgorithms) {
			testStart = System.currentTimeMillis();
			ke.initKeys(512);
			logKeyExchange.addDetails("Generated 512 bits " + ke.getName() + " keys in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
			testStart = System.currentTimeMillis();
			for(int i = 0; i < numOfTests; i++) {
				ke.setYb(ke.getPublicKey());
			}
			logKeyExchange.addDetails("Algorithm: " + ke.getName() + " in " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
		}
		// TEST HASH FUNCTIONS
		LogEvent logMac = new LogEvent("Performance test of hash functions","");
		performanceTest.addLogEvent(logMac);
		numOfTests = 5000;
		logMac.addDetails("Performing " + numOfTests + " hash tests");		
		testStart = System.currentTimeMillis();
		for(IHash mac : IHash.allHashAlgorithms) {
			testStart = System.currentTimeMillis();
			for(int i = 0; i < numOfTests; i++) {
				mac.getHash(createBytes(16,i));
			}
			logMac.addDetails("Algorithm: " + mac.getName() + " (16 bytes) in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
			testStart = System.currentTimeMillis();
			for(int i = 0; i < numOfTests; i++) {
				mac.getHash(createBytes(512,i));
			}
			logMac.addDetails("Algorithm: " + mac.getName() + " (512 bytes) in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
			testStart = System.currentTimeMillis();
			for(int i = 0; i < numOfTests; i++) {
				mac.getHash(createBytes(16384,i));
			}
			logMac.addDetails("Algorithm: " + mac.getName() + " (16384 bytes) in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
		}
		// TEST HASH FUNCTIONS
		LogEvent logCompr = new LogEvent("Performance test of compression methods","");
		performanceTest.addLogEvent(logCompr);
		numOfTests = 1000;
		logCompr.addDetails("Performing " + numOfTests + " compression tests (compress and decompress)");		
		testStart = System.currentTimeMillis();
		byte[] tmpBytes = new byte[0];
		for(ICompression comp : ICompression.allCompressionMethods) {
			testStart = System.currentTimeMillis();
			for(int i = 0; i < numOfTests; i++) {
				tmpBytes = comp.compress(createBytes(16,i));
				comp.decompress(tmpBytes);
			}
			double ratio = (1-tmpBytes.length/(double)16)*100;
			logCompr.addDetails("Algorithm: " + comp.getName() + " (16 bytes, " + ratio + "% compression ratio) in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
			testStart = System.currentTimeMillis();
			for(int i = 0; i < numOfTests; i++) {
				tmpBytes = comp.compress(createBytes(512,i));
				comp.decompress(tmpBytes);
			}
			ratio = (1-tmpBytes.length/(double)512)*100;
			logCompr.addDetails("Algorithm: " + comp.getName() + " (512 bytes, " + ratio + "% compression ratio) in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
			testStart = System.currentTimeMillis();
			for(int i = 0; i < numOfTests; i++) {
				tmpBytes = comp.compress(createBytes(16384,i));
				comp.decompress(tmpBytes);
			}
			ratio = (1-tmpBytes.length/(double)16384)*100;
			logCompr.addDetails("Algorithm: " + comp.getName() + " (16384 bytes, " + ratio + "% compression ratio) in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
		}
		
		LogEvent logCipher = new LogEvent("Performance test of encryption algorithms","");
		performanceTest.addLogEvent(logCipher);
		numOfTests = 500;
		logCipher.addDetails("Performing " + numOfTests + " encryption and decryption tests");		
		byte[] key = new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		byte[] tmpByte=null, text, plain;
		byte[][] res=null;
		for(ICipher cipher : ICipher.allCipherAlgorithms) {
			int blocks=0, rest;
			testStart = System.currentTimeMillis();
			int bs = cipher.getBlockSize();
			for(int i = 0; i < numOfTests; i++) {
				cipher.init(true, key);
				tmpByte = createBytes(256,i);
				rest = (bs-(tmpByte.length%bs));
				text = new byte[tmpByte.length+rest];
				System.arraycopy(tmpByte, 0, text, 0, tmpByte.length);
				plain = new byte[text.length];
				blocks = (int)Math.ceil(text.length/bs);
				res = new byte[blocks][bs];
				byte[] tmp = new byte[bs];
				cipher.init(true, key);
				for(int j = 0; j < blocks; j++) {
					System.arraycopy(text, j*bs, tmp, 0, bs);
					cipher.cipher(tmp, 0, res[j], 0);
				}
				cipher.init(false, key);
				for(int j = 0; j < blocks; j++) {
					cipher.cipher(res[j], 0, plain, j*bs);
				}
			}
			logCipher.addDetails("Algorithm: " + cipher.getName() + " (256 bytes)  in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
			testStart = System.currentTimeMillis();
			for(int i = 0; i < numOfTests; i++) {
				tmpByte =  createBytes(2048,i);
				rest = (bs-(tmpByte.length%bs));
				text = new byte[tmpByte.length+rest];
				System.arraycopy(tmpByte, 0, text, 0, tmpByte.length);
				plain = new byte[text.length];
				blocks = (int)Math.ceil(text.length/bs);
				res = new byte[blocks][bs];
				byte[] tmp = new byte[bs];
				cipher.init(true, key);
				for(int j = 0; j < blocks; j++) {
					System.arraycopy(text, j*bs, tmp, 0, bs);
					cipher.cipher(tmp, 0, res[j], 0);
				}
				cipher.init(false, key);
				for(int j = 0; j < blocks; j++) {
					cipher.cipher(res[j], 0, plain, j*bs);
				}
			}
			logCipher.addDetails("Algorithm: " + cipher.getName() + " (2048 bytes)  in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms");

		}
		btnPerformance.setEnabled(true);
	}

	/*
	 * Displays a simple message box
	 * 
	 * @param message	String, the message to me displayed
	 * @returns	Nothing
	 */
	private void displayMessageBox(String message) {
		JOptionPane.showMessageDialog(this, message);
	}

	/*
	 * Adds a host to the session list
	 * 
	 * @param host	String, the host to add
	 * @returns	Nothing
	 */
	private void addConnection(String host) {
		addConnection(host, true);
	}
	
	private void addConnection(String host, boolean testHost) {
		if(!testHost) {
			lstModelSessions.add(0, host);
			lstExistingSessions.setSelectedIndex(0);
			return;
		}
		if(testConnection(host)) {
			lstModelSessions.add(0, host);
			lstExistingSessions.setSelectedIndex(0);
		}
		else
			displayMessageBox("Connection could not be established. Please check the host and try again.");
	}

	private String getCurrentConnection() {
		return lstExistingSessions.getSelectedValue().toString();
	}
	
	private void sendMessage(String message) throws AlertException {
		try {
			if(engine==null)
				engine = new TLSEngine(new PeerSocket(getCurrentConnection()), this);
			if(engine.getState().getPeerHost().equals(getCurrentConnection()))
				Tools.print("Currently connected to another host..");
			if(!engine.connect()) {
				displayMessageBox("Error when connecting to " + engine.getState().getPeerHost());
				return;
			}

		} catch (UnknownHostException e) {
			Tools.printerr("" + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Tools.printerr("" + e.getMessage());
			e.printStackTrace();
		} catch (AlertException e) {
			Tools.printerr("" + e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Tools.printerr("" + e.getMessage());
			e.printStackTrace();
		}

		engine.send(message.getBytes(TLSEngine.ENCODING));
		lstModelChat.addElement("[me]: " + message);
	}

	private boolean testConnection(String host) {
		return PeerSocket.testConnection(host);
	}

	private void connectTo(String host) {
		PeerSocket peer;
		try {
			peer = new server.PeerSocket(host);
			engine = new TLSEngine(peer, this);
			engine.connect();
		} catch (UnknownHostException e) {
			Tools.print("Error when connecting; " + e.getMessage());
		} catch (IOException e) {
			Tools.print("Error when connecting; " + e.getMessage());
		} catch (AlertException e) {
			Tools.print("Error when connecting; " + e.getMessage());
		} catch (InterruptedException e) {
			Tools.print("Error when connecting; " + e.getMessage());
		}
	}

	@Override
	public void getMessage(byte[] message) {
		lstModelChat.addElement("[" + getCurrentConnection() + "]: " + new String(message, TLSEngine.ENCODING));
	}

	@Override
	public void getStatus(STATUS type, String message, String details) {
		if(type==STATUS.SESSION_TIMEOUT)
			lblSessionTimeout.setText(message + " s");
		if(type==STATUS.INCOMING_CONNECTION) 
			addConnection(message, false);
		if(type==STATUS.ACTIVE_CIPHER_SUITE)
			lblActiveCipherSuite.setText(message);
			
	}

	public void addLog(LogEvent le) {
		addLog(le, treeRootNode);
	}
	public void addLog(LogEvent le, DefaultMutableTreeNode root) {
		lstLogTreeModel.insertNodeInto(new DefaultMutableTreeNode(le), root, root.getChildCount());
		lstLogTree.repaint();
	}
	
	private byte[] createBytes(int length, int seed) {
		byte[] b = new byte[length];
		b[0] = (byte)seed;
		return b;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg1 instanceof LogEvent) {
			LogEvent el = (LogEvent)arg1;
			addLog(el);
		}
		else
			Tools.print("WHAT?");
	}
	
	private class ActionListenerImpl implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// SEND BUTTON CLICKED
			if(arg0.getSource().equals(btnSend)) {
				if(!txtChatSendMsg.getText().equals("")) {
					try {
						sendMessage(txtChatSendMsg.getText());
					} catch (AlertException e) {
						displayMessageBox("Not connected");
					}
					txtChatSendMsg.setText("");
				}
			}
			// ADD CONNECTION BUTTON CLICKED
			else if(arg0.getSource().equals(btnAddConnection)) {
				if(!txtAddConnection.getText().equals("")) {
					addConnection(txtAddConnection.getText());
					txtAddConnection.setText("");
				}
			}
			// PERFORMANCE BUTTON CLICKED
			else if(arg0.getSource().equals(btnPerformance)) {
				testPerformance();
			}
			
			else if(arg0.getSource() instanceof JCheckBox) {
				JCheckBox changed = (JCheckBox)arg0.getSource();
				try {
					CipherSuite tmpSuite = TLSEngine.findCipherSuite(changed.getText());
					if(tmpSuite != null) {
						tmpSuite.setEnabled(changed.isSelected());
						System.out.println("cipher suite " + changed.getText() + " " + changed.isSelected());
					}
					else {
						for(ICompression comp : ICompression.allCompressionMethods) {
							if(comp.getName() == changed.getText()) {
								comp.setEnabled(changed.isSelected());
								System.out.println("cipher suite " + changed.getText() + " " + changed.isSelected());
							}
						}
					}
				} catch(Exception e) {
					Tools.printerr(e.getMessage());
				}
			}
			
		}
		
	}
	

	private class ListAction extends MouseAdapter {
		/*
		 * Used to control the JList onClick events
		 */
		public void mouseClicked(MouseEvent e) {
			if(e.getSource().equals(lstExistingSessions)) {
				int index = lstExistingSessions.locationToIndex(e.getPoint());
				if(index < 0)
					return;
				String host = (String)lstExistingSessions.getModel().getElementAt(index);
				if(testConnection(host))
					connectTo(host);
			}
		}
	}


	private class TreeListAction implements TreeSelectionListener  {
		DefaultMutableTreeNode node;
		@Override
		public void valueChanged(TreeSelectionEvent tree) {
			try {
				node = (DefaultMutableTreeNode) tree.getNewLeadSelectionPath().getLastPathComponent();
			} catch(Exception e) {
				// No node selected
				return; 
				}
			
			if(node.equals(treeRootNode))
				txtLogInfo.setText("Application has started. This graphical user interface uses " +
						"the TLSEngine to perform its tasks. The application listens for incoming " +
						"connections at port " + Listener.PORT);
			else {
				try {
					LogEvent log = (LogEvent)node.getUserObject();
					txtLogInfo.setText(log.getDetails());
					if(log.getSubLogEvents().size()>0) {
						// TODO open the node?
						for(LogEvent tmpLog : log.getSubLogEvents())
							addLog(tmpLog, node);
					}
				} catch(Exception e) {
					Tools.printerr(e.getMessage());
				}
			}
			
		}

	}

	private class ClosingAdapter extends WindowAdapter {  
		public void windowClosing( WindowEvent e ) {  
			int option = JOptionPane.showOptionDialog(  
					ChatGui.this,  
					"Are you sure you want to quit?",  
					"Exit Dialog", JOptionPane.YES_NO_OPTION,  
					JOptionPane.WARNING_MESSAGE, null, null,  
					null );  
			if( option == JOptionPane.YES_OPTION ) {
				
				Listener.close();
				System.exit( 0 );  
			}  
		} 
	}
}
