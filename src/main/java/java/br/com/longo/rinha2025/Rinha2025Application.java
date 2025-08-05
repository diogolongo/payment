package java.br.com.longo.rinha2025;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.zeromq.ZeroMqProxy;
import org.zeromq.ZContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class Rinha2025Application {
	private static final Map<String, PaymentCategoryStats> paymentStats = new ConcurrentHashMap<>();
	private static final ZContext CONTEXT = new ZContext();

	public static void main(String[] args) {
		SpringApplication.run(Rinha2025Application.class, args);
	}

	@Bean
	public Map<String, PaymentCategoryStats> paymentCategoryStats() {
		paymentStats.clear();

		// Initialize with default values
		PaymentCategoryStats defaultStats = new PaymentCategoryStats();
		paymentStats.put("default", defaultStats);

		PaymentCategoryStats fallbackStats = new PaymentCategoryStats();
		paymentStats.put("fallback", fallbackStats);
		return paymentStats;
	}
	@Bean
	ZeroMqProxy zeroMqProxy() {
		ZeroMqProxy proxy = new ZeroMqProxy(CONTEXT, ZeroMqProxy.Type.SUB_PUB);
		proxy.setExposeCaptureSocket(true);
		proxy.setFrontendPort(6001);
		proxy.setBackendPort(6002);
		return proxy;
	}
}
