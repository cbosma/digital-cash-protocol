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


public class Customer extends JPanel implements ActionListener{

	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = 8465685567853888181L;

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
		JFrame frame = new JFrame("Customer");
		frame.setLayout(new GridLayout(2,1));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setMinimumSize(new Dimension(250, frame.getPreferredSize().height));
		frame.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth() - frame.getSize().width) / 2), 200);

		JTextField amount = new JTextField("Amount:", 8);
		amount.setHorizontalAlignment(JTextField.CENTER);
		amount.setToolTipText("Enter the amount to transfer");
		frame.add(amount);

		JButton submit = new JButton("Submit");
		submit.addActionListener(this);
		submit.setActionCommand("submit");
		submit.setToolTipText("Submit your purchase");
		submit.addActionListener(this);
		frame.add(submit);

		// Display the window
		frame.pack();
		frame.setVisible(true);
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
				System.err.println("You are trying to connect to an unknown host!");
			}
			catch(IOException ioException){
				ioException.printStackTrace();
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
