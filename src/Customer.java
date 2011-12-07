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
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
	private TextField error = null;
	private Double transationAmount = null;
	private int numMoneyOrders = 100;
	private String testIdentity = "test";
	private Ecash[] moneyOrderArray = null;

	// Constants
	private static final String name = "Alice";
	private static final String address = "8000 York Road Towson Maryland 21252";
	private static final String phone = "410-704-2000";
	private static final String email = "alice@towson.edu";
	private static final String ssn = "123456789";

	public void splitIdentity() {
		System.out.println(name);

		String binary = new BigInteger(name.getBytes()).toString(2);
		System.out.println(binary);

		String key = "";
		for (int i = 0; i < binary.length(); i++) {
			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt(2);
			key += randomInt;
		}
		System.out.println(key);

		String temp = "";
		for (int i = 0; i < binary.length() && i < key.length(); i++) {
			temp += binary.charAt(i) ^ key.charAt(i);
		}
		System.out.println(temp);

		/*
		String text2 = new String(new BigInteger(binary, 2).toByteArray());
		System.out.println(text2);
		 */
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
		splitIdentity();
		// Create and set up the window
		this.frame.setLayout(new GridLayout(2,1));
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.frame.setMinimumSize(new Dimension(250, this.frame.getPreferredSize().height));
		this.frame.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth() - this.frame.getSize().width) / 2), 200);

		amount = new TextField();
		amount.setText("Money Order Amount:");
		this.frame.add(amount);

		this.submit = new JButton("Submit Money Order");
		this.submit.addActionListener(this);
		this.submit.setActionCommand("submit");
		this.submit.setToolTipText("Submit the Money Order");
		this.submit.addActionListener(this);
		this.frame.add(submit);

		this.badAmountTest = new JButton("Run Bad Amount Test");
		this.badAmountTest.addActionListener(this);
		this.badAmountTest.setActionCommand("badAmount");
		this.badAmountTest.setToolTipText("run bad amount test");
		this.badAmountTest.addActionListener(this);
		this.frame.add(badAmountTest);

		this.badUniqunessTest = new JButton("Run Bad Uniquness Test");
		this.badUniqunessTest.addActionListener(this);
		this.badUniqunessTest.setActionCommand("badUniquness");
		this.badUniqunessTest.setToolTipText("run bad uniquness test");
		this.badUniqunessTest.addActionListener(this);
		this.frame.add(badUniqunessTest);

		// Display the window
		this.frame.pack();
		this.frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("submit")) {

			// If the amount TextField has data, try to make the connection
			if ((amount.getText() != null && !amount.getText().isEmpty())){
				transationAmount = Double.valueOf(amount.getText());

				// Prepares 'n' anonymous money orders for a given amount
				moneyOrderArray = new Ecash[numMoneyOrders];
				for ( int i = 0; i < moneyOrderArray.length; i++ ){
					moneyOrderArray[i] = new Ecash(transationAmount, testIdentity);
				}

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
					this.error = new TextField("Unknown Host");
					error.setEditable(false);
					this.frame.remove(this.amount);
					this.frame.remove(this.submit);				
					this.frame.add(error);
					// Display the window
					this.frame.pack();
					this.frame.setVisible(true);
				}
				catch(IOException ioException){
					ioException.printStackTrace();
					this.error = new TextField("Error Connecting");
					error.setEditable(false);
					this.frame.remove(this.amount);
					this.frame.remove(this.submit);				
					this.frame.add(error);
					// Display the window
					this.frame.pack();
					this.frame.setVisible(true);				
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
						System.exit(0);
					}
					catch(IOException ioException){
						ioException.printStackTrace();
					}
				} // finally
			} //end if text field is not empty
			else{
				this.error = new TextField("Error Connecting");
				error.setEditable(false);
				this.frame.remove(this.amount);
				this.frame.remove(this.submit);				
				this.frame.add(error);
				// Display the window
				this.frame.pack();
				this.frame.setVisible(true);		
			}
		} // end if

		if(e.getActionCommand().equals("badAmount")) {

			// If the amount TextField has data, try to make the connection
			if ((amount.getText() != null && !amount.getText().isEmpty())){
				transationAmount = Double.valueOf(amount.getText());

				// Prepares 'n' anonymous money orders for a given amount
				moneyOrderArray = new Ecash[numMoneyOrders];
				for ( int i = 0; i < moneyOrderArray.length; i++ ){
					moneyOrderArray[i] = new Ecash(transationAmount, testIdentity);
				}
				moneyOrderArray[moneyOrderArray.length-3] = new Ecash(1.23, testIdentity);

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
					this.error = new TextField("Unknown Host");
					error.setEditable(false);
					this.frame.remove(this.amount);
					this.frame.remove(this.submit);				
					this.frame.add(error);
					// Display the window
					this.frame.pack();
					this.frame.setVisible(true);
				}
				catch(IOException ioException){
					ioException.printStackTrace();
					this.error = new TextField("Error Connecting");
					error.setEditable(false);
					this.frame.remove(this.amount);
					this.frame.remove(this.submit);				
					this.frame.add(error);
					// Display the window
					this.frame.pack();
					this.frame.setVisible(true);				
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
						System.exit(0);
					}
					catch(IOException ioException){
						ioException.printStackTrace();
					}
				} // finally
			} //end if text field is not empty
			else{
				this.error = new TextField("Error Connecting");
				error.setEditable(false);
				this.frame.remove(this.amount);
				this.frame.remove(this.submit);				
				this.frame.add(error);
				// Display the window
				this.frame.pack();
				this.frame.setVisible(true);		
			}
		} // end if
		
		if(e.getActionCommand().equals("badUniquness")) {

			// If the amount TextField has data, try to make the connection
			if ((amount.getText() != null && !amount.getText().isEmpty())){
				transationAmount = Double.valueOf(amount.getText());

				// Prepares 'n' anonymous money orders for a given amount
				moneyOrderArray = new Ecash[numMoneyOrders];
				for ( int i = 0; i < moneyOrderArray.length; i++ ){
					moneyOrderArray[i] = new Ecash(transationAmount, testIdentity);
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
					this.error = new TextField("Unknown Host");
					error.setEditable(false);
					this.frame.remove(this.amount);
					this.frame.remove(this.submit);				
					this.frame.add(error);
					// Display the window
					this.frame.pack();
					this.frame.setVisible(true);
				}
				catch(IOException ioException){
					ioException.printStackTrace();
					this.error = new TextField("Error Connecting");
					error.setEditable(false);
					this.frame.remove(this.amount);
					this.frame.remove(this.submit);				
					this.frame.add(error);
					// Display the window
					this.frame.pack();
					this.frame.setVisible(true);				
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
						System.exit(0);
					}
					catch(IOException ioException){
						ioException.printStackTrace();
					}
				} // finally
			} //end if text field is not empty
			else{
				this.error = new TextField("Error Connecting");
				error.setEditable(false);
				this.frame.remove(this.amount);
				this.frame.remove(this.submit);				
				this.frame.add(error);
				// Display the window
				this.frame.pack();
				this.frame.setVisible(true);		
			}
		} // end if

		
		
	}
} // end method

