package java.br.com.longo.rinha2025;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.lang.NonNull;

import java.time.Instant;

public class PaymentRequest {
    @NonNull
    private String correlationId;
    @NonNull
    private double amount;
    @NonNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant requestedAt;

    //constructor with fields
    public PaymentRequest (String correlationId, double amount, Instant requestedAt){
        this.correlationId = correlationId;
        this.amount = amount;
        this.requestedAt = requestedAt;
    }

    // Default constructor for JSON deserialization
    public PaymentRequest() {
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }
}
