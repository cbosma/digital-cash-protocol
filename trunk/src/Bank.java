import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
public class Bank extends JPanel implements ActionListener{

	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = 8465685567853888181L;

	/**
	 * Constructor
	 */
	public Bank() {
		super();
	}
	
	/**
	 * Create the frame to hold the panel.
	 */
	public void createAndShowGUI() {
		// Create and set up the window
		JFrame frame = new JFrame("Bank");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setMinimumSize(new Dimension(250, frame.getPreferredSize().height));
		frame.setLocation((((int) Toolkit.getDefaultToolkit().getScreenSize()
				.getWidth() - frame.getSize().width) / 2), 200);

		Container contentPane = frame.getContentPane();
		SpringLayout layout = new SpringLayout();
		contentPane.setLayout(layout);
		
		JLabel title = new JLabel("The Bank");
		contentPane.add(title);
		layout.putConstraint(SpringLayout.NORTH, title, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, title, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, contentPane, 5, SpringLayout.EAST, title);
		layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, title);
		
		// Display the window
		frame.pack();
		frame.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}