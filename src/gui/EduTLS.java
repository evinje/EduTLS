package gui;

import java.awt.Color;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

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
public class EduTLS extends JFrame implements tls.IApplication, Observer {
	
	private static final long serialVersionUID = -7578809751842248888L;
	public long SYSTEM_START;
		
	private JTextField txtChatSendMsg;
	private JTextField txtAddConnection;
	private DefaultListModel lstModelChat;
	private DefaultListModel lstModelLog;
	private DefaultListModel lstModelSessions;
	private TextArea txtLogInfo;
	private JList lstLog;
	private JList lstExistingSessions;	
	private JLabel lblSessionTimeout;
	private JButton btnSend;
	private JButton btnAddConnection;
	
	private TLSEngine engine;
	private ArrayList<LogEvent> logevents;
	private JButton btnPerformance;
	/**
	 * The Constructor
	 */
	public EduTLS() {
		super("EduTLS");
		SYSTEM_START = System.currentTimeMillis();
		setSize(800, 600);
		setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		logevents = new ArrayList<LogEvent>();
		Log.get().addObserver(this);
		initializeComponents();
		initializeActionListeners();
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
		pnlChatArea.add(btnSend);
		
		JPanel pnlLogArea = new JPanel();
		pnlLogArea.setBounds(202, 11, 572, 352);
		pnlLogArea.setBorder(BorderFactory.createTitledBorder("Log"));
		getContentPane().add(pnlLogArea);
		pnlLogArea.setLayout(null);
		
		JPanel pnlLogList = new JPanel();
		
		pnlLogList.setBounds(10, 21, 552, 149);
		pnlLogArea.add(pnlLogList);
		
		lstModelLog = new DefaultListModel();
		lstLog = new JList(lstModelLog);
		lstLog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstLog.setBounds(0, 0, pnlLogList.getWidth(), pnlLogList.getHeight()-5);
		lstLog.setPrototypeCellValue("Index 12345678901234567890123456789012345678901234567890123456789012345678901");
		lstLog.addMouseListener(new ListAction());
		pnlLogList.add(new JScrollPane(lstLog));
		
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
		pnlToolsArea.add(btnPerformance);
		
		int i = 20;
		for(CipherSuite s : TLSEngine.cipherSuites) {
			JCheckBox chckbxCipherSuite = new JCheckBox(s.getName());
			chckbxCipherSuite.setToolTipText(s.getName());
			chckbxCipherSuite.setBounds(0, i, 160, 20);
			chckbxCipherSuite.setSelected(true);
			
			pnlCipherSuites.add(chckbxCipherSuite);
			i=i+20;
		}
		
		test();
		repaint();
	}

	/*
	 * Adds action listeners (on click events)
	 * to the buttons
	 */
	private void initializeActionListeners() {
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!txtChatSendMsg.getText().equals("")) {
					try {
						sendMessage(txtChatSendMsg.getText());
					} catch (AlertException e) {
						displayMessageBox("Not connected");
					}
					txtChatSendMsg.setText("");
				}
			}
		});
		
		btnAddConnection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!txtAddConnection.getText().equals("")) {
					if(testConnection(txtAddConnection.getText()))
						addConnection(txtAddConnection.getText());
					txtAddConnection.setText("");
				}
			}
		});
		
		btnPerformance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnPerformance.setEnabled(false);
				LogEvent le = new LogEvent("Performance test","");
				Log.get().add(le);
				for(int i = 0; i < 100; i++) {
					le.addDetails("Starting test " + i);
				}
				btnPerformance.setEnabled(true);
			}
		});
	}
	
	private void test() {
//		for(int i = 0; i < 3; i++)
//			addLog("Test " + i,"Lots of fun " + i);
		lstModelSessions.add(0, "localhost");
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
		//modelSessions.addElement(host);
		if(testConnection(host))
			lstModelSessions.add(0, host);
		else
			displayMessageBox("Connection could not be established. Please check the host and try again.");
	}
	
	private void sendMessage(String message) throws AlertException {
		// TODO: test for active connection, else use localhost
		try {
			if(engine==null)
				engine = new TLSEngine(new PeerSocket("localhost"), this);
			
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
		lstModelChat.addElement("me: " + message);
	}
	
	private boolean testConnection(String host) {
		if(PeerSocket.testConnection(host))
			return true;
		return false;
	}
	
	private void connectTo(String host) {
		LogEvent lo = new LogEvent("Connecting to " + host, "");
		Log.get().add(lo);
		PeerSocket peer;
		try {
			peer = new server.PeerSocket(host);
			engine = new TLSEngine(peer, this);
			if(engine.connect())
				lo.addDetails("Successful");
			else
				lo.addDetails("Connection failed");
		} catch (UnknownHostException e) {
			lo.addDetails(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			lo.addDetails(e.getMessage());
			e.printStackTrace();
		} catch (AlertException e) {
			lo.addDetails(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			lo.addDetails(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public void getMessage(byte[] message) {
		lstModelChat.addElement("[nick]: " + new String(message, TLSEngine.ENCODING));
	}
	
	@Override
	public void getStatus(STATUS type, String message, String details) {
		if(type==STATUS.SESSION_TIMEOUT)
			lblSessionTimeout.setText(message + " s");
	}
	
	public void addLog(LogEvent le) {
		this.lstModelLog.addElement(le);
		logevents.add(le);
	}
	
	
	
	private class ListAction extends MouseAdapter {
		/*
		 * Used to control the JList onClick events
		 */
		public void mouseClicked(MouseEvent e) {
			if(e.getSource().equals(lstLog)) {
				int index = lstLog.locationToIndex(e.getPoint());
//				Object o = lstLog.getModel().getElementAt(index);
//				LogEvent le = (LogEvent)o;
				txtLogInfo.setText(logevents.get(index).getDetails());
			}
			else if(e.getSource().equals(lstExistingSessions)) {
				int index = lstExistingSessions.locationToIndex(e.getPoint());
				String host = (String)lstExistingSessions.getModel().getElementAt(index);
				if(testConnection(host))
					connectTo(host);
				else
					lstExistingSessions.getComponent(index).setBackground(Color.LIGHT_GRAY);
			}
		}
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
}
