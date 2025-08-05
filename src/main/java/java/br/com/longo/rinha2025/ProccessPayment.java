package java.br.com.longo.rinha2025;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@Service
public class ProccessPayment {
    private static final Logger logger = LoggerFactory.getLogger(ProccessPayment.class);
    private static final String RETRY_NAME = "paymentRetry";
    private static final String SERVER_AVAILABLE_HEADER = "X-Server-Available-In";

    private final RetryRegistry retryRegistry;
    private final String defaultServerUrl;
    private final String fallbackServerUrl;
    private final int maxRetryAttempts;
    private final long retryDelayMs;
    private final Map<String, PaymentCategoryStats> paymentCategoryStats;

    // Using RestTemplate for HTTP requests
    private RestTemplate restTemplate;

    // Protected method to get the RestTemplate (for testing)
    protected RestTemplate getRestTemplate() {
        return restTemplate;
    }

    // Track when the default server will be available again
    private volatile long defaultServerAvailableAt = 0;

    public ProccessPayment(
            @Value("${payment.server.default.url}") String defaultServerUrl,
            @Value("${payment.server.fallback.url}") String fallbackServerUrl,
            @Value("${payment.server.retry.max-attempts}") int maxRetryAttempts,
            @Value("${payment.server.retry.delay-ms}") long retryDelayMs, Map<String, PaymentCategoryStats> paymentCategoryStats) {
        this.restTemplate = new RestTemplate();
        this.defaultServerUrl = defaultServerUrl;
        this.fallbackServerUrl = fallbackServerUrl;
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryDelayMs = retryDelayMs;

        // Configure retry
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(maxRetryAttempts)
                .waitDuration(Duration.ofMillis(retryDelayMs))
                .retryExceptions(Exception.class)
                .build();
        this.retryRegistry = RetryRegistry.of(RetryConfig.ofDefaults());
        this.retryRegistry.addConfiguration(RETRY_NAME, retryConfig);
        this.paymentCategoryStats = paymentCategoryStats;
    }

    public ResponseEntity<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        // Check if default server is available
        long now = System.currentTimeMillis();
        String serverUsed = "default";
        if (defaultServerAvailableAt - now >= 1000) { // 1000 ms = 1 second
            logger.info("Default server not available yet, using fallback directly");
            return processWithFallbackServer(paymentRequest);
        }

        try {
            // Try with default server first with retry
            Retry retry = retryRegistry.retry(RETRY_NAME);
            Supplier<ResponseEntity<PaymentResponse>> retryableSupplier = Retry.decorateSupplier(
                    retry,
                    () -> sendToDefaultServer(paymentRequest)
            );

            return retryableSupplier.get();
        } catch (Exception e) {
            logger.error("Failed to process payment with default server after retries: {}", e.getMessage());
            // If default server fails after retries, use fallback
            return processWithFallbackServer(paymentRequest);
        }
    }

    private ResponseEntity<PaymentResponse> sendToDefaultServer(PaymentRequest paymentRequest) {
        try {
            logger.info("Sending payment request to default server: {}", paymentRequest.getCorrelationId());
            ResponseEntity<PaymentResponse> response = getRestTemplate().postForEntity(
                    defaultServerUrl,
                    paymentRequest,
                    PaymentResponse.class
            );

            logger.info("Payment processed successfully by default server");
            return response;
        } catch (HttpStatusCodeException e) {
            // Check for the header that indicates when the server will be available
            HttpHeaders headers = e.getResponseHeaders();
            if (headers != null && headers.containsKey(SERVER_AVAILABLE_HEADER)) {
                String availableInSeconds = headers.getFirst(SERVER_AVAILABLE_HEADER);
                if (availableInSeconds != null) {
                    try {
                        long seconds = Long.parseLong(availableInSeconds);
                        defaultServerAvailableAt = System.currentTimeMillis() + (seconds * 1000);
                        logger.info("Default server will be available in {} seconds", seconds);
                    } catch (NumberFormatException nfe) {
                        logger.warn("Invalid server available time: {}", availableInSeconds);
                    }
                }
            }
            throw e;
        }
    }

    private ResponseEntity<PaymentResponse> processWithFallbackServer(PaymentRequest paymentRequest) {
        logger.info("Sending payment request to fallback server: {}", paymentRequest.getCorrelationId());
        try {
            ResponseEntity<PaymentResponse> response = getRestTemplate().postForEntity(
                    fallbackServerUrl,
                    paymentRequest,
                    PaymentResponse.class
            );
            logger.info("Payment processed successfully by fallback server");
            return response;
        } catch (Exception e) {
            logger.error("Failed to process payment with fallback server: {}", e.getMessage());
            // If fallback also fails, return an error response
            PaymentResponse errorResponse = new PaymentResponse("Failed to process payment");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
