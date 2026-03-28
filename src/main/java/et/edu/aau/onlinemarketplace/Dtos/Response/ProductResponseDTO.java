package et.edu.aau.onlinemarketplace.Dtos.Response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private List<CategoryResponseDTO> categories;

    @Data
    public static class CategoryResponseDTO {
        private Long id;
        private String name;
        private String description;
    }
}
