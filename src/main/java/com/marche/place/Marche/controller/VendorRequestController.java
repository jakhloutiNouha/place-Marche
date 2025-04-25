// VendorRequestController.java
package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.VendorRequestDto;
import com.marche.place.Marche.service.VendorRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Vendor Requests", description = "Gestion des demandes de vendeurs")
public class VendorRequestController {

    private final VendorRequestService vendorRequestService;

    @Operation(summary = "Soumettre une demande de vendeur", description = "Permet à un utilisateur de soumettre une demande pour devenir vendeur")
    @PostMapping
    public ResponseEntity<VendorRequestDto> submitRequest(@RequestBody VendorRequestDto requestDto) {
        return new ResponseEntity<>(vendorRequestService.submitRequest(requestDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Obtenir toutes les demandes", description = "Récupère toutes les demandes de vendeurs")
    @GetMapping
    public ResponseEntity<List<VendorRequestDto>> getAllRequests() {
        return ResponseEntity.ok(vendorRequestService.getAllRequests());
    }

    @Operation(summary = "Obtenir les demandes en attente", description = "Récupère les demandes de vendeurs en attente de validation")
    @GetMapping("/pending")
    public ResponseEntity<List<VendorRequestDto>> getPendingRequests() {
        return ResponseEntity.ok(vendorRequestService.getPendingRequests());
    }

    @Operation(summary = "Approuver une demande", description = "Approuve une demande de vendeur et crée un compte utilisateur")
    @PostMapping("/{id}/approve")
    public ResponseEntity<VendorRequestDto> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(vendorRequestService.approveRequest(id));
    }

    @Operation(summary = "Rejeter une demande", description = "Rejette une demande de vendeur avec un motif")
    @PostMapping("/{id}/reject")
    public ResponseEntity<VendorRequestDto> rejectRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return ResponseEntity.ok(vendorRequestService.rejectRequest(id, reason));
    }
}