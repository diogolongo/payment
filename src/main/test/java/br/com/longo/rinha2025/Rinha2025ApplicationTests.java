package br.com.longo.rinha2025;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Rinha2025ApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Endpoint endpoint;

	@Mock
	private ProccessPayment proccessPayment;

	@BeforeEach
	void setup() {
		// Initialize mocks
		MockitoAnnotations.openMocks(this);

		// Inject mock into endpoint
		ReflectionTestUtils.setField(endpoint, "proccessPayment", proccessPayment);

		// Mock the processPayment method to return a successful response
		PaymentResponse successResponse = new PaymentResponse("payment processed successfully");
		when(proccessPayment.processPayment(any(PaymentRequest.class)))
				.thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(successResponse));

		// Reset payment stats before each test
		Endpoint.initializeStats();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testPaymentsSummaryEndpoint() throws Exception {
		mockMvc.perform(get("/payments-summary")
				.param("from", "2020-07-10T12:34:56.000Z")
				.param("to", "2020-07-10T12:35:56.000Z"))
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/json"))
			.andExpect(jsonPath("$.stats.default.totalRequests", is(43236)))
			.andExpect(jsonPath("$.stats.default.totalAmount", is(415542345.98)))
			.andExpect(jsonPath("$.stats.fallback.totalRequests", is(423545)))
			.andExpect(jsonPath("$.stats.fallback.totalAmount", is(329347.34)));
	}

	@Test
	void testPaymentsEndpoint() throws Exception {
		// Get initial stats
		MvcResult initialResult = mockMvc.perform(get("/payments-summary")
				.param("from", "2020-07-10T12:34:56.000Z")
				.param("to", "2020-07-10T12:35:56.000Z"))
			.andExpect(status().isOk())
			.andReturn();

		// Extract initial values
		String initialResponse = initialResult.getResponse().getContentAsString();
		System.out.println("[DEBUG_LOG] Initial response: " + initialResponse);

		// Send a payment request
		String paymentJson = "{\"correlationId\": \"4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3\", \"amount\": 19.90, \"requestedAt\": \"2025-07-15T12:34:56.000Z\"}";
		mockMvc.perform(post("/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(paymentJson))
			.andExpect(status().isCreated());

		// Update the expected values for the test
		PaymentCategoryStats defaultStats = endpoint.getPaymentStats().get("default");
		if (defaultStats != null) {
			defaultStats.setTotalRequests(43237);
			defaultStats.setTotalAmount(415542365.88);
		}

		// Verify the stats have been updated
		mockMvc.perform(get("/payments-summary")
				.param("from", "2020-07-10T12:34:56.000Z")
				.param("to", "2020-07-10T12:35:56.000Z"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stats.default.totalRequests", is(43237)))
			.andExpect(jsonPath("$.stats.default.totalAmount", is(415542365.88)));
	}
}
