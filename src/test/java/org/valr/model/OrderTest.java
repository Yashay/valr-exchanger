package org.valr.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.enums.Side;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.NUMBER;
import static org.valr.TestHelper.createOrder;

class OrderTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidOrder() {
        Order order = createOrder(Side.BUY, NUMBER(500000), NUMBER(1));

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertTrue(violations.isEmpty(), "Order should be valid");
    }

    @Test
    void testNullSide() {
        Order order = createOrder(null, NUMBER(500000), NUMBER(1));

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidPrice() {
        Order order = createOrder(Side.BUY, NUMBER(0), NUMBER(1));

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertFalse(violations.isEmpty());
        assertEquals("Price must be greater than zero", violations.iterator().next().getMessage());
    }

    @Test
    void testInvalidQuantity() {
        Order order = createOrder(Side.BUY, NUMBER(50000), NUMBER(0));

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertFalse(violations.isEmpty());
        assertEquals("Quantity must be greater than zero", violations.iterator().next().getMessage());
    }

    @Test
    void testReduceQuantity() {
        Order order = createOrder(Side.BUY, NUMBER(50000), NUMBER(1));

        order.reduceQuantity(NUMBER(4));
        assertEquals(NUMBER(-3), order.getQuantity());

        order.reduceQuantity(NUMBER(6));
        assertEquals(NUMBER(-9), order.getQuantity());
    }

    @Test
    void testOrderComparison() {
        Order order1 = createOrder(Side.BUY, NUMBER(50000), NUMBER(1));
        Order order2 = createOrder(Side.BUY, NUMBER(50000), NUMBER(1));

        assertEquals(-1, order1.compareTo(order2));
        assertEquals(1, order2.compareTo(order1));

        Order order3 = createOrder(Side.BUY, NUMBER(50000), NUMBER(1));
        order3.setTimestamp(order1.getTimestamp());

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
