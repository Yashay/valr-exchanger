package org.valr.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;
import org.valr.util.enums.ValidEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order implements Comparable<Order> {
    private String orderId = UUID.randomUUID().toString();

    private String userId;
    private Long sequence;

    @NotNull(message = "TimeInForce is required")
    private TimeInForce timeInForce;

    @NotNull(message = "Side is required")
    @ValidEnum(enumClass = Side.class, message = "Must match a side")
    private Side side;

    @NotNull(message = "ExchangePair currency is required")
    @ValidEnum(enumClass = ExchangePair.class, message = "Must match a exchange currency pair")
    private ExchangePair exchangePair;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.000001", message = "Price must be greater than zero")
    private BigDecimal price;

    private BigDecimal initialQuantity;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.000001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private Instant timestamp = Instant.now();

    public void reduceQuantity(BigDecimal quantity) {
        this.quantity = this.quantity.subtract(quantity);
    }

    @Override
    public int compareTo(Order other) {
        int priceComparison = this.price.compareTo(other.price);
        if (priceComparison != 0) {
            return priceComparison;
        }

        int timeComparison = this.timestamp.compareTo(other.timestamp);
        if (timeComparison != 0) {
            return timeComparison;
        }

        int quantityComparison = this.quantity.compareTo(other.quantity);
        if (quantityComparison != 0) {
            return quantityComparison;
        }

        int sequenceComparison = this.sequence.compareTo(other.sequence);
        return sequenceComparison;
    }

    //TODO perhaps json annotated constructor?
}