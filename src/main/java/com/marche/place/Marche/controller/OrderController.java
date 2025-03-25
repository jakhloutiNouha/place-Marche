package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.OrderDto;
import com.marche.place.Marche.enums.OrderStatus;
import com.marche.place.Marche.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Endpoints pour la gestion des commandes")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Créer une commande", description = "Ajoute une nouvelle commande.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(@RequestBody OrderDto orderDto) {
        return orderService.createOrder(orderDto);
    }

    @Operation(summary = "Lister toutes les commandes", description = "Retourne la liste des commandes.")
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Operation(summary = "Obtenir une commande", description = "Retourne une commande spécifique par ID.")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Mettre à jour le statut d'une commande", description = "Modifie le statut d'une commande.")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @Operation(summary = "Supprimer une commande", description = "Supprime une commande spécifique.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}
