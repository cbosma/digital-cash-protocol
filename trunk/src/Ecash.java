import java.io.Serializable;

/**
 * ===== Requirements =====
 * The electronic cash (ecash) used during these transactions is a file
 * which contains:
 * - the amount of the transaction involved;
 * - an uniqueness string number;
 * - identity strings which contain the identity of the customer (this 
 *   information remains secret unless the customer tries to use the ecash 
 *   illicitly - more than once);
 * - bank's signature (before the customer can use the ecash).
 */
public class Ecash implements Serializable{

	// The amount of the transaction involved
	private Double amount;

	// An uniqueness string number
	private String uniqueness;

	// TODO - add banks signature
	// TODO - add identity strings which contain the identity of the customer
	

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}


}
