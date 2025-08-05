package java.br.com.longo.rinha2025;

/**
 * Response object for payment processing
 */
public class PaymentResponse {
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}