import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.util.Properties;

import javax.swing.JPanel;

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
public class BankServer extends Thread{

	private static Ecash[] moneyOrderArrayFromCustomer = null; 
	private static String[] uniqueness = new String[100]; 
	private static double moneyOrderAmount;
	private static boolean matchingAmounts = true;
	private static boolean matchingUniqueness = false;
	private static boolean tmpUniqueness = false;
	private static SignedObject signedObject;

	/**
	 * Properties object that holds all account information
	 */
	static Properties accountProps = new Properties();

	@Override
	public void run() {
		readProperties();
		BankInterface.accountNum.setText("Account Number: " + BankServer.accountProps.getProperty("accountNum"));
		BankInterface.customerBalance.setText("Account Balance: " + BankServer.accountProps.getProperty("customerBalance"));
		BankInterface.merchAccountNum.setText("Account Number: " + BankServer.accountProps.getProperty("merchAccountNum"));
		BankInterface.merchantBalance.setText("Account Balance: " + BankServer.accountProps.getProperty("merchantBalance"));
		setupSockets();
	}

	/**
	 * Open the properties file, generate a default if it doesn't exist.
	 * The use of default settings is only for functionality proofing,
	 * real accounts would be stored in a database and encrypted.
	 * 
	 * For now, the properties file will consist of
	 * customerBalance = [some integer]
	 * merchantBalance = [some integer]
	 */
	private void readProperties() {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream("accountProperties");
		} catch (FileNotFoundException e) {
			// No properties file, so making a default
			try {
				out = new FileOutputStream("accountProperties");
			} catch (FileNotFoundException e2) {
				// can't create a file
				e2.printStackTrace();
				System.exit(JPanel.ERROR);
			}
			try {
				accountProps.setProperty("customerBalance", "100");
				accountProps.setProperty("merchantBalance", "100");
				accountProps.setProperty("accountNum", "0123456789");
				accountProps.store(out, "Default setup");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e1) {
				// Error closing the file
				System.out.println("Error closing the properties file.");
				e1.printStackTrace();
			}
		}
		try {
			// utilize the in stream if it exists
			if ( in != null ){
				accountProps.load(in);
			}
		} catch (IOException e) {
			// Error loading the properties file, create defaults
			System.out.println("Error loading the properties");
			accountProps.setProperty("customerBalance", "100");
			accountProps.setProperty("merchantBalance", "100");
			try {
				accountProps.store(out, "Default setup");
			} catch (IOException e1) {
				// Error writing the account info back out
				System.out.println("Error storing properties.");
			}
		}
		try {
			// close the in stream if it exists
			if ( in != null ){
				in.close();
			}
		} catch (IOException e) {
			// Error closing the file
			System.out.println("Error closing the properties file.");
		}

	}

	private void setupSockets() {
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		ServerSocket providerSocket = null;
		try {
			//1. creating a server socket
			providerSocket = new ServerSocket(2004, 10);
			//2. Wait for connection
			System.out.println("Waiting for connection from Customer...");
			BankInterface.status.append("\nWaiting...");
			Socket connection = providerSocket.accept();
			System.out.println("Connection received from Customer at " + connection.getInetAddress().getHostName());
			//3. Get Input Stream
			in = new ObjectInputStream(connection.getInputStream());
			//4. The two parts communicate via the input and output streams
			try{
				moneyOrderArrayFromCustomer = (Ecash[]) in.readObject();
				System.out.println("Received " + moneyOrderArrayFromCustomer.length + " money orders from the Customer");
				// Compare the amounts of n-1 money orders check the uniqueness string 
				if( compareMoneyOrders() == true ){
					// Bank deducts the amount from their account
					Double currBalance = Double.valueOf(accountProps.getProperty("customerBalance"));

					// Check to see the the Customer has enough money to complete the withdraw
					if ( currBalance - (moneyOrderArrayFromCustomer[moneyOrderArrayFromCustomer.length-1].getAmount()) >= 0 ){
						accountProps.setProperty("customerBalance", String.valueOf((currBalance - (moneyOrderArrayFromCustomer[moneyOrderArrayFromCustomer.length-1].getAmount()))));
						BankInterface.accountNum.setText("Account Number: " + BankServer.accountProps.getProperty("accountNum"));
						BankInterface.customerBalance.setText("Account Balance: " + BankServer.accountProps.getProperty("customerBalance"));
						System.out.println(String.valueOf((currBalance - (moneyOrderArrayFromCustomer[moneyOrderArrayFromCustomer.length-1].getAmount()))));
						System.out.println("Money was removed from Customers Account");
						BankInterface.status.append("\nMoney was removed from Customers Account");

						// The bank signs one of the Ecash Objects
						if ( signMoneyOrder(moneyOrderArrayFromCustomer[moneyOrderArrayFromCustomer.length-1])){
							System.out.println("Signed one Ecash Object to send back to Customer");
							BankInterface.status.append("\nSigned one Ecash Object to send back to Customer");
							// Bank hands the blinded money order back to Customer 
							out = new ObjectOutputStream(connection.getOutputStream());
							out.flush();
							out.writeObject(signedObject);
							out.flush();
							System.out.println("Sent Signed Money Order back to Customer");
							BankInterface.status.append("\nSent Signed Money Order back to Customer");

							in.close();
							out.close();
							connection.close();
							providerSocket.close();

							//1. creating a server socket for merchant
							providerSocket = new ServerSocket(2006, 10);
							//2. Wait for connection
							System.out.println("Waiting for connection from Merchant...");
							BankInterface.status.append("\nWaiting for connection from Merchant...");
							connection = providerSocket.accept();
							System.out.println("Connection received from Customer at " + connection.getInetAddress().getHostName());
							//3. Get Input Stream
							in = new ObjectInputStream(connection.getInputStream());
							//4. The two parts communicate via the input and output streams
							signedObject = (SignedObject) in.readObject();
							PublicKey publicKey;
							try {
								publicKey = (PublicKey) readFromFile("publicKey.dat");
								Signature sig = Signature.getInstance("DSA");
								if (signedObject.verify(publicKey, sig)){
									System.out.println("Bank has checked, and the Banks Signature is good");
									MerchantInterface.status.append("Bank has checked, and the Banks Signature is good");
								}

							} catch (Exception e) {
								e.printStackTrace();
							}

							}
							else{
								System.out.println("Bank could not sign the Money Order");
								BankInterface.status.append("\nBank could not sign the Money Order");							
							}
						}
						else{
							System.out.println("Customer does not have sufficient funds");
							BankInterface.status.append("\nCustomer does not have sufficient funds");							
						}
					} else {
						connection.close();
					}
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

		public static Boolean signMoneyOrder(Ecash ecashToSign){
			// The Bank is satisfied that Customer did not make any attempts to cheat, so sign the one remaining money order 
			try {

				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
				keyGen.initialize(1024);
				KeyPair keypair = keyGen.genKeyPair();
				PrivateKey privateKey = keypair.getPrivate();
				PublicKey publicKey = keypair.getPublic();
				try {
					writeToFile("publicKey.dat", publicKey);
				} catch (Exception e) {
					e.printStackTrace();
				}


				Signature sig = Signature.getInstance(privateKey.getAlgorithm());
				signedObject = new SignedObject(ecashToSign, privateKey, sig);
				sig = Signature.getInstance(publicKey.getAlgorithm());
				if ( signedObject.verify(publicKey, sig) ){
					return true;
				}
				else{
					return false;
				}
			} 

			catch (NoSuchAlgorithmException e) {
				System.err.println("Error Signing Ecash Object");
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				System.err.println("Error Signing Ecash Object");
				e.printStackTrace();
			} catch (SignatureException e) {
				System.err.println("Error Signing Ecash Object");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Error Signing Ecash Object");
				e.printStackTrace();
			}
			return false;
		}

		public static Boolean compareMoneyOrders(){
			// The bank checks the amount of n-1 money orders
			// Open n-1 money orders and see that they all have the same amount
			for ( int i = 0; i < moneyOrderArrayFromCustomer.length -2; i++ ){

				// Save the first amount and uniqueness string to compare to the others
				if(i == 0){
					if ( moneyOrderArrayFromCustomer[i].getAmount() != null ){
						moneyOrderAmount= moneyOrderArrayFromCustomer[i].getAmount();					
					}
					if( moneyOrderArrayFromCustomer[i].getUniqueness() != null ){
						uniqueness[i] = moneyOrderArrayFromCustomer[i].getUniqueness();
					}
				}
				// Compare all other amounts and uniqueness strings  to the first
				else{
					// If there is a mismatch then set the matching boolean to false
					if( moneyOrderArrayFromCustomer[i].getAmount() != moneyOrderAmount){
						System.err.println("The amounts do not match for " + moneyOrderArrayFromCustomer[i].getAmount() + " and " + moneyOrderAmount);
						BankInterface.status.append("\nThe amounts do not match for " + moneyOrderArrayFromCustomer[i].getAmount() + " and " + moneyOrderAmount);
						matchingAmounts = false;
					}
					// If any two uniqueness string match set the matching unuqieness to true
					if ( uniqueness != null){
						tmpUniqueness = false;
						for ( int j = 0; j < uniqueness.length; j++ ){
							if ( uniqueness[j] != null){
								if ( uniqueness[j].compareTo(moneyOrderArrayFromCustomer[i].getUniqueness()) == 0 ){
									matchingUniqueness = true;
									tmpUniqueness = true;
									System.err.println("The two Uniqueness Strings " + uniqueness[j].toString() + " are the same, you are cheating!");
									BankInterface.status.append("\nThe two Uniqueness Strings " + uniqueness[j].toString() + " are the same, you are cheating!");
								}	
							}
						}
						if ( tmpUniqueness == false){
							uniqueness[i] = moneyOrderArrayFromCustomer[i].getUniqueness();
						}
					}
				}
			}
			if ( matchingAmounts == true && matchingUniqueness == false ){
				System.out.println("All amounts matched and All Uniqueness String are different!");
				BankInterface.status.append("\nAll amounts matched and All Uniqueness String are different!");
				return true;
			}
			else{
				System.err.println("The amounts did not match or Two Uniqueness String are the same, you are cheating!");
				BankInterface.status.append("\nThe amounts did not match or Two Uniqueness String are the same, you are cheating!");
				return false;
			}	
		}

		private static void writeToFile(String filename, Object object) throws Exception {
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;

			try {
				fos = new FileOutputStream(new File(filename));
				oos = new ObjectOutputStream(fos);
				oos.writeObject(object);
				oos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (oos != null) {
					oos.close();
				}
				if (fos != null) {
					fos.close();
				}
			}
		}

		private static Object readFromFile(String filename) throws Exception {
			FileInputStream fis = null;
			ObjectInputStream ois = null;
			Object object = null;

			try {
				fis = new FileInputStream(new File(filename));
				ois = new ObjectInputStream(fis);
				object = ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (ois != null) {
					ois.close();
				}
				if (fis != null) {
					fis.close();
				}
			}
			return object;
		}

	}