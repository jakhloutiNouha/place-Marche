package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.OrderDto;
import com.marche.place.Marche.dto.OrderItemDto;
import com.marche.place.Marche.entity.*;
import com.marche.place.Marche.enums.OrderStatus;
import com.marche.place.Marche.enums.UserRole;
import com.marche.place.Marche.repository.OrderRepository;
import com.marche.place.Marche.repository.ProductRepository;
import com.marche.place.Marche.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtenir toutes les commandes
     */
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir une commande par son ID
     */
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));
        return convertToDto(order);
    }

    /**
     * Obtenir les commandes d'un client
     */
    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour le statut d'une commande
     */
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        return convertToDto(updatedOrder);
    }

    /**
     * Créer une nouvelle commande
     */
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        User customer = userRepository.findById(orderDto.getCustomerId())
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());

        // Enregistrer les informations de livraison
        if (orderDto.getShippingAddress() != null) {
            order.setShippingAddress(orderDto.getShippingAddress());
        }
        if (orderDto.getShippingCity() != null) {
            order.setShippingCity(orderDto.getShippingCity());
        }
        if (orderDto.getShippingPostalCode() != null) {
            order.setShippingPostalCode(orderDto.getShippingPostalCode());
        }
        if (orderDto.getContactEmail() != null) {
            order.setContactEmail(orderDto.getContactEmail());
        }
        if (orderDto.getContactPhone() != null) {
            order.setContactPhone(orderDto.getContactPhone());
        }
        if (orderDto.getFirstName() != null) {
            order.setFirstName(orderDto.getFirstName());
        }
        if (orderDto.getLastName() != null) {
            order.setLastName(orderDto.getLastName());
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDto itemDto : orderDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Produit non trouvé"));

            if (product.getStock() < itemDto.getQuantity()) {
                throw new IllegalStateException("Stock insuffisant pour le produit: " + product.getTitle());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);

            product.setStock(product.getStock() - itemDto.getQuantity());
            productRepository.save(product);
        }

        order.setItems(orderItems);
        order.calculateTotalAmount();

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    /**
     * Ajouter un article à une commande
     */
    @Transactional
    public OrderDto addItemToOrder(Long orderId, OrderItemDto itemDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));

        Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Produit non trouvé"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(itemDto.getQuantity());
        item.setUnitPrice(itemDto.getUnitPrice());

        order.addItem(item);
        order.calculateTotalAmount();

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    /**
     * Annuler une commande
     */
    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Impossible d'annuler une commande déjà livrée");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        return convertToDto(cancelledOrder);
    }


    /**
     * Obtenir les commandes d'un vendeur par son email
     */
    public List<OrderDto> getOrdersByVendorEmail(String email) {
        User vendor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé"));

        if (vendor.getRole() != UserRole.Vendor) {
            throw new IllegalStateException("L'utilisateur n'est pas un vendeur");
        }

        return getOrdersByVendorId(vendor.getId());
    }

    /**
     * Obtenir une commande spécifique pour un vendeur
     */
    public OrderDto getVendorOrderById(Long orderId, String email) {
        User vendor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé"));

        if (vendor.getRole() != UserRole.Vendor) {
            throw new IllegalStateException("L'utilisateur n'est pas un vendeur");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));

        // Vérifier que la commande contient au moins un produit du vendeur
        boolean hasVendorProduct = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getVendorId().equals(vendor.getId()));

        if (!hasVendorProduct) {
            throw new NoSuchElementException("Commande non autorisée pour ce vendeur");
        }

        return convertToDto(order);
    }
    /**
     * Obtenir les commandes d'un vendeur par son ID
     */
    public List<OrderDto> getOrdersByVendorId(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé"));

        if (vendor.getRole() != UserRole.Vendor) {
            throw new IllegalStateException("L'utilisateur n'est pas un vendeur");
        }

        // Récupérer les produits du vendeur
        List<Product> vendorProducts = productRepository.findByVendorId(vendorId);

        if (vendorProducts.isEmpty()) {
            return new ArrayList<>();
        }

        // Récupérer les IDs des produits
        List<Long> productIds = vendorProducts.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        // Trouver les commandes contenant ces produits
        List<Order> vendorOrders = orderRepository.findOrdersContainingProducts(productIds);

        return vendorOrders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    /**
     * Accepter une commande (pour vendeur)
     */
    @Transactional
    public OrderDto acceptOrder(Long orderId, String email, String notes) {
        User vendor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé"));

        if (vendor.getRole() != UserRole.Vendor) {
            throw new IllegalStateException("L'utilisateur n'est pas un vendeur");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));

        // Vérifier que la commande contient au moins un produit du vendeur
        boolean hasVendorProduct = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getVendorId().equals(vendor.getId()));

        if (!hasVendorProduct) {
            throw new NoSuchElementException("Commande non autorisée pour ce vendeur");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cette commande ne peut pas être acceptée car son statut est: " + order.getStatus());
        }

        // Mettre à jour le statut de la commande
        order.setStatus(OrderStatus.CONFIRMED);

        // Si vous avez un champ pour les notes dans votre entité Order
        // if (notes != null && !notes.isEmpty()) {
        //     order.setNotes(notes);
        // }

        Order updatedOrder = orderRepository.save(order);
        return convertToDto(updatedOrder);
    }

    /**
     * Refuser une commande (pour vendeur)
     */
    @Transactional
    public OrderDto rejectOrder(Long orderId, String email, String reason) {
        User vendor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé"));

        if (vendor.getRole() != UserRole.Vendor) {
            throw new IllegalStateException("L'utilisateur n'est pas un vendeur");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));

        // Vérifier que la commande contient au moins un produit du vendeur
        boolean hasVendorProduct = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getVendorId().equals(vendor.getId()));

        if (!hasVendorProduct) {
            throw new NoSuchElementException("Commande non autorisée pour ce vendeur");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cette commande ne peut pas être refusée car son statut est: " + order.getStatus());
        }

        // Restaurer le stock pour les articles du vendeur
        for (OrderItem item : order.getItems()) {
            if (item.getProduct().getVendorId().equals(vendor.getId())) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        // Mettre à jour le statut de la commande
        order.setStatus(OrderStatus.CANCELLED);

        // Si vous avez un champ pour les notes dans votre entité Order
        // if (reason != null && !reason.isEmpty()) {
        //     order.setNotes("Refusée par le vendeur: " + reason);
        // }

        Order updatedOrder = orderRepository.save(order);
        return convertToDto(updatedOrder);
    }

    /**
     * Marquer une commande comme expédiée (pour vendeur)
     */
    @Transactional
    public OrderDto shipOrder(Long orderId, String email, String trackingNumber) {
        User vendor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé"));

        if (vendor.getRole() != UserRole.Vendor) {
            throw new IllegalStateException("L'utilisateur n'est pas un vendeur");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));

        // Vérifier que la commande contient au moins un produit du vendeur
        boolean hasVendorProduct = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getVendorId().equals(vendor.getId()));

        if (!hasVendorProduct) {
            throw new NoSuchElementException("Commande non autorisée pour ce vendeur");
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cette commande ne peut pas être expédiée car son statut est: " + order.getStatus());
        }

        // Mettre à jour le statut de la commande
        order.setStatus(OrderStatus.SHIPPED);

        // Si vous avez un champ pour le numéro de suivi dans votre entité Order
        // if (trackingNumber != null && !trackingNumber.isEmpty()) {
        //     order.setTrackingNumber(trackingNumber);
        // }

        Order updatedOrder = orderRepository.save(order);
        return convertToDto(updatedOrder);
    }

    /**
     * Convertir une entité Order en DTO
     */
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCreatedAt(order.getCreatedAt());

        // Ajouter les infos de livraison si elles existent
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingCity(order.getShippingCity());
        dto.setShippingPostalCode(order.getShippingPostalCode());
        dto.setContactEmail(order.getContactEmail());
        dto.setContactPhone(order.getContactPhone());
        dto.setFirstName(order.getFirstName());
        dto.setLastName(order.getLastName());

        dto.setItems(order.getItems().stream()
                .map(this::convertItemToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Convertir une entité OrderItem en DTO
     */
    private OrderItemDto convertItemToDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(item.getProduct().getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setProductName(item.getProduct().getTitle());
        dto.setImageUrl(item.getProduct().getImageUrl());
        return dto;
    }
}