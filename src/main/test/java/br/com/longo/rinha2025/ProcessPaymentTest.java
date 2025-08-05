package br.com.longo.rinha2025;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProcessPaymentTest {

    @Mock
    private RestTemplate restTemplate;

    private ProccessPayment proccessPayment;

    private PaymentRequest paymentRequest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Create a test instance with our mocked RestTemplate
        proccessPayment = new ProccessPayment(
                "http://default-server/process-payment",
                "http://fallback-server/process-payment",
                2,
                500
        ) {
            @Override
            protected RestTemplate getRestTemplate() {
                return restTemplate;
            }
        };

        // Create a sample payment request
        paymentRequest = new PaymentRequest(
                UUID.randomUUID().toString(),
                19.90,
                Instant.parse("2025-07-15T12:34:56.000Z")
        );
    }

    @Test
    public void testProcessPayment_DefaultServerSuccess() {
        // Arrange
        PaymentResponse expectedResponse = new PaymentResponse("payment processed successfully");
        when(restTemplate.postForEntity(
                eq("http://default-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        )).thenReturn(ResponseEntity.ok(expectedResponse));

        // Act
        ResponseEntity<PaymentResponse> response = proccessPayment.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("payment processed successfully", response.getBody().getMessage());

        // Verify default server was called once
        verify(restTemplate, times(1)).postForEntity(
                eq("http://default-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        );
    }

    @Test
    public void testProcessPayment_DefaultServerFailsFallbackSuccess() {
        // Arrange
        // Default server fails
        when(restTemplate.postForEntity(
                eq("http://default-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Fallback server succeeds
        PaymentResponse expectedResponse = new PaymentResponse("payment processed successfully");
        when(restTemplate.postForEntity(
                eq("http://fallback-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        )).thenReturn(ResponseEntity.ok(expectedResponse));

        // Act
        ResponseEntity<PaymentResponse> response = proccessPayment.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("payment processed successfully", response.getBody().getMessage());

        // Verify default server was called the maximum number of retry attempts (3 total: 1 initial + 2 retries)
        verify(restTemplate, times(3)).postForEntity(
                eq("http://default-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        );

        // Verify fallback server was called once
        verify(restTemplate, times(1)).postForEntity(
                eq("http://fallback-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        );
    }

    @Test
    public void testProcessPayment_ServerAvailableHeader() {
        // Arrange
        // Create headers with the server available time
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Server-Available-In", "30");

        // Default server fails with headers
        HttpServerErrorException exception = HttpServerErrorException
                .create(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", headers, null, null);

        when(restTemplate.postForEntity(
                eq("http://default-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        )).thenThrow(exception);

        // Fallback server succeeds
        PaymentResponse expectedResponse = new PaymentResponse("payment processed successfully");
        when(restTemplate.postForEntity(
                eq("http://fallback-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        )).thenReturn(ResponseEntity.ok(expectedResponse));

        // Act
        ResponseEntity<PaymentResponse> response = proccessPayment.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("payment processed successfully", response.getBody().getMessage());

        // Verify fallback server was called
        verify(restTemplate, times(1)).postForEntity(
                eq("http://fallback-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        );

        // Make a second request - should go directly to fallback
        proccessPayment.processPayment(paymentRequest);

        // Verify default server was not called again (still at 3 calls from first request)
        verify(restTemplate, times(3)).postForEntity(
                eq("http://default-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        );

        // Verify fallback server was called again (now 2 times total)
        verify(restTemplate, times(2)).postForEntity(
                eq("http://fallback-server/process-payment"),
                any(PaymentRequest.class),
                eq(PaymentResponse.class)
        );
    }
}
