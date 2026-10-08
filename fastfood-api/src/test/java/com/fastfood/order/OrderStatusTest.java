package com.fastfood.order;

import org.junit.jupiter.api.Test;

import static com.fastfood.order.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void followsTheNormalFlow() {
        assertThat(RECEIVED.canMoveTo(PREPARING)).isTrue();
        assertThat(PREPARING.canMoveTo(READY)).isTrue();
        assertThat(READY.canMoveTo(COMPLETED)).isTrue();
    }

    @Test
    void cannotSkipSteps() {
        assertThat(RECEIVED.canMoveTo(READY)).isFalse();
        assertThat(RECEIVED.canMoveTo(COMPLETED)).isFalse();
        assertThat(PREPARING.canMoveTo(COMPLETED)).isFalse();
    }

    @Test
    void canBeCancelledOnlyBeforeBeingReady() {
        assertThat(RECEIVED.canMoveTo(CANCELLED)).isTrue();
        assertThat(PREPARING.canMoveTo(CANCELLED)).isTrue();
        assertThat(READY.canMoveTo(CANCELLED)).isFalse();
    }

    @Test
    void finalStatusesAreFinal() {
        for (OrderStatus next : values()) {
            assertThat(COMPLETED.canMoveTo(next)).isFalse();
            assertThat(CANCELLED.canMoveTo(next)).isFalse();
        }
    }
}
