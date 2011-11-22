import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Choose the role to be for this demo.
 * - Customer
 * - Merchant
 * - Bank
 */
public class Chooser extends JPanel implements ActionListener {

	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = -3623821215053750506L;
	
	private static JFrame frame;

	/**
	 * Create the panel.  Setting the layout and adding the buttons.
	 */
	public Chooser() {
		super(new GridLayout(3, 1));
		
		JButton custButton = new JButton("Customer");
		JButton merchButton = new JButton("Merchant");
		JButton bankButton = new JButton("Bank");

		custButton.addActionListener(this);
		merchButton.addActionListener(this);
		bankButton.addActionListener(this);
		
		custButton.setMnemonic(KeyEvent.VK_C);
		merchButton.setMnemonic(KeyEvent.VK_M);
		bankButton.setMnemonic(KeyEvent.VK_B);
		
		custButton.setActionCommand("customer");
		merchButton.setActionCommand("merchant");
		bankButton.setActionCommand("bank");
		
		custButton.setToolTipText("Choose the customer role");
		merchButton.setToolTipText("Choose the merchant role");
		bankButton.setToolTipText("Choose the bank role");
		
		add(custButton);
		add(merchButton);
		add(bankButton);
	}

	/**
	 * Create the frame to hold the panel.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window
		frame = new JFrame("Choose Your Role");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setMinimumSize(new Dimension(250, frame.getPreferredSize().height));
		frame.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth() - frame.getSize().width) / 2), 200);

		// Add content
		frame.add(new Chooser());

		// Display the window
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		// Create and show the application
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Turn off bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);

				createAndShowGUI();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("customer")) {
			Customer cust = new Customer();
			this.setVisible(false);
			frame.setVisible(false);
			cust.createAndShowGUI();
//			System.exit(0);
		}else if (e.getActionCommand().equals("merchant")) {
			Merchant merch = new Merchant();
			this.setVisible(false);
			frame.setVisible(false);
			merch.createAndShowGUI();
//			System.exit(0);
		}else if (e.getActionCommand().equals("bank")) {
			Bank bank = new Bank();
			this.setVisible(false);
			frame.setVisible(false);
			bank.createAndShowGUI();
//			System.exit(0);
		}
	}
}
