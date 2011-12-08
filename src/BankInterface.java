import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * ===== Requirements =====
 * - random choice of 1 out of N money orders sent by the
 *   customer to remain unopened
 * - an algorithm that certifies that all the N-1 money orders
 *   have been filled with valid information
 * - a procedure to certify that the orders received from
 *   merchants have not been used previously and storage of the 
 *   uniqueness string and identity strings of the orders in a
 *   database file
 * - Appropriate measures against reuse of the ecash
 */
public class BankInterface extends JFrame implements ActionListener, WindowListener{

	static JTextArea status = new JTextArea();
	private JButton startSockets;
	private BankServer server;
	
	static JLabel merchAccountNum;
	static JLabel merchantBalance;
	static JLabel accountNum;
	static JLabel customerBalance;
	
	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = 8465685567853888181L;
	
	/**
	 * Create the frame to hold the panel.
	 */
	public  BankInterface() {
		// Create and set up the window
		super("Bank");
		setTitle("Bank");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(this);

		setMinimumSize(new Dimension(500, getPreferredSize().height));
		setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth()) / 2), 200);

		Container contentPane = getContentPane();
		contentPane.setMinimumSize(new Dimension(400, getPreferredSize().height));
		setContentPane(contentPane);
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		JLabel title = new JLabel("The Bank");
		contentPane.add(title);
		
		startSockets = new JButton("Open the Bank");
		startSockets.setActionCommand("openBank");
		startSockets.addActionListener(this);
		contentPane.add(startSockets);

		JPanel accountPane = new JPanel();
		accountPane.setLayout(new BoxLayout(accountPane, BoxLayout.LINE_AXIS));
		accountPane.setBorder(BorderFactory.createTitledBorder("Account Information"));
		contentPane.add(accountPane);
		
		JPanel customerPane = new JPanel();
		customerPane.setLayout(new BoxLayout(customerPane, BoxLayout.PAGE_AXIS));
		customerPane.setBorder(BorderFactory.createTitledBorder("Customer Account"));
		accountNum = new JLabel();
		accountNum.setText("Account Number: --");
		customerBalance = new JLabel();
		customerBalance.setText("Account Balance: --");
		customerPane.add(accountNum);
		customerPane.add(customerBalance);
		accountPane.add(customerPane);
		
		JPanel merchantPane = new JPanel();
		merchantPane.setLayout(new BoxLayout(merchantPane, BoxLayout.PAGE_AXIS));
		merchantPane.setBorder(BorderFactory.createTitledBorder("Merchant Account"));
		merchAccountNum = new JLabel();
		merchAccountNum.setText("Account Number: --");
		merchantBalance = new JLabel();
		merchantBalance.setText("Account Balance: --");
		merchantPane.add(merchAccountNum);
		merchantPane.add(merchantBalance);
		accountPane.add(merchantPane);
		
		status = new JTextArea(5, 20);
		JScrollPane scrollPane = new JScrollPane(status);
		scrollPane.setSize(500, 100);
		status.setEditable(false);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
		contentPane.add(scrollPane);

		// bind the top of the title to the top of the content pane
		layout.putConstraint(SpringLayout.NORTH, title, 0, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, title, 0, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.WEST, startSockets, 5, SpringLayout.EAST, title);
		layout.putConstraint(SpringLayout.NORTH, accountPane, 10, SpringLayout.SOUTH, title);
		layout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.SOUTH, accountPane);
		layout.putConstraint(SpringLayout.EAST, contentPane, 0, SpringLayout.EAST, scrollPane);
		layout.putConstraint(SpringLayout.SOUTH, contentPane, 0, SpringLayout.SOUTH, scrollPane);

		// Display the window
		pack();
		setVisible(true);

		status.append("Initialized...");
	}

	public static void main(String[] args) {
		// Create and show the application
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Turn off bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);

				// Start Interface
				BankInterface window = new BankInterface();
				window.setVisible(true);

			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("Action Performed, " + e.getActionCommand().toString());
		if(e.getActionCommand().equals("openBank")) {
			status.append("\nOpening the bank");
			startSockets.setText("Close the Bank"); 
			startSockets.setActionCommand("closeBank");
			server = new BankServer();
			server.start();
			this.repaint();
		} else if (e.getActionCommand().equals("closeBank")) {
			status.append("Closing the bank");
			startSockets.setText("Close the Bank");
			System.exit(0);
		}
		
	}
	
	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Write out all files
		System.out.println("windows closing");
		try {
			FileOutputStream out = new FileOutputStream("accountProperties");
			BankServer.accountProps.store(out, "---No Comment---");
			out.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

}