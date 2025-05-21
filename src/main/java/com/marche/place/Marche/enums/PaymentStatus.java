package com.marche.place.Marche.enums;

public enum PaymentStatus {
    PENDING("En attente"),
    COMPLETED("Complété"),
    FAILED("Échoué"),
    REFUNDED("Remboursé"),
    CANCELLED("Annulé");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}