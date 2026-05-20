package com.snack24.order.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void OrderTest() {
        // given
        Order order = Order.place(1L, 1L, 1L, BigDecimal.valueOf(1000L));

        // when
        order.confirm();

        // then
        Assertions.assertThatThrownBy(() -> order.cancel("test"))
                .isInstanceOf(IllegalStateException.class);
    }
}