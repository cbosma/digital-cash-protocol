import java.awt.Dimension;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignedObject;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * ===== Requirements =====
 * - verification of the legitimacy of the bank's signature
 * - random generator of the selector string, which determines
 *   the half of the identity string the customer is required to reveal
 */
public class MerchantInterface extends JFrame implements ActionListener{


	private Ecash EcashFromCustomer = null;
	private static SignedObject signedObject;

	protected static JTextArea status = new JTextArea();
	private JButton startSockets;

	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = 8465685567853888181L;

	/**
	 * Constructor
	 */
	public MerchantInterface() {
		super("Merchant");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setMinimumSize(new Dimension(500, (getPreferredSize().height) + 300));
		setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth()) / 2), 440);

		JPanel panel = (JPanel) this.getContentPane();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		this.startSockets = new JButton("Open the Store");
		this.startSockets.setActionCommand("open");
		this.startSockets.addActionListener(this);
		panel.add(this.startSockets);

		this.status = new JTextArea(5, 20);
		JScrollPane scrollPane = new JScrollPane(this.status);
		scrollPane.setSize(500, 100);
		this.status.setEditable(false);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {  
			e.getAdjustable().setValue(e.getAdjustable().getMaximum());  
			}});  
		panel.add(scrollPane);

		// Display the window
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		// Create and show the application
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Turn off bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);

				// Start Interface
				MerchantInterface window = new MerchantInterface();
				window.setVisible(true);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//checks if the button clicked
		if(e.getActionCommand().equals("open"))
		{
			// start server
			MerchantServer server = new MerchantServer();
			server.start();
		}else if(e.getActionCommand().equals("close")) {
			// stop server  
//        	server.stopserver();
		}
	}
}