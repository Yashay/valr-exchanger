package org.valr.repository;

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
}
