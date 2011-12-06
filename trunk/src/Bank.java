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
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
public class Bank extends JFrame implements ActionListener{

	private Ecash[] moneyOrderArrayFromCustomer = null; 
	private String[] uniqueness = new String[100]; 
	private double moneyOrderAmount;
	private boolean matchingAmounts = true;
	private boolean matchingUniqueness = false;

	/**
	 * Properties object that holds all account information
	 */
	private Properties accountProps = new Properties();

	private JTextArea status = new JTextArea();

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
		this.setTitle("Bank");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//		this.setMinimumSize(new Dimension(250, this.getPreferredSize().height));
		this.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth() - this.getSize().width) / 2), 200);

		Container contentPane = this.getContentPane();
		this.setContentPane(contentPane);
		SpringLayout layout = new SpringLayout();
		this.setLayout(layout);

		JLabel title = new JLabel("The Bank");
		contentPane.add(title);

		this.status = new JTextArea(5, 20);
		JScrollPane scrollPane = new JScrollPane(this.status); 
		this.status.setEditable(false);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
		contentPane.add(scrollPane);

		System.out.println("setting layouts");

		//		// bind the top of the title to the top of the content pane
		layout.putConstraint(SpringLayout.NORTH, title, 0, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, title, 0, SpringLayout.WEST, contentPane);
		// bind the bottom of the scrollpane to the bottom of the content pane
		//		layout.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, contentPane);
		//		layout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, contentPane);
		// bind the bottom right of the content pane to the bottom right of the status window
		layout.putConstraint(SpringLayout.EAST, contentPane, 0, SpringLayout.EAST, scrollPane);
		layout.putConstraint(SpringLayout.SOUTH, contentPane, 0, SpringLayout.SOUTH, scrollPane);

		System.out.println("done with layouts");

		// Display the window
		this.pack();
		this.setVisible(true);

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
			//3. get Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			//4. The two parts communicate via the input and output streams
			try{
				moneyOrderArrayFromCustomer = (Ecash[]) in.readObject();
				System.out.println("Received " + moneyOrderArrayFromCustomer.length + " money orders from the Customer");
				// Compare the amounts of n-1 money orders check the uniqueness string 
				compareMoneyOrders();				
			}
			catch(ClassNotFoundException classnot){
				System.err.println("Data received in unknown format");
			}
		}

		catch(IOException ioException){
			System.out.println("Error With Socket Connection");
			ioException.printStackTrace();
		}

		this.repaint();
		this.validate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Action!!!!");
	}

	public void compareMoneyOrders(){
		// The bank checks the amount of n-1 money orders
		// Open n-1 money orders and see that they all have the same amount
		for ( int i = 0; i < moneyOrderArrayFromCustomer.length -1; i++ ){

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
					for ( int j = 0; j < uniqueness.length; j++ ){
						if ( uniqueness[j] != null){
							if ( uniqueness[j].compareTo(moneyOrderArrayFromCustomer[i].getUniqueness()) == 0 ){
								matchingUniqueness = true;
							}							
						}

					}					
				}

			}
		}
		if ( matchingAmounts == true ){
			System.out.println("All amounts matched!");
		}
		else{
			System.err.println("The amounts did not match, you are cheating!");
		}	
		if ( matchingUniqueness == true){
			System.err.println("Two Uniqueness String are the same, you are cheating!");			
		}
		else{
			System.out.println("All Uniqueness String are different!");			
		}
	}

}