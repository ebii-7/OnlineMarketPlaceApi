package et.edu.aau.onlinemarketplace.Dtos.Response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private String paymentReference;
    private UserSummaryDTO user;
    private List<OrderItemResponseDTO> items;

    @Data
    public static class UserSummaryDTO {
        private Long id;
        private String username;
        private String email;
    }

    @Data
    public static class OrderItemResponseDTO {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}
