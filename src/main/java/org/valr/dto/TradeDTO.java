package org.valr.dto;

import io.vertx.core.json.JsonObject;
import org.valr.model.Trade;

import java.time.Instant;

public class TradeDTO {
    private String id;
    private String price;
    private String quantity;
    private String currencyPair;
    private String takerSide;
    private Instant tradedAt;
    private String quoteVolume;

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