import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SignedObject;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * ===== Requirements =====
 * - generates N orders for each money order the customer wants to 
 *   make and assigns a different random uniqueness string number for 
 *   each of the N ecash money orders
 * - implements the secret splitting and bit commitment protocols used
 *   to generate the identity strings that describe the customer's name,
 *   address and any other piece of identifying information that the
 *   bank wants to see.
 * - implements a blind signature protocol for all N money orders
 * - automatically complies to reveal the half of the identity string
 *   chosen by the merchant
 */
public class Customer extends JPanel implements ActionListener{

	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = 8465685567853888181L;
	private JFrame frame = new JFrame("Customer");
	private JButton badAmountTest = null;
	private JButton badUniqunessTest = null;
	private JButton submit = null;
	private TextField amount = null;
	private JButton sendToMerchant = null;
	private JButton sendBadUniquenessToMerchant = null;
	private TextField error = null;
	private JTextArea status = new JTextArea();
	private static SignedObject signedObject;
	public static Boolean BadUniquenessToMerchant = false;

	private Double transactionAmount = null;
	private static int numMoneyOrders = 100;
	private Ecash[] moneyOrderArray = null;
	private static String[][] LandRArray;
	private static int Pflag = 0;
	private static String P = "";

	// Constant Identity Information
	private static final String name = "Alice";
	private static final String address = "8000 York Road Towson Maryland 21252";
	private static final String phone = "410-704-2000";

	public static String generateKey(String M) {
		String key = "";

		// Generate key, a random string of 1's and 0's with the same length as the the message, M
		for (int i = 0; i < M.length(); i++) {
			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt(2);
			key += randomInt;
		}

		return key;
	}

	public static String commitment(String bit) {
		String b = bit;

		if (Pflag == 0) {
			// Generate P, a random string of 1's and 0's with the same length as the bit to be committed
			for (int i = 0; i < b.length(); i++) {
				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(2);
				P += randomInt;
			}

			Pflag = 1;
		}

		//		System.out.println("\n\n\nPrinting P: " + P.toString() + "\n\n\n");
		//		System.out.println("\n\n\nPrinting b: " + b.toString() + "\n\n\n");
		// Put P & b in a hash function to generate h
		return (P.hashCode() ^ b.hashCode()) + "";
	}

	public static void splitIdentity() {
		for (int i = 0; i < numMoneyOrders; i++) {
			// Convert the message to 1's and 0's
			String M = name + " " + address + " " + phone;
			M = new BigInteger(M.getBytes()).toString(2);

			// Generate the key, L
			String L = generateKey(M);

			// XOR M and L to get R
			String R = "";
			for (int j = 0; j < M.length() && j < L.length(); j++) {
				R += M.charAt(j) ^ L.charAt(j);
			}

			LandRArray[i][0] = L;
			LandRArray[i][1] = R;
		}
	}

	/**
	 * Constructor
	 */
	public Customer() {
		super();
	}

