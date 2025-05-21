package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.OrderDto;
import com.marche.place.Marche.dto.OrderItemDto;
import com.marche.place.Marche.enums.OrderStatus;
import com.marche.place.Marche.enums.UserRole;
import com.marche.place.Marche.service.OrderService;
import com.marche.place.Marche.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Routes générales pour toutes les commandes

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addItemToOrder(
            @PathVariable Long orderId,
            @RequestBody OrderItemDto itemDto) {
        try {
            OrderDto updatedOrder = orderService.addItemToOrder(orderId, itemDto);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de l'ajout: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto) {
        try {
            OrderDto createdOrder = orderService.createOrder(orderDto);
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la création: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            OrderDto order = orderService.getOrderById(id);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Commande non trouvée", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Une erreur est survenue: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<OrderDto> orders = orderService.getOrdersByCustomerId(customerId);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        try {
            OrderDto updatedOrder = orderService.updateOrderStatus(id, status);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Commande non trouvée", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Une erreur est survenue: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            OrderDto cancelledOrder = orderService.cancelOrder(id);
            return new ResponseEntity<>(cancelledOrder, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Commande non trouvée", HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Une erreur est survenue: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Routes spécifiques aux vendeurs

    @GetMapping("/vendor")
    public ResponseEntity<?> getVendorOrders() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            List<OrderDto> orders = orderService.getOrdersByVendorEmail(email);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la récupération des commandes: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/vendor/{id}")
    public ResponseEntity<?> getVendorOrderById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            OrderDto order = orderService.getVendorOrderById(id, email);
            return ResponseEntity.ok(order);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Commande non trouvée ou non autorisée", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la récupération de la commande: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/vendor/{id}/accept")
    public ResponseEntity<?> acceptOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            String notes = null;
            if (payload != null && payload.containsKey("notes")) {
                notes = payload.get("notes");
            }

            OrderDto order = orderService.acceptOrder(id, email, notes);
            return ResponseEntity.ok(order);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de l'acceptation de la commande: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/vendor/{id}/reject")
    public ResponseEntity<?> rejectOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            String reason = payload.get("reason");
            if (reason == null || reason.isEmpty()) {
                return new ResponseEntity<>("Un motif de refus est requis", HttpStatus.BAD_REQUEST);
            }

            OrderDto order = orderService.rejectOrder(id, email, reason);
            return ResponseEntity.ok(order);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors du refus de la commande: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/vendor/{id}/ship")
    public ResponseEntity<?> shipOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            String trackingNumber = payload.get("trackingNumber");
            if (trackingNumber == null || trackingNumber.isEmpty()) {
                return new ResponseEntity<>("Un numéro de suivi est requis", HttpStatus.BAD_REQUEST);
            }

            OrderDto order = orderService.shipOrder(id, email, trackingNumber);
            return ResponseEntity.ok(order);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de l'expédition de la commande: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}