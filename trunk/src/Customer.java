import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
	    frame.add(amount);
	    
		JButton submit = new JButton("Submit");
		submit.addActionListener(this);
		submit.setActionCommand("submit");
		submit.setToolTipText("Submit your purchase");
		frame.add(submit);
		
		// Display the window
		frame.pack();
		frame.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
