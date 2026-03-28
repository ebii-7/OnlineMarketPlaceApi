package et.edu.aau.onlinemarketplace.Service;

import et.edu.aau.onlinemarketplace.Dtos.Request.OrderRequestDTO;
import et.edu.aau.onlinemarketplace.Dtos.Response.OrderResponseDTO;
import et.edu.aau.onlinemarketplace.Entity.Order;
import et.edu.aau.onlinemarketplace.Entity.OrderItem;
import et.edu.aau.onlinemarketplace.Entity.Product;
import et.edu.aau.onlinemarketplace.Entity.User;
import et.edu.aau.onlinemarketplace.Exception.InsufficientStockException;
import et.edu.aau.onlinemarketplace.Exception.ResourceNotFoundException;
import et.edu.aau.onlinemarketplace.Repository.OrderRepository;
import et.edu.aau.onlinemarketplace.Repository.UserRepository;
import et.edu.aau.onlinemarketplace.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

    public OrderResponseDTO createOrder(Long userId, OrderRequestDTO request) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Process each item
        for (OrderRequestDTO.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            // Check stock availability
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(product.getName(), itemRequest.getQuantity(), product.getStockQuantity());
            }

            // Calculate subtotal
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            items.add(orderItem);

            // Update stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        // Process payment via external API
        String paymentReference = paymentService.processPayment(totalAmount, request.getPaymentMethod());
        order.setPaymentReference(paymentReference);
        order.setStatus("PROCESSING");

        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return convertToDTO(order);
    }

    public List<OrderResponseDTO> getOrdersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return orderRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    private OrderResponseDTO convertToDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentReference(order.getPaymentReference());

        // User summary
        OrderResponseDTO.UserSummaryDTO userSummary = new OrderResponseDTO.UserSummaryDTO();
        userSummary.setId(order.getUser().getId());
        userSummary.setUsername(order.getUser().getUsername());
        userSummary.setEmail(order.getUser().getEmail());
        dto.setUser(userSummary);

        // Order items
        List<OrderResponseDTO.OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(item -> {
                    OrderResponseDTO.OrderItemResponseDTO itemDTO = new OrderResponseDTO.OrderItemResponseDTO();
                    itemDTO.setProductId(item.getProductId());
                    itemDTO.setProductName(item.getProductName());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getPrice());
                    itemDTO.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    return itemDTO;
                })
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }
}
