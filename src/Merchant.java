import java.awt.Dimension;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * ===== Requirements =====
 * - verification of the legitimacy of the bank's signature
 * - random generator of the selector string, which determines
 *   the half of the identity string the customer is required to reveal
 */
public class Merchant extends JPanel implements ActionListener{

	
	private Ecash EcashFromCustomer = null;
	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = 8465685567853888181L;

	/**
	 * Constructor
	 */
	public Merchant() {
		super();
	}

	/**
	 * Create the frame to hold the panel.
	 */
	public void createAndShowGUI() {
		// Create and set up the window
		JFrame frame = new JFrame("Merchant");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setMinimumSize(new Dimension(250, frame.getPreferredSize().height));
		frame.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth() - frame.getSize().width) / 2), 200);

		// Display the window
		frame.pack();
		frame.setVisible(true);

		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		ServerSocket providerSocket = null;
		try {
			//1. creating a server socket
			providerSocket = new ServerSocket(2005, 10);
			//2. Wait for connection
			System.out.println("Waiting for connection from Customer...");

			Socket connection = providerSocket.accept();
			System.out.println("Connection received from Customer at " + connection.getInetAddress().getHostName());

			//3. Get Input Stream
			in = new ObjectInputStream(connection.getInputStream());
			//4. The two parts communicate via the input and output streams
			try{
				EcashFromCustomer = (Ecash) in.readObject();
				System.out.println("Received Ecash from the Customer");
				//TODO - Merchant needs to verify the Banks signature
//				Signature verificationEngine =
//					     Signature.getInstance(algorithm, provider);
//					 i f (so.verify(publickey, verificationEngine))
//					     try {
//					         Object myobj = so.getObject();
//					     } catch (java.lang.ClassNotFoundException e) {};
					 

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
	}

}
