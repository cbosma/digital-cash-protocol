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
public class Bank implements ActionListener, WindowListener{

	private static Ecash[] moneyOrderArrayFromCustomer = null; 
	private static String[] uniqueness = new String[100]; 
	private static double moneyOrderAmount;
	private static boolean matchingAmounts = true;
	private static boolean matchingUniqueness = false;
	private static boolean tmpUniqueness = false;
	private static SignedObject signedObject;

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
	 * Open the properties file, generate a default if it doesn't exist.
	 * The use of default settings is only for functionality proofing,
	 * real accounts would be stored in a database and encrypted.
	 * 
	 * For now, the properties file will consist of
	 * customerBalance = [some integer]
	 * merchantBalance = [some integer]
	 */
	private static void readProperties() {
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
				System.exit(JPanel.ERROR);
			}
			try {
				accountProps.setProperty("customerBalance", "100");
				accountProps.setProperty("merchantBalance", "100");
				accountProps.setProperty("accountNum", "0123456789");
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
	public  void createAndShowGUI() {
		// Create and set up the window
		JFrame frame = new JFrame("Bank");
		frame.setTitle("Bank");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(this);

		//frame.setMinimumSize(new Dimension(, this.getPreferredSize().height));
		frame.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth() - frame.getSize().width) / 2), 200);

		Container contentPane = frame.getContentPane();
		contentPane.setMinimumSize(new Dimension(400, frame.getPreferredSize().height));
		frame.setContentPane(contentPane);
		SpringLayout layout = new SpringLayout();
		frame.setLayout(layout);

		JLabel title = new JLabel("The Bank");
		contentPane.add(title);

		TextField accountNum = new TextField();
		accountNum.setText("Account Number:" + accountProps.getProperty("accountNum"));
		accountNum.setEditable(false);
		
		TextField customerBalance = new TextField();
		customerBalance.setText("Account Balance:" + accountProps.getProperty("customerBalance"));
		customerBalance.setEditable(false);
		
		contentPane.add(accountNum);
		contentPane.add(customerBalance);
		
		status = new JTextArea(5, 20);
		JScrollPane scrollPane = new JScrollPane(status);
		scrollPane.setSize(500, 100);
		status.setEditable(false);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
		contentPane.add(scrollPane);

		// bind the top of the title to the top of the content pane
		layout.putConstraint(SpringLayout.NORTH, title, 0, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, title, 0, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, accountNum, 0, SpringLayout.SOUTH, title);
		layout.putConstraint(SpringLayout.NORTH, customerBalance, 0, SpringLayout.SOUTH, accountNum);
		// bind the bottom of the scrollpane to the bottom of the content pane
		layout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.SOUTH, customerBalance);
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
				Bank bank = new Bank();
				bank.readProperties();
				bank.createAndShowGUI();
				bank.setupSockets();

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
					// Bank deducts the amount from their account
					Double currBalance = Double.valueOf(accountProps.getProperty("customerBalance"));
					
					// Check to see the the Customer has enough money to complete the withdraw
					if ( currBalance - (moneyOrderArrayFromCustomer[moneyOrderArrayFromCustomer.length-1].getAmount()) >= 0 ){
						accountProps.setProperty("customerBalance", String.valueOf((currBalance - (moneyOrderArrayFromCustomer[moneyOrderArrayFromCustomer.length-1].getAmount()))));
						System.out.println(String.valueOf((currBalance - (moneyOrderArrayFromCustomer[moneyOrderArrayFromCustomer.length-1].getAmount()))));
						System.out.println("Money was removed from Customers Account");
						status.append("\nMoney was removed from Customers Account");

						// The bank signs one of the Ecash Objects
						if ( signMoneyOrder(moneyOrderArrayFromCustomer[moneyOrderArrayFromCustomer.length-1])){
							System.out.println("Signed one Ecash Object to send back to Customer");
							status.append("\nSigned one Ecash Object to send back to Customer");
							// Bank hands the blinded money order back to Customer 
							out = new ObjectOutputStream(connection.getOutputStream());
							out.flush();
							out.writeObject(signedObject);
							out.flush();
							System.out.println("Sent Signed Money Order back to Customer");
							status.append("\nSent Signed Money Order back to Customer");
						}
						else{
							System.out.println("Bank could not sign the Money Order");
							status.append("\nBank could not sign the Money Order");							
						}
					}
					else{
						System.out.println("Customer does not have sufficient funds");
						status.append("\nCustomer does not have sufficient funds");							
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
			try {
				writeToFile("publicKey.dat", publicKey);
			} catch (Exception e) {
				e.printStackTrace();
			}


			Signature sig = Signature.getInstance(privateKey.getAlgorithm());
			signedObject = new SignedObject(ecashToSign, privateKey, sig);
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
					System.err.println("The amounts do not match for " + moneyOrderArrayFromCustomer[i].getAmount() + " and " + moneyOrderAmount);
					status.append("\nThe amounts do not match for " + moneyOrderArrayFromCustomer[i].getAmount() + " and " + moneyOrderAmount);
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
								System.err.println("The two Uniqueness Strings " + uniqueness[j].toString() + " are the same, you are cheating!");
								status.append("\nThe two Uniqueness Strings " + uniqueness[j].toString() + " are the same, you are cheating!");
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
			status.append("\nAll amounts matched and All Uniqueness String are different!");
			return true;
		}
		else{
			System.err.println("The amounts did not match or Two Uniqueness String are the same, you are cheating!");
			status.append("\nThe amounts did not match or Two Uniqueness String are the same, you are cheating!");
			return false;
		}	
	}

    private static void writeToFile(String filename, Object object) throws Exception {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(new File(filename));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
	
	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Write out all files
		System.out.println("windows closing");
		try {
			FileOutputStream out = new FileOutputStream("accountPropertiess");
			accountProps.store(out, "---No Comment---");
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
	public void windowClosed(WindowEvent e) {
		System.out.println("windows closing2");
	}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

}