package org.valr.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.enums.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.NUMBER;

class DepositTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidDeposit() {
        Deposit deposit = new Deposit(Currency.BTC, new BigDecimal("0.5"), Instant.now());

        Set<ConstraintViolation<Deposit>> violations = validator.validate(deposit);
        assertTrue(violations.isEmpty(), "Deposit should be valid");
    }

    @Test
    void testNullCurrency() {
        Deposit deposit = new Deposit(null, new BigDecimal("0.5"), Instant.now());

        Set<ConstraintViolation<Deposit>> violations = validator.validate(deposit);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidAmount() {
        Deposit deposit = new Deposit(Currency.ZAR, new BigDecimal("0.0000000001"), Instant.now());

        Set<ConstraintViolation<Deposit>> violations = validator.validate(deposit);
        assertFalse(violations.isEmpty());
        assertEquals("Amount must be greater than zero", violations.iterator().next().getMessage());
    }

    @Test
    void testZeroAmount() {
        Deposit deposit = new Deposit(Currency.BTC, NUMBER(0), Instant.now());

        Set<ConstraintViolation<Deposit>> violations = validator.validate(deposit);
        assertFalse(violations.isEmpty());
        assertEquals("Amount must be greater than zero", violations.iterator().next().getMessage());
    }

    @Test
    void testDefaultConstructor() {
        Deposit deposit = new Deposit();

        assertNull(deposit.getCurrency());
        assertEquals(NUMBER(0), deposit.getAmount());
        assertNotNull(deposit.getInitiated());
    }

}
