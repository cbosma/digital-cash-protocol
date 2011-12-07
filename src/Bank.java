import java.awt.Container;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

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

	private static Ecash[] moneyOrderArrayFromCustomer = null; 
	private static String[] uniqueness = new String[100]; 
	private static double moneyOrderAmount;
	private static boolean matchingAmounts = true;
	private static boolean matchingUniqueness = false;
	private static boolean tmpUniqueness = false;

	/**
	 * Properties object that holds all account information
	 */
	private static Properties accountProps = new Properties();

	private static JTextArea status = new JTextArea();

	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = 8465685567853888181L;

	/**
	 * Constructor
	 */
	public Bank() {
		super();
		status.append("Reading properties file...");
		this.readProperties();
		status.append("Initialized");
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
		FileInputStream in = null;
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
	public static void createAndShowGUI() {
		// Create and set up the window
		JFrame frame = new JFrame("Bank");
		frame.setTitle("Bank");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//		this.setMinimumSize(new Dimension(250, this.getPreferredSize().height));
		frame.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth() - frame.getSize().width) / 2), 200);

		Container contentPane = frame.getContentPane();
		frame.setContentPane(contentPane);
		SpringLayout layout = new SpringLayout();
		frame.setLayout(layout);

		JLabel title = new JLabel("The Bank");
		contentPane.add(title);

		status = new JTextArea(5, 20);
		JScrollPane scrollPane = new JScrollPane(status);
		scrollPane.setSize(500, 100);
		status.setEditable(false);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
		contentPane.add(scrollPane);

		// bind the top of the title to the top of the content pane
		layout.putConstraint(SpringLayout.NORTH, title, 0, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, title, 0, SpringLayout.WEST, contentPane);
		// bind the bottom of the scrollpane to the bottom of the content pane
		layout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.SOUTH, title);
		// bind the bottom right of the content pane to the bottom right of the status window
		layout.putConstraint(SpringLayout.EAST, contentPane, 0, SpringLayout.EAST, scrollPane);
		layout.putConstraint(SpringLayout.SOUTH, contentPane, 0, SpringLayout.SOUTH, scrollPane);

		// Display the window
		frame.pack();
		frame.setVisible(true);

		status.append("Initialized...");
	}

	public static void main(String[] args) {
		// Create and show the application
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Turn off bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
				setupSockets();

			}
		});
		//		SwingUtilities.invokeLater(new Runnable() {
		//			@Override
		//			public void run() {
		//				setupSockets();
		//			}
		//		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("Action!!!!");
	}

	private static void setupSockets() {
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		ServerSocket providerSocket = null;
		try {
			//1. creating a server socket
			providerSocket = new ServerSocket(2004, 10);
			//2. Wait for connection
			System.out.println("Waiting for connection from Customer...");
			status.append("Waiting...");
			Socket connection = providerSocket.accept();
			System.out.println("Connection received from Customer at " + connection.getInetAddress().getHostName());
			//3. Get Input Stream
			in = new ObjectInputStream(connection.getInputStream());
			//4. The two parts communicate via the input and output streams
			try{
				moneyOrderArrayFromCustomer = (Ecash[]) in.readObject();
				System.out.println("Received " + moneyOrderArrayFromCustomer.length + " money orders from the Customer");
				// Compare the amounts of n-1 money orders check the uniqueness string 
				if( compareMoneyOrders() == true ){
					System.out.println("Signed one Ecash Object to send back to Customer");
					status.append("\nSigned one Ecash Object to send back to Customer");

					// The bank signs one of the Ecash Objects
					if ( signMoneyOrder(moneyOrderArrayFromCustomer[moneyOrderArrayFromCustomer.length-1]) ){
						// Bank hands the blinded money order back to Customer 
						out = new ObjectOutputStream(connection.getOutputStream());
						out.flush();
						out.writeObject(moneyOrderArrayFromCustomer[(moneyOrderArrayFromCustomer.length-1)]);
						out.flush();
						System.out.println("Sent Money Order back to Customer");
						status.append("\nSent Money Order back to Customer");

						// Bank deducts the amount from their account
						accountProps.getProperty("customerBalance");
;
					}
				}
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

	public static Boolean signMoneyOrder(Ecash ecashToSign){
		// The Bank is satisfied that Customer did not make any attempts to cheat, so sign the one remaining money order 
		try {

			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			keyGen.initialize(1024);
			KeyPair keypair = keyGen.genKeyPair();
			PrivateKey privateKey = keypair.getPrivate();
			PublicKey publicKey = keypair.getPublic();

			Signature sig = Signature.getInstance(privateKey.getAlgorithm());
			SignedObject signedObject = new SignedObject(ecashToSign, privateKey, sig);
			sig = Signature.getInstance(publicKey.getAlgorithm());
			if ( signedObject.verify(publicKey, sig) ){
				return true;
			}
			else{
				return false;
			}
		} 

		catch (NoSuchAlgorithmException e) {
			System.err.println("Error Signing Ecash Object");
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			System.err.println("Error Signing Ecash Object");
			e.printStackTrace();
		} catch (SignatureException e) {
			System.err.println("Error Signing Ecash Object");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error Signing Ecash Object");
			e.printStackTrace();
		}
		return false;
	}

	public static Boolean compareMoneyOrders(){
		// The bank checks the amount of n-1 money orders
		// Open n-1 money orders and see that they all have the same amount
		for ( int i = 0; i < moneyOrderArrayFromCustomer.length -2; i++ ){

			// Save the first amount and uniqueness string to compare to the others
			if(i == 0){
				if ( moneyOrderArrayFromCustomer[i].getAmount() != null ){
					moneyOrderAmount= moneyOrderArrayFromCustomer[i].getAmount();					
				}
				if( moneyOrderArrayFromCustomer[i].getUniqueness() != null ){
					uniqueness[i] = moneyOrderArrayFromCustomer[i].getUniqueness();
				}
			}
			// Compare all other amounts and uniqueness strings  to the first
			else{
				// If there is a mismatch then set the matching boolean to false
				if( moneyOrderArrayFromCustomer[i].getAmount() != moneyOrderAmount){
					matchingAmounts = false;
				}
				// If any two uniqueness string match set the matching unuqieness to true
				if ( uniqueness != null){
					tmpUniqueness = false;
					for ( int j = 0; j < uniqueness.length; j++ ){
						if ( uniqueness[j] != null){
							if ( uniqueness[j].compareTo(moneyOrderArrayFromCustomer[i].getUniqueness()) == 0 ){
								matchingUniqueness = true;
								tmpUniqueness = true;
								System.err.println("The two Uniqueness Strings" + uniqueness[j].toString() + " are the same, you are cheating!");
							}	
						}
					}
					if ( tmpUniqueness == false){
						uniqueness[i] = moneyOrderArrayFromCustomer[i].getUniqueness();
					}
				}
			}
		}
		if ( matchingAmounts == true && matchingUniqueness == false ){
			System.out.println("All amounts matched and All Uniqueness String are different!");
			return true;
		}
		else{
			System.err.println("The amounts did not match or Two Uniqueness String are the same, you are cheating!");
			return false;
		}	
	}

}