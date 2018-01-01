package answer.king.service;

public class InsufficientPaymentException extends Exception {

	private static final long serialVersionUID = -6834379681218109582L;

	public InsufficientPaymentException(String message) {
		super(message);
	}
}
