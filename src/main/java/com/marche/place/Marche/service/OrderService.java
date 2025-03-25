package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.OrderDto;
import com.marche.place.Marche.dto.OrderItemDto;
import com.marche.place.Marche.entity.Order;
import com.marche.place.Marche.entity.OrderItem;
import com.marche.place.Marche.entity.Product;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.OrderStatus;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.OrderRepository;
import com.marche.place.Marche.repository.ProductRepository;
import com.marche.place.Marche.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Crée une nouvelle commande
     */
    public OrderDto createOrder(OrderDto dto) {
        User customer = userRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());  // ✅ Correction : java.time.LocalDateTime
        order.setStatus(OrderStatus.PENDING);
        order.setCustomer(customer);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setOrder(order);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(product.getPrice());

            order.getItems().add(item);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return mapToDto(savedOrder);
    }

    /**
     * Récupère toutes les commandes
     */
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une commande par ID
     */
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToDto(order);
    }

    /**
     * Met à jour le statut d'une commande
     */
    public OrderDto updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return mapToDto(updatedOrder);
    }

    /**
     * Supprime une commande
     */
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        orderRepository.delete(order);
    }

    /**
     * Convertit une entité Order en OrderDto
     */
    private OrderDto mapToDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getOrderDate(),  // ✅ Correction : LocalDateTime est maintenant correct
                order.getTotalAmount(),
                order.getStatus(),
                order.getCustomer().getId(),
                order.getItems().stream().map(item ->
                        new OrderItemDto(
                                item.getProduct().getId(),  // ✅ Vérification que ce champ existe bien
                                item.getQuantity(),
                                item.getUnitPrice()
                        )).collect(Collectors.toList()),
                order.getCreatedAt()
        );
    }
}