	/**
	 * Create the frame to hold the panel.
	 */
	public void createAndShowGUI() {
		// Create and set up the window
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.frame.setContentPane(this);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.frame.setMinimumSize(new Dimension(250, this.frame.getPreferredSize().height));
		this.frame.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth()/2) - (this.frame.getSize().width*2)), 200);

		JPanel customerPane = new JPanel();
		customerPane.setLayout(new BoxLayout(customerPane, BoxLayout.PAGE_AXIS));
		customerPane.setBorder(BorderFactory.createTitledBorder("Customer Information"));
		customerPane.add(new JLabel("Name: " + Customer.name));
		customerPane.add(new JLabel("Address: " + Customer.address));
		customerPane.add(new JLabel("Phone: " + Customer.phone));
		this.add(customerPane);

		JPanel bankPane = new JPanel();
		bankPane.setLayout(new BoxLayout(bankPane, BoxLayout.PAGE_AXIS));
		bankPane.setBorder(BorderFactory.createTitledBorder("New Bank Transaction"));
		amount = new TextField();
		amount.setText("Money Order Amount:");
		bankPane.add(amount);

		this.submit = new JButton("Submit Money Order");
		this.submit.addActionListener(this);
		this.submit.setActionCommand("submit");
		this.submit.setToolTipText("Submit the Money Order");
		this.submit.addActionListener(this);
		bankPane.add(submit);

		this.badAmountTest = new JButton("Run Bad Amount Test");
		this.badAmountTest.addActionListener(this);
		this.badAmountTest.setActionCommand("badAmount");
		this.badAmountTest.setToolTipText("run bad amount test");
		this.badAmountTest.addActionListener(this);
		bankPane.add(badAmountTest);

		this.badUniqunessTest = new JButton("Run Bad Uniquness Test");
		this.badUniqunessTest.addActionListener(this);
		this.badUniqunessTest.setActionCommand("badUniquness");
		this.badUniqunessTest.setToolTipText("run bad uniquness test");
		this.badUniqunessTest.addActionListener(this);
		bankPane.add(badUniqunessTest);

		this.add(bankPane);

		JPanel merchantPane = new JPanel();
		merchantPane.setLayout(new BoxLayout(merchantPane, BoxLayout.PAGE_AXIS));
		merchantPane.setBorder(BorderFactory.createTitledBorder("Merchant Transactions"));
		this.sendToMerchant = new JButton("Send Money to Merchant");
		this.sendToMerchant.addActionListener(this);
		this.sendToMerchant.setActionCommand("sendToMerchant");
		this.sendToMerchant.setToolTipText("Send Money to Merchant");
		this.sendToMerchant.addActionListener(this);
		merchantPane.add(sendToMerchant);

		this.sendBadUniquenessToMerchant = new JButton("Send Bad Uniqueness Money Order to Merchant");
		this.sendBadUniquenessToMerchant.addActionListener(this);
		this.sendBadUniquenessToMerchant.setActionCommand("sendBadUniquenessToMerchant");
		this.sendBadUniquenessToMerchant.setToolTipText("Already used Unuiqueness String");
		this.sendBadUniquenessToMerchant.addActionListener(this);
		merchantPane.add(sendBadUniquenessToMerchant);
		this.add(merchantPane);

		this.status = new JTextArea(5, 20);
		JScrollPane scrollPane = new JScrollPane(this.status);
		scrollPane.setSize(500, 100);
		this.status.setEditable(false);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
		this.frame.add(scrollPane);

		// Display the window
		this.frame.pack();
		this.frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("submit")) {

			// If the amount TextField has data, try to make the connection
			if ((amount.getText() != null && !amount.getText().isEmpty())){
				transactionAmount = Double.valueOf(amount.getText());

				// Prepares 'n' anonymous money orders for a given amount
				moneyOrderArray = new Ecash[numMoneyOrders];
				LandRArray = new String[numMoneyOrders][2];

				status.append("\nGenerating Identity Strings...");
				splitIdentity();

				status.append("\nBit-Commitment in progress...");
				for (int i = 0; i < moneyOrderArray.length; i++) {
					for (int j = 0; j < LandRArray.length; j++) {
						//						System.out.println("MO: " + i + " | L/R: " + j );
						moneyOrderArray[i] = new Ecash(transactionAmount, commitment(LandRArray[j][0]), commitment(LandRArray[j][1]));
					}
				}
				status.append("Bit-Commitment complete...");

				ObjectOutputStream out = null;
				ObjectInputStream in = null;
				Socket requestSocket = null;
				try{
					//1. creating a socket to connect to the server
					requestSocket = new Socket("localhost", 2004);
					requestSocket.setKeepAlive(false);
					System.out.println("Connected to localhost in port 2004");
					//2. get Input and Output streams
					out = new ObjectOutputStream(requestSocket.getOutputStream());
					out.flush();
					// Send the money order array to the bank
					out.writeObject(moneyOrderArray);
					out.flush();
					System.out.println("Money Order Array Sent to the Bank...");
					status.append("Money Order Array Sent to the Bank...");
					in = new ObjectInputStream(requestSocket.getInputStream());
					signedObject = (SignedObject) in.readObject();
					System.out.println("Signed Money Order Received back from bank");
					status.append("\nMoney Order Received back from bank");
				}

				catch(UnknownHostException unknownHost){
					status.append("Unknown Host");
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				try {
					in.close();
					out.close();
					requestSocket.close();

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			} //end if text field is not empty
			else{
				status.append("No Amount Entered for Money Order");		
			}
		} // end if

		if(e.getActionCommand().equals("badAmount")) {

			// If the amount TextField has data, try to make the connection
			if ((amount.getText() != null && !amount.getText().isEmpty())){
				transactionAmount = Double.valueOf(amount.getText());

				// Prepares 'n' anonymous money orders for a given amount
				moneyOrderArray = new Ecash[numMoneyOrders];
				LandRArray = new String[numMoneyOrders][2];

				for (int i = 0; i < moneyOrderArray.length; i++) {
					for (int j = 0; j < LandRArray.length; j++) {
						//						System.out.println("MO: " + i + " | L/R: " + j );
						moneyOrderArray[i] = new Ecash(transactionAmount, commitment(LandRArray[j][0]), commitment(LandRArray[j][1]));
					}
				}

				// I don't know what think is
				// moneyOrderArray[moneyOrderArray.length-3] = new Ecash(1.23, testIdentity);

				ObjectOutputStream out = null;
				ObjectInputStream in = null;
				Socket requestSocket = null;
				try{
					//1. creating a socket to connect to the server
					requestSocket = new Socket("localhost", 2004);
					System.out.println("Connected to localhost in port 2004");
					//2. get Input and Output streams
					out = new ObjectOutputStream(requestSocket.getOutputStream());
					out.flush();
					// Send the money order array to the bank
					out.writeObject(moneyOrderArray);
					out.flush();
					System.out.println("Money Order Array Sent to the Bank...");
					in = new ObjectInputStream(requestSocket.getInputStream());
					Ecash signedEcashFromBank = (Ecash) in.readObject();
					System.out.println("Money Order Received back from bank for "+ signedEcashFromBank.getAmount());
				}

				catch(UnknownHostException unknownHost){
					status.append("Unknown Host");
				}
				catch(IOException ioException){
					ioException.printStackTrace();
					status.append("Error Connecting");				
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally{
					//4: Closing connection
					try{
						in.close();
						out.close();
						requestSocket.close();
					}
					catch(IOException ioException){
						ioException.printStackTrace();
					}
				} // finally
			} //end if text field is not empty
			else{
				status.append("Error Connecting");
			}
		} // end if

		if(e.getActionCommand().equals("badUniquness")) {

			// If the amount TextField has data, try to make the connection
			if ((amount.getText() != null && !amount.getText().isEmpty())){
				transactionAmount = Double.valueOf(amount.getText());

				// Prepares 'n' anonymous money orders for a given amount
				moneyOrderArray = new Ecash[numMoneyOrders];
				LandRArray = new String[numMoneyOrders][2];

				for (int i = 0; i < moneyOrderArray.length; i++) {
					for (int j = 0; j < LandRArray.length; j++) {
						moneyOrderArray[i] = new Ecash(transactionAmount, commitment(LandRArray[j][0]), commitment(LandRArray[j][1]));
					}
				}

				moneyOrderArray[moneyOrderArray.length-3].setUniqueness(moneyOrderArray[moneyOrderArray.length-4].getUniqueness()); 

				ObjectOutputStream out = null;
				ObjectInputStream in = null;
				Socket requestSocket = null;
				try{
					//1. creating a socket to connect to the server
					requestSocket = new Socket("localhost", 2004);
					System.out.println("Connected to localhost in port 2004");
					//2. get Input and Output streams
					out = new ObjectOutputStream(requestSocket.getOutputStream());
					out.flush();
					// Send the money order array to the bank
					out.writeObject(moneyOrderArray);
					out.flush();
					System.out.println("Money Order Array Sent to the Bank...");
					in = new ObjectInputStream(requestSocket.getInputStream());
					Ecash signedEcashFromBank = (Ecash) in.readObject();
					System.out.println("Money Order Received back from bank for "+ signedEcashFromBank.getAmount());
				}

				catch(UnknownHostException unknownHost){
					status.append("Unknown Host");
				}
				catch(IOException ioException){
					ioException.printStackTrace();
					status.append("Error Connecting");				
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally{
					//4: Closing connection
					try{
						in.close();
						out.close();
						requestSocket.close();
					}
					catch(IOException ioException){
						ioException.printStackTrace();
					}
				} // finally
			} //end if text field is not empty
			else{
				status.append("Error Connecting");		
			}
		} // end if

		if(e.getActionCommand().equals("sendToMerchant")) {

			ObjectOutputStream out = null;
			ObjectInputStream in = null;
			Socket requestSocket = null;
			try{
				//1. creating a socket to connect to the server
				requestSocket = new Socket("localhost", 2005);
				System.out.println("Connected to localhost in port 2005");
				//2. get Input and Output streams
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				out.flush();
				// Send the money order array to the bank
				out.writeObject(signedObject);
				out.flush();
				System.out.println("Money Order Sent to the Merchant...");
				status.append("\nMoney Order Sent to the Merchant...");
				out.close();
				requestSocket.close();
			}
			catch(UnknownHostException unknownHost){
				status.append("Unknown Host");
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			} //end if text field is not empty

		} // end if
		if(e.getActionCommand().equals("sendBadUniquenessToMerchant")) {
			BadUniquenessToMerchant = true;
			ObjectOutputStream out = null;
			ObjectInputStream in = null;
			Socket requestSocket = null;
			try{
				//1. creating a socket to connect to the server
				requestSocket = new Socket("localhost", 2005);
				System.out.println("Connected to localhost in port 2005");
				//2. get Input and Output streams
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				out.flush();
				// Send the money order array to the bank
				out.writeObject(signedObject);
				out.flush();
				System.out.println("Money Order Sent to the Merchant...");
				status.append("\nMoney Order Sent to the Merchant...");
				out.close();
				requestSocket.close();
			}
			catch(UnknownHostException unknownHost){
				status.append("Unknown Host");
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			} //end if text field is not empty

		} // end if
	}
} // end method

