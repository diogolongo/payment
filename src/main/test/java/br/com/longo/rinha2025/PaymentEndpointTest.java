package br.com.longo.rinha2025;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PaymentEndpointTest {

    @Mock
    private ProccessPayment proccessPayment;

    @InjectMocks
    private Endpoint endpoint;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        Endpoint.initializeStats();
    }

    @Test
    public void testProcessPayment_Success() {
        // Arrange
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID().toString(),
                19.90,
                Instant.parse("2025-07-15T12:34:56.000Z")
        );

        PaymentResponse expectedResponse = new PaymentResponse("payment processed successfully");
        when(proccessPayment.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        // Act
        ResponseEntity<PaymentResponse> response = endpoint.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("payment processed successfully", response.getBody().getMessage());
    }

    @Test
    public void testProcessPayment_NullResponseBody() {
        // Arrange
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID().toString(),
                19.90,
                Instant.parse("2025-07-15T12:34:56.000Z")
        );

        when(proccessPayment.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<PaymentResponse> response = endpoint.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("payment processed successfully", response.getBody().getMessage());
    }
}
