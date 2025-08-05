package java.br.com.longo.rinha2025;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class  Endpoint {

    private final ProccessPayment proccessPayment;
    private final Map<String, PaymentCategoryStats> paymentCategoryStats;

    public Endpoint(ProccessPayment proccessPayment, Map<String, PaymentCategoryStats> paymentCategoryStats) {
        this.proccessPayment = proccessPayment;
        initializeStats();
        this.paymentCategoryStats = paymentCategoryStats;
    }

    // Method to initialize or reset stats (useful for testing)
    public static void initializeStats() {

    }

    @GetMapping("/payments-summary")
    public ResponseEntity<PaymentSummaryResponse> getPaymentsSummary(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        return ResponseEntity.ok(new PaymentSummaryResponse(paymentCategoryStats));
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest paymentRequest) {
        // Increment the default stats (we'll assume default server for stats)

        // Process the payment using the ProcessPayment service
        ResponseEntity<PaymentResponse> response = proccessPayment.processPayment(paymentRequest);

        // If the response is successful, return it with CREATED status
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response.getBody());
        }

        return response;
    }
}
