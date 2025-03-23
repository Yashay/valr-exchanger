package org.valr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Getter
@ToString
public class Trade implements Comparable<Trade> {
    private final String tradeId;

    private final BigDecimal price;
    private final BigDecimal quantity;
    private final BigDecimal quoteVolume;

    private final Side takerSide;
    private final Side makerSide;

    private final String makerId;
    private final String takerId;

    private final String makerOrderId;
    private final String takerOrderId;

    private final ExchangePair currenyExchangePair;
    
    private final Instant tradedAt;

    public Trade(Order takerOrder, Order makerOrder, BigDecimal price, BigDecimal quantity, Instant tradedAt) {
        tradeId = UUID.randomUUID().toString();
        this.tradedAt = tradedAt;
        this.price = price;
        this.quantity = quantity;
        this.quoteVolume = price.multiply(quantity);
        this.takerId = takerOrder.getUserId();
        this.makerId = makerOrder.getUserId();
        this.takerOrderId = takerOrder.getOrderId();
        this.makerOrderId = makerOrder.getOrderId();
        this.takerSide = takerOrder.getSide();
        this.makerSide = makerOrder.getSide();
        this.currenyExchangePair = takerOrder.getExchangePair();
    }

    public Trade(Order takerOrder, Order makerOrder, BigDecimal price, BigDecimal quantity) {
        this(takerOrder, makerOrder, price, quantity, Instant.now());
    }

    @Override
    public int compareTo(Trade otherTrade) {
        int timeComparison = this.tradedAt.compareTo(otherTrade.tradedAt);
        if (timeComparison != 0) return timeComparison;

        int tradeIdComparison = this.tradeId.compareTo(otherTrade.tradeId);
        if (tradeIdComparison != 0) return tradeIdComparison;

        int priceComparison = otherTrade.price.compareTo(this.price);
        if (priceComparison != 0) return priceComparison;

        int quantityComparison = otherTrade.quantity.compareTo(this.quantity);
        if (quantityComparison != 0) return quantityComparison;

        return 0;
    }
}