package org.valr.model;

import io.vertx.core.json.JsonObject;
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
    private String tradeId;

    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal quoteVolume;

    private Side takerSide;
    private Side makerSide;

    private String makerId;
    private String takerId;

    private String makerOrderId;
    private String takerOrderId;

    private ExchangePair currenyExchangePair;
    
    private Instant tradedAt;

    public Trade(Order takerOrder, Order makerOrder, BigDecimal price, BigDecimal quantity) {
        tradeId = UUID.randomUUID().toString();
        tradedAt = Instant.now();
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

    public JsonObject toJson() {
        JsonObject tradeJson = new JsonObject();
        tradeJson.put("price", getPrice());
        tradeJson.put("quantity", getQuantity());
        tradeJson.put("currencyPair", getCurrenyExchangePair());
        tradeJson.put("tradedAt", getTradedAt());
        tradeJson.put("takerSide", getTakerSide());
        tradeJson.put("id", getTradeId());
        tradeJson.put("quoteVolume", getQuoteVolume());
        return tradeJson;
    }

    @Override
    public int compareTo(Trade otherTrade) {
        return tradedAt.compareTo(otherTrade.tradedAt);
    }
}