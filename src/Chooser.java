import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

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
public class Chooser extends JPanel {

	/**
	 * Randomly Generated Serial Version UID
	 */
	private static final long serialVersionUID = -3623821215053750506L;

	/**
	 * Create the panel.  Setting the layout and adding the buttons.
	 */
	public Chooser() {
		super(new GridLayout(3, 1));

		add(new JButton("Customer"));
		add(new JButton("Merchant"));
		add(new JButton("Bank"));
	}

	/**
	 * Create the frame to hold the panel.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window
		JFrame frame = new JFrame("Choose Your Role");
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
}
