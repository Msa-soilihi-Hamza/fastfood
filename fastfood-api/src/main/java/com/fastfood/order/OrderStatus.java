package com.fastfood.order;

/**
 * Cycle de vie d'une commande :
 * RECEIVED → PREPARING → READY → COMPLETED
 * Annulation possible tant que la commande n'est pas prête.
 */
public enum OrderStatus {
    RECEIVED,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED;

    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case RECEIVED -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == READY || next == CANCELLED;
            case READY -> next == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    public boolean isActive() {
        return this == RECEIVED || this == PREPARING || this == READY;
    }
}
