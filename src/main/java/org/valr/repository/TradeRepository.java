package org.valr.repository;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.valr.model.Trade;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class TradeRepository {
    private final ConcurrentSkipListMap<String, ConcurrentSkipListSet<Trade>> tradeHistoryByUserMap = new ConcurrentSkipListMap<>();

    public void addTrade(Trade trade) {
        tradeHistoryByUserMap.computeIfAbsent(trade.getTakerId(), k -> new ConcurrentSkipListSet<>()).add(trade);
        tradeHistoryByUserMap.computeIfAbsent(trade.getMakerId(), k -> new ConcurrentSkipListSet<>()).add(trade);
    }

    public ConcurrentSkipListSet<Trade> getTradeHistory(String userId) {
        return tradeHistoryByUserMap.getOrDefault(userId, new ConcurrentSkipListSet<>());
    }

    public JsonArray getRecentTradesAsJson(String userId, int limit) {
        var userTrades = tradeHistoryByUserMap.getOrDefault(userId, new ConcurrentSkipListSet<>());
        JsonArray tradesJson = new JsonArray();

        userTrades.stream()
                .sorted((a, b) -> b.getTradedAt().compareTo(a.getTradedAt()))
                .limit(limit)
                .forEach(trade -> tradesJson.add(JsonObject.mapFrom(trade)));

        return tradesJson;
    }
}
