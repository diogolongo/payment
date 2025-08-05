package java.br.com.longo.rinha2025;

public class PaymentCategoryStats {
    private long totalRequests = 0;
    private double totalAmount= 0;

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public void incrementStats(double amount) {
        this.totalRequests++;
        this.totalAmount += amount;
    }
}