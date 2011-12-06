import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

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
public class Bank extends JPanel implements ActionListener{

	private Ecash[] moneyOrderArrayFromCustomer = null; 

	private Properties accountProps = new Properties();
	
	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = 8465685567853888181L;

	/**
	 * Constructor
	 */
	public Bank() {
		super();
		this.readProperties();
	}
	
	/**
	 * Open the properties file, generate a default if it doesn't exist.
	 * The use of default settings is only for functionality proofing,
	 * real accounts would be stored in a database and encrypted.
	 * 
	 * For now, the properties file will consist of
	 * customerBalance = [some integer]
	 * merchantBalance = [some integer]
	 */
	private void readProperties() {
		FileInputStream in=null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream("accountProperties");
		} catch (FileNotFoundException e) {
			// No properties file, so making a default
			try {
				out = new FileOutputStream("accountProperties");
			} catch (FileNotFoundException e2) {
				// can't create a file
				e2.printStackTrace();
				System.exit(ERROR);
			}
			try {
				accountProps.setProperty("customerBalance", "100");
				accountProps.setProperty("merchantBalance", "100");
				accountProps.store(out, "Default setup");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e1) {
				// Error closing the file
				System.out.println("Error closing the properties file.");
				e1.printStackTrace();
			}
		}
		try {
			// utilize the in stream if it exists
			if ( in != null ){
				accountProps.load(in);
			}
		} catch (IOException e) {
			// Error loading the properties file, create defaults
			System.out.println("Error loading the properties");
			accountProps.setProperty("customerBalance", "100");
			accountProps.setProperty("merchantBalance", "100");
			try {
				accountProps.store(out, "Default setup");
			} catch (IOException e1) {
				// Error writing the account info back out
				System.out.println("Error storing properties.");
			}
		}
		try {
			// close the in stream if it exists
			if ( in != null ){
				in.close();
			}
		} catch (IOException e) {
			// Error closing the file
			System.out.println("Error closing the properties file.");
		}
		
	}
	
	/**
	 * Create the frame to hold the panel.
	 */
	public void createAndShowGUI() {
		// Create and set up the window
		JFrame frame = new JFrame("Bank");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setMinimumSize(new Dimension(250, frame.getPreferredSize().height));
		frame.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth() - frame.getSize().width) / 2), 200);

		Container contentPane = frame.getContentPane();
		SpringLayout layout = new SpringLayout();
		contentPane.setLayout(layout);
		
		JLabel title = new JLabel("The Bank");
		contentPane.add(title);
		layout.putConstraint(SpringLayout.NORTH, title, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, title, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, contentPane, 5, SpringLayout.EAST, title);
		layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, title);
		
		// Display the window
		frame.pack();
		frame.setVisible(true);
		
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		ServerSocket providerSocket = null;
		try {
			//1. creating a server socket
			providerSocket = new ServerSocket(2004, 10);
			//2. Wait for connection
			System.out.println("Waiting for connection from Customer...");
			Socket connection = providerSocket.accept();
			System.out.println("Connection received from Customer at " + connection.getInetAddress().getHostName());
			//3. get Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			//4. The two parts communicate via the input and output streams
				try{
					moneyOrderArrayFromCustomer = (Ecash[]) in.readObject();
					System.out.println("Received " + moneyOrderArrayFromCustomer.length + " money orders from the Customer");
				}
				catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
		}

		catch(IOException ioException){
			System.out.println("Error With Socket Connection");
			ioException.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}