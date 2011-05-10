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
import java.security.InvalidKeyException;
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
//		logevents = new ArrayList<LogEvent>();
		Log.get().addObserver(this);
		initializeComponents();
//		initializeActionListeners();
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
		pnlLogArea.setBounds(202, 11, 572, 352);
		pnlLogArea.setBorder(BorderFactory.createTitledBorder("Log"));
		getContentPane().add(pnlLogArea);
		pnlLogArea.setLayout(null);

		JPanel pnlLogList = new JPanel();
		pnlLogList.setLayout(null);
		pnlLogList.setBounds(10, 21, 552, 149);
		pnlLogArea.add(pnlLogList);

		//		lstModelLog = new DefaultListModel();
		//		lstLog = new JList(lstModelLog);
		//		lstLog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//		lstLog.setBounds(0, 0, pnlLogList.getWidth(), pnlLogList.getHeight()-5);
		//		lstLog.setPrototypeCellValue("Index 12345678901234567890123456789012345678901234567890123456789012345678901");
		//		lstLog.addMouseListener(new ListAction());

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
		pnlLogDetailedInfo.setBounds(10, 172, 552, 169);
		pnlLogArea.add(pnlLogDetailedInfo);
		pnlLogDetailedInfo.setLayout(null);

		txtLogInfo = new TextArea("",5,50,TextArea.SCROLLBARS_VERTICAL_ONLY);
		txtLogInfo.setBackground(getBackground());
		txtLogInfo.setEditable(false);
		txtLogInfo.setBounds(10, 11, 532, 147);
		pnlLogDetailedInfo.add(txtLogInfo);


		JPanel pnlSettingsArea = new JPanel();
		pnlSettingsArea.setBorder(BorderFactory.createTitledBorder("Settings"));
		pnlSettingsArea.setBounds(10, 11, 182, 352);
		getContentPane().add(pnlSettingsArea);
		pnlSettingsArea.setLayout(null);

		JLabel lblEnterHostname = new JLabel("Enter hostname:");
		lblEnterHostname.setBounds(10, 21, 162, 14);
		pnlSettingsArea.add(lblEnterHostname);

		txtAddConnection = new JTextField();
		txtAddConnection.setBorder(txtBorder);
		txtAddConnection.setBounds(10, 46, 92, 20);
		pnlSettingsArea.add(txtAddConnection);
		txtAddConnection.setColumns(10);

		btnAddConnection = new JButton("Add");
		btnAddConnection.setBounds(111, 45, 61, 23);
		btnAddConnection.addActionListener(new ActionListenerImpl());
		pnlSettingsArea.add(btnAddConnection);

		JLabel lblExistingSessions = new JLabel("Existing sessions:");
		lblExistingSessions.setBounds(10, 90, 162, 14);
		pnlSettingsArea.add(lblExistingSessions);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 77, 162, 2);
		pnlSettingsArea.add(separator);

		JLabel lblChosenCipherSuite = new JLabel("Active cipher suite:");
		lblChosenCipherSuite.setBounds(10, 249, 162, 14);
		pnlSettingsArea.add(lblChosenCipherSuite);
		lblChosenCipherSuite.setEnabled(false);

		JLabel lblActiveCipherSuite = new JLabel("");
		lblActiveCipherSuite.setEnabled(false);
		lblActiveCipherSuite.setBounds(10, 274, 162, 67);
		pnlSettingsArea.add(lblActiveCipherSuite);

		//		JList lstSettings = new JList();
		//		lstSettings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//		lstSettings.setBounds(10, 103, pnlSettingsArea.getWidth(), pnlSettingsArea.getHeight());
		//		pnlSettingsArea.add(new JScrollPane(lstSettings));

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 237, 162, 2);
		pnlSettingsArea.add(separator_1);

		lstModelSessions = new DefaultListModel();
		lstExistingSessions = new JList(lstModelSessions);
		lstExistingSessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//lstExistingSessions.setCellRenderer(new ListRenderer());
		lstExistingSessions.setBounds(10, 124, pnlSettingsArea.getWidth()-20, 100);
		lstExistingSessions.setPrototypeCellValue("Index 12");
		lstExistingSessions.addMouseListener(new ListAction());
		pnlSettingsArea.add((lstExistingSessions));

		JLabel lblSessionTimeoutInfo = new JLabel("Session timeout:");
		lblSessionTimeoutInfo.setBounds(10, 327, 110, 14);
		pnlSettingsArea.add(lblSessionTimeoutInfo);

		lblSessionTimeout = new JLabel("");
		lblSessionTimeout.setBounds(130, 327, 46, 14);
		pnlSettingsArea.add(lblSessionTimeout);

		JPanel pnlToolsArea = new JPanel();
		pnlToolsArea.setBorder(BorderFactory.createTitledBorder("Tools"));
		pnlToolsArea.setBounds(10, 373, 182, 178);
		getContentPane().add(pnlToolsArea);
		pnlToolsArea.setLayout(null);

		JPanel pnlCipherSuites = new JPanel();
		pnlCipherSuites.setBounds(10, 20, 162, 113);
		pnlToolsArea.add(pnlCipherSuites);
		pnlCipherSuites.setLayout(null);

		JLabel lblCipherSuites = new JLabel("Cipher suites:");

		lblCipherSuites.setBounds(0, 0, 142, 14);
		pnlCipherSuites.add(lblCipherSuites);
		btnPerformance = new JButton("Performance");
		btnPerformance.setBounds(10, 144, 162, 23);
		btnPerformance.addActionListener(new ActionListenerImpl());
		pnlToolsArea.add(btnPerformance);

		int i = 20;
		for(CipherSuite s : TLSEngine.allCipherSuites) {
			JCheckBox chckbxCipherSuite = new JCheckBox(s.getName());
			chckbxCipherSuite.setToolTipText(s.getName());
			chckbxCipherSuite.setBounds(0, i, 160, 20);
			chckbxCipherSuite.setSelected(true);
			chckbxCipherSuite.addActionListener(new ActionListenerImpl());
			pnlCipherSuites.add(chckbxCipherSuite);
			i=i+20;
		}
		addConnection("localhost",false);
		test();
		repaint();
	}


	private void testPerformance() {
		btnPerformance.setEnabled(false);
		LogEvent performanceTest = new LogEvent("Performance test","Tests performance of the various cryptographic components");
		Log.get().add(performanceTest);
		// TEST KEY EXCHANGE
		int numOfTests = 50;
		LogEvent logKeyExchange = new LogEvent("Performance test of key exchange","");
		performanceTest.addLogEvent(logKeyExchange);
		crypto.keyexchange.DH diffie1;
		crypto.keyexchange.DH diffie2;
		long testStart = System.currentTimeMillis();
		diffie1 = new crypto.keyexchange.DH(512);
		diffie2 = new crypto.keyexchange.DH(512);
		logKeyExchange.addDetails("Two Diffie-Hellman certificates generated in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms (with pre-generated G and P)");
		testStart = System.currentTimeMillis();
		for(int i = 0; i < numOfTests; i++) {
			diffie1.setYb(diffie2.getPublicKey());
			diffie2.setYb(diffie1.getPublicKey());
			//le.addDetails("Diffie-Hellman key exchange " + i);
		}
		logKeyExchange.addDetails(numOfTests + " Diffie-Hellman tests in " + Math.abs(System.currentTimeMillis()-testStart) + " ms (two encryptions)");
		crypto.keyexchange.RSA rivest1;
		crypto.keyexchange.RSA rivest2;

		testStart = System.currentTimeMillis();
		rivest1 = new crypto.keyexchange.RSA(512);
		rivest2 = new crypto.keyexchange.RSA(512);
		logKeyExchange.addDetails("Two Rivest-Shamir-Adleman certificates generated in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms (with no pre-defined values, all primes generated with miller-rabin prime test)");
		for(int i = 0; i < numOfTests; i++) {
			rivest1.encrypt(rivest2.getPublicKey());
			rivest2.encrypt(rivest1.getPublicKey());
			//le.addDetails("Rivest-Shamir-Adleman key exchange " + i);
		}
		logKeyExchange.addDetails(numOfTests + " Rivest-Shamir-Adleman tests in " + Math.abs(System.currentTimeMillis()-testStart) + " ms (two encryptions)");

		// TEST HASH FUNCTIONS
		LogEvent logSha = new LogEvent("Performance test of hash functions","");
		performanceTest.addLogEvent(logSha);
		numOfTests = 500;
		crypto.mac.SHA1 sha1;
		String tmpString;
		testStart = System.currentTimeMillis();
		for(int i = 0; i < numOfTests; i++) {
			sha1 = new crypto.mac.SHA1();
			tmpString = "test number " + i;
			sha1.getMac(tmpString.getBytes());
		}
		logSha.addDetails(numOfTests + " SHA-1 tests in " + Math.abs(System.currentTimeMillis()-testStart) + " ms");
		crypto.mac.SHA256 sha256;
		testStart = System.currentTimeMillis();
		for(int i = 0; i < numOfTests; i++) {
			sha256 = new crypto.mac.SHA256();
			tmpString = "test number " + i;
			sha256.getMac(tmpString.getBytes());
		}
		logSha.addDetails(numOfTests + " SHA-256 tests in " + Math.abs(System.currentTimeMillis()-testStart) + " ms");

		LogEvent logCipher = new LogEvent("Performance test of conventional encryption","");
		performanceTest.addLogEvent(logCipher);
		numOfTests = 500;


		crypto.cipher.Rijndael aes = new crypto.cipher.Rijndael();
		byte[] key = new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		byte[] tmpByte=null, text, plain;
		byte[][] res=null;

		int blocks=0, rest;
		testStart = System.currentTimeMillis();
		for(int i = 0; i < numOfTests; i++) {
			aes.init(true, key);
			tmpString = "test number " + i;
			tmpByte = tmpString.getBytes();
			rest = (16-(tmpByte.length%16));
			text = new byte[tmpByte.length+rest];
			System.arraycopy(tmpByte, 0, text, 0, tmpByte.length);
			plain = new byte[text.length];
			blocks = (int)Math.ceil(text.length/16);
			res = new byte[blocks][16];
			byte[] tmp = new byte[16];
			for(int j = 0; j < blocks; j++) {
				System.arraycopy(text, j*16, tmp, 0, 16);
				aes.processBlock(tmp, 0, res[j], 0);
			}
			//			System.out.println("test " + i);
			aes.init(false, key);
			for(int j = 0; j < blocks; j++) {
				aes.processBlock(res[j], 0, plain, j*16);
			}
		}
		logCipher.addDetails(numOfTests + " Rijndael (implementation one) encryption and decryption in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms (" + tmpByte.length + " bytes)");

		testStart = System.currentTimeMillis();
		for(int i = 0; i < numOfTests; i++) {
			tmpString = "test number " + i + " aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccccccccccccccccccccccccccccccccccccccccdddddddddddddddddddddddddddddddddddddddddddddddddd";
			tmpByte = tmpString.getBytes();
			rest = (16-(tmpByte.length%16));
			text = new byte[tmpByte.length+rest];
			System.arraycopy(tmpByte, 0, text, 0, tmpByte.length);
			plain = new byte[text.length];
			blocks = (int)Math.ceil(text.length/16);
			res = new byte[blocks][16];
			byte[] tmp = new byte[16];
			for(int j = 0; j < blocks; j++) {
				System.arraycopy(text, j*16, tmp, 0, 16);
				aes.processBlock(tmp, 0, res[j], 0);
			}
			//			System.out.println("test " + i);
			aes.init(false, key);
			for(int j = 0; j < blocks; j++) {
				aes.processBlock(res[j], 0, plain, j*16);
			}
		}
		logCipher.addDetails(numOfTests + " Rijndael (implementation one) encryption and decryption in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms (" + tmpByte.length + " bytes)");

		crypto.cipher.Rijndael2 aes2 = new crypto.cipher.Rijndael2();

		Object aesKey=null;
		try {
			aesKey = aes2.makeKey(key, 16);
		} catch (InvalidKeyException e) {
			logCipher.addDetails("Error when creating key for Rijndael implementation number two");
			btnPerformance.setEnabled(true);
			return;
		}
		testStart = System.currentTimeMillis();
		for(int i = 0; i < numOfTests; i++) {
			tmpString = "test number " + i;
			tmpByte = tmpString.getBytes();
			rest = (16-(tmpByte.length%16));
			text = new byte[tmpByte.length+rest];
			System.arraycopy(tmpByte, 0, text, 0, tmpByte.length);
			plain = new byte[text.length];
			blocks = (int)Math.ceil(text.length/16);
			res = new byte[blocks][16];
			byte[] tmp = new byte[16];
			for(int j = 0; j < blocks; j++) {
				System.arraycopy(text, j*16, tmp, 0, 16);
				aes2.encrypt(tmp, 0, res[j], 0, aesKey, 16);
			}
			for(int j = 0; j < blocks; j++) {
				aes2.decrypt(res[j], 0, plain, j*16, aesKey, 16);
			}
		}
		logCipher.addDetails(numOfTests + " Rijndael (implementation two) encryption and decryption in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms (" + tmpByte.length + " bytes)");

		testStart = System.currentTimeMillis();
		for(int i = 0; i < numOfTests; i++) {
			tmpString = "test number " + i + " aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccccccccccccccccccccccccccccccccccccccccdddddddddddddddddddddddddddddddddddddddddddddddddd";
			tmpByte = tmpString.getBytes();
			rest = (16-(tmpByte.length%16));
			text = new byte[tmpByte.length+rest];
			System.arraycopy(tmpByte, 0, text, 0, tmpByte.length);
			plain = new byte[text.length];
			blocks = (int)Math.ceil(text.length/16);
			res = new byte[blocks][16];
			byte[] tmp = new byte[16];
			for(int j = 0; j < blocks; j++) {
				System.arraycopy(text, j*16, tmp, 0, 16);
				aes2.encrypt(tmp, 0, res[j], 0, aesKey, 16);
			}
			for(int j = 0; j < blocks; j++) {
				aes2.decrypt(res[j], 0, plain, j*16, aesKey, 16);
			}
		}
		logCipher.addDetails(numOfTests + " Rijndael (implementation two) encryption and decryption in: " + Math.abs(System.currentTimeMillis()-testStart) + " ms (" + tmpByte.length + " bytes)");
		btnPerformance.setEnabled(true);
	}

	private void test() {
		this.txtAddConnection.setText("192.168.10.105");
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
		if(type==STATUS.INCOMING_CONNECTION) {
			addConnection(message, false);
		}
			
	}

	public void addLog(LogEvent le) {
		addLog(le, treeRootNode);
	}
	public void addLog(LogEvent le, DefaultMutableTreeNode root) {
		lstLogTreeModel.insertNodeInto(new DefaultMutableTreeNode(le), root, root.getChildCount());
		lstLogTree.repaint();
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
					TLSEngine.findCipherSuite(changed.getText()).setEnabled(changed.isSelected());
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
