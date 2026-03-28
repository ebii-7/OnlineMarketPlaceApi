package et.edu.aau.onlinemarketplace.Entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Embedded class for Order Items
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
  public class OrderItem {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
