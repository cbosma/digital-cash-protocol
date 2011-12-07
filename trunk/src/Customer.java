import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

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
	private JButton submit = null;
	private TextField amount = null;
	private TextField error = null;
	private Double transationAmount = null;
	private int numMoneyOrders = 100;
	private String testIdentity = "test";
	private Ecash[] moneyOrderArray = null; 

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
	} // end method
}
