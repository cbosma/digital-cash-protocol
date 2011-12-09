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
import java.util.BitSet;
import java.util.Random;

/**
 * ===== Requirements =====
 * - verification of the legitimacy of the bank's signature
 * - random generator of the selector string, which determines
 *   the half of the identity string the customer is required to reveal
 */
public class MerchantServer extends Thread {
	private Ecash EcashFromCustomer = null;
	private static SignedObject signedObject;
	private static String[][] identityResultsFromCustomer = null;
	
	public static String generateBitVector() {
        String bitVector = "";

        int bitVectorLength = 100;
       
        for (int i = 0; i < bitVectorLength; i++) {
            Random randomGenerator = new Random();
            int randomInt = randomGenerator.nextInt(2);
            bitVector += randomInt;
        }
       
        for (int i = 0; i < bitVector.length(); i++) {
            if (bitVector.charAt(i) == '0') {
//                System.out.println("Reveal Right " + i);   
            }
           
            else if (bitVector.charAt(i) == '1') {
//                System.out.println("Reveal Left " + i);
            }
        }
        
        return bitVector;
	}
	
	/**
	 * Constructor
	 */
	@Override
	public void run() {
		setupSockets();
	}

	/**
	 * Create the frame to hold the panel.
	 */
	public void setupSockets() {
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		ServerSocket providerSocket = null;
		try {
			//1. creating a server socket
			providerSocket = new ServerSocket(2005, 10);
			//2. Wait for connection
			System.out.println("Waiting for connection from Customer...");
			MerchantInterface.status.append("Waiting for connection from Customer...");

			Socket connection = providerSocket.accept();
			System.out.println("Connection received from Customer at " + connection.getInetAddress().getHostName());

			//3. Get Input Stream
			in = new ObjectInputStream(connection.getInputStream());
			//4. The two parts communicate via the input and output streams
			try{
				signedObject = (SignedObject) in.readObject();
				System.out.println("Received a Signed Ecash from the Customer");
				MerchantInterface.status.append("\nReceived a Signed Ecash from the Customer");
				PublicKey publicKey;
				try {
					publicKey = (PublicKey) readFromFile("publicKey.dat");
					Signature sig = Signature.getInstance("DSA");
					if (signedObject.verify(publicKey, sig)){
						System.out.println("\nMerchant has checked, and the Banks Signature is good");
						MerchantInterface.status.append("\nMerchant has checked, and the Banks Signature is good");
						EcashFromCustomer = (Ecash) signedObject.getObject();
						System.out.println("The Ecash received from the customer is good for $" + EcashFromCustomer.getAmount());
						MerchantInterface.status.append("\nThe Ecash received from the customer is good for $" + EcashFromCustomer.getAmount());
						out = new ObjectOutputStream(connection.getOutputStream());
						out.flush();
						// Send the signed money order to the bank
						out.writeObject(generateBitVector());
						out.flush();
						System.out.println("Bit Vector sent to Merchant...");
						MerchantInterface.status.append("\nBit Vector sent to Customer...");
						
						identityResultsFromCustomer = (String[][]) in.readObject();
						System.out.println("Identity Results Received from Customer...");
						MerchantInterface.status.append("\nIdentity Results Received from Customer...");
						
						in.close();
						out.close();
						connection.close();
						providerSocket.close();
										
						ObjectOutputStream bankOut = null;
						ObjectInputStream bankIn = null;
						Socket BankrequestSocket = null;
						
						
						try{
							//1. creating a socket to connect to the bank
							BankrequestSocket = new Socket("localhost", 2006);
							BankrequestSocket.setKeepAlive(false);
							System.out.println("Connected to localhost in port 2006");
							//2. get Input and Output streams
							bankOut = new ObjectOutputStream(BankrequestSocket.getOutputStream());
							bankOut.flush();
							// Send the identity results and signed money order to the bank
							bankOut.writeObject(identityResultsFromCustomer);
							bankOut.flush();
							System.out.println("Identity Results sent to Bank...");
							MerchantInterface.status.append("\nIdentity Results sent to Bank...");
							bankOut.writeObject(signedObject);
							bankOut.flush();
							System.out.println("Signed Money Order was sent to Bank for Verification...");
							MerchantInterface.status.append("\nSigned Money Order was sent to Bank for Verification...");
//							bankIn = new ObjectInputStream(BankrequestSocket.getInputStream());
//							signedObject = (SignedObject) bankIn.readObject();
//							System.out.println("Signed Money Order Received back from bank");
//							MerchantInterface.status.append("\nMoney Order Received back from bank");
						}

						catch(UnknownHostException unknownHost){
							MerchantInterface.status.append("Unknown Host");
						}
						catch(IOException ioException){
							ioException.printStackTrace();
						}
						
						try {
							in.close();
							BankrequestSocket.close();

						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
						
					}


				} catch (Exception e) {
					e.printStackTrace();
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
