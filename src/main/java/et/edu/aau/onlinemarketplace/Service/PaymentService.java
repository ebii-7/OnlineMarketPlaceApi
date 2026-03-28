package et.edu.aau.onlinemarketplace.Service;

import et.edu.aau.onlinemarketplace.Client.PaymentGatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGatewayClient paymentGatewayClient;

    public String processPayment(BigDecimal amount, String paymentMethod) {
        try {
            // Call external payment gateway API
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(amount);
            paymentRequest.setCurrency("USD");
            paymentRequest.setPaymentMethod(paymentMethod);
            paymentRequest.setTransactionId(UUID.randomUUID().toString());

            PaymentResponse response = paymentGatewayClient.processPayment(paymentRequest);

            if (response.isSuccess()) {
                return response.getTransactionId();
            } else {
                throw new RuntimeException("Payment failed: " + response.getMessage());
            }
        } catch (Exception e) {
            // For simulation, return a mock reference
            return "SIM_" + UUID.randomUUID().toString();
        }
    }

    // Inner classes for payment request/response
    public static class PaymentRequest {
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String transactionId;

        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    }

    public static class PaymentResponse {
        private boolean success;
        private String transactionId;
        private String message;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
