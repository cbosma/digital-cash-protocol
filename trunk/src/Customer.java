import java.awt.Dimension;
import java.awt.GridLayout;
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
import javax.swing.JTextField;

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
	private JTextField amount;
	private JButton submit;
	private JTextField error;
	
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

		amount = new JTextField("Amount:", 8);
		amount.setHorizontalAlignment(JTextField.CENTER);
		amount.setToolTipText("Enter the amount to transfer");
		this.frame.add(amount);

		this.submit = new JButton("Submit");
		this.submit.addActionListener(this);
		this.submit.setActionCommand("submit");
		this.submit.setToolTipText("Submit your purchase");
		this.submit.addActionListener(this);
		this.frame.add(submit);

		// Display the window
		this.frame.pack();
		this.frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("submit")) {

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
				in = new ObjectInputStream(requestSocket.getInputStream());
				out.writeObject("Test Message to Server");
				out.flush();
				System.out.println("Message sent from client");
			}
			catch(UnknownHostException unknownHost){
				this.error = new JTextField("Unknown Host");
				error.setEditable(false);
				error.setHorizontalAlignment(JTextField.CENTER);
				error.setToolTipText("You are trying to connect to an unknown host!");
				this.frame.remove(this.amount);
				this.frame.remove(this.submit);				
				this.frame.add(error);
				// Display the window
				this.frame.pack();
				this.frame.setVisible(true);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
				
				this.error = new JTextField("Error Connecting");
				error.setEditable(false);
				error.setHorizontalAlignment(JTextField.CENTER);
				error.setToolTipText("Error Connecting");
				this.frame.remove(this.amount);
				this.frame.remove(this.submit);				
				this.frame.add(error);
				// Display the window
				this.frame.pack();
				this.frame.setVisible(true);
				
				
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
		} // end if
	} // end method
}
