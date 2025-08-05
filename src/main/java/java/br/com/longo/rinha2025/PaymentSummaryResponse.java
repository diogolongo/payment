package java.br.com.longo.rinha2025;

import java.util.Map;

/**
 * Response object for payment summary
 */
public class PaymentSummaryResponse {
    private Map<String, PaymentCategoryStats> stats;

    public PaymentSummaryResponse() {
    }

    public PaymentSummaryResponse(Map<String, PaymentCategoryStats> stats) {
        this.stats = stats;
    }

    public Map<String, PaymentCategoryStats> getStats() {
        return stats;
    }

    public void setStats(Map<String, PaymentCategoryStats> stats) {
        this.stats = stats;
    }
}