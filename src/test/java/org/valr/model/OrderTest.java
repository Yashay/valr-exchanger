package org.valr.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.NUMBER;

class OrderTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidOrder() {
        Order order = Order.builder()
                .side(Side.BUY)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertTrue(violations.isEmpty(), "Order should be valid");
    }

    @Test
    void testNullSide() {
        Order order = Order.builder()
                .side(null)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidPrice() {
        Order order = Order.builder()
                .side(Side.BUY)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(1))
                .price(NUMBER(0))
                .timeInForce(TimeInForce.GTC)
                .build();

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertFalse(violations.isEmpty());
        assertEquals("Price must be greater than zero", violations.iterator().next().getMessage());
    }

    @Test
    void testInvalidQuantity() {
        Order order = Order.builder()
                .side(Side.BUY)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(0))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertFalse(violations.isEmpty());
        assertEquals("Quantity must be greater than zero", violations.iterator().next().getMessage());
    }

    @Test
    void testReduceQuantity() {
        Order order = Order.builder()
                .price(NUMBER(50000))
                .quantity(NUMBER(1))
                .build();

        order.reduceQuantity(NUMBER(4));
        assertEquals(NUMBER(-3), order.getQuantity());

        order.reduceQuantity(NUMBER(6));
        assertEquals(NUMBER(-9), order.getQuantity());
    }

    @Test
    void testOrderComparison() {
        Order order1 = Order.builder()
                .side(Side.BUY)
                .price(NUMBER(50000))
                .quantity(NUMBER(1))
                .timestamp(Instant.ofEpochMilli(100000))
                .sequence(1L)
                .build();
        Order order2 = Order.builder()
                .side(Side.BUY)
                .price(NUMBER(50000))
                .quantity(NUMBER(1))
                .timestamp(Instant.ofEpochMilli(200000))
                .sequence(2L)
                .build();
        Order order3 = Order.builder()
                .side(Side.BUY)
                .price(NUMBER(50000))
                .quantity(NUMBER(1))
                .timestamp(Instant.ofEpochMilli(300000))
                .sequence(3L)
                .build();

        assertEquals(-1, order1.compareTo(order2));
        assertEquals(1, order2.compareTo(order1));
        assertEquals(-1, order1.compareTo(order3));
    }

    @Test
    void testDefaultConstructor() {
        Order order = new Order();

        assertNotNull(order.getOrderId());
        assertNull(order.getUserId());
        assertNull(order.getSide());
        assertNull(order.getTimeInForce());
        assertNull(order.getExchangePair());
        assertNull(order.getPrice());
        assertNull(order.getQuantity());
        assertNotNull(order.getTimestamp());
    }

}
