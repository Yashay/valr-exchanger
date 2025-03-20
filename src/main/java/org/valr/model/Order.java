package org.valr.model;

import lombok.*;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;

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

    private TimeInForce timeInForce;
    private Side side;
    private ExchangePair exchangePair;

    private BigDecimal price;
    private BigDecimal initialQuantity;
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

        int sequenceComparison = this.sequence.compareTo(other.sequence);
        if (sequenceComparison != 0) {
            return sequenceComparison;
        }

        int quantityComparison = this.quantity.compareTo(other.quantity);
        if (quantityComparison != 0) {
            return quantityComparison;
        }

        return 0;
    }
}