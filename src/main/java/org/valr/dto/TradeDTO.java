package org.valr.dto;

import io.vertx.core.json.JsonObject;
import org.valr.model.Trade;

import java.time.Instant;

public class TradeDTO {
    private final String id;
    private final String price;
    private final String quantity;
    private final String currencyPair;
    private final String takerSide;
    private final Instant tradedAt;
    private final String quoteVolume;

    public static TradeDTO from(Trade trade) {
        return new TradeDTO(trade);
    }

    public TradeDTO(Trade trade) {
        this.id = trade.getTradeId();
        this.price = trade.getPrice().toPlainString();
        this.quantity = trade.getQuantity().toPlainString();
        this.currencyPair = trade.getCurrenyExchangePair().name();
        this.takerSide = trade.getTakerSide().name();
        this.tradedAt = trade.getTradedAt();
        this.quoteVolume = trade.getQuoteVolume().toPlainString();
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("price", price)
                .put("quantity", quantity)
                .put("currencyPair", currencyPair)
                .put("takerSide", takerSide)
                .put("tradedAt", tradedAt)
                .put("quoteVolume", quoteVolume);
    }
}