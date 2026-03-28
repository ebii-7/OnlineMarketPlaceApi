package et.edu.aau.onlinemarketplace.Client;

import et.edu.aau.onlinemarketplace.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Component
@RequiredArgsConstructor
public class PaymentGatewayClient {

    private final RestTemplate restTemplate;

    @Value("${payment.gateway.url}")
    private String paymentGatewayUrl;

    public PaymentService.PaymentResponse processPayment(PaymentService.PaymentRequest request) {
        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create HTTP entity
            HttpEntity<PaymentService.PaymentRequest> entity = new HttpEntity<>(request, headers);

            // Make POST request to external API
            ResponseEntity<PaymentService.PaymentResponse> response = restTemplate.exchange(
                    paymentGatewayUrl,
                    HttpMethod.POST,
                    entity,
                    PaymentService.PaymentResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Payment gateway returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // For simulation, return mock response
            PaymentService.PaymentResponse mockResponse = new PaymentService.PaymentResponse();
            mockResponse.setSuccess(true);
            mockResponse.setTransactionId("MOCK_" + System.currentTimeMillis());
            mockResponse.setMessage("Payment processed successfully (mock)");
            return mockResponse;
        }
    }
}