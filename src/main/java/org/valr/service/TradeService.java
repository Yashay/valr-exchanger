package org.valr.service;

import com.google.inject.Inject;
import io.vertx.core.json.JsonArray;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.Trade;
import org.valr.model.enums.Side;
import org.valr.repository.OrderBookRepository;
import org.valr.repository.TradeRepository;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentSkipListSet;

public class TradeService {
    private final BalanceService balanceService;
    private final OrderBookRepository orderBookRepository;
    private final TradeRepository tradeRepository;

    @Inject
    public TradeService(BalanceService balanceService, OrderBookRepository orderBookRepository, TradeRepository tradeRepository) {
        this.balanceService = balanceService;
        this.orderBookRepository = orderBookRepository;
        this.tradeRepository = tradeRepository;
    }

    public void executeMatches(Order takerOrder, ConcurrentSkipListSet<Pool> validVolumePools) {
        for (Pool pool : validVolumePools) {
            for (Order makerOrder : pool.getOrders()) {
                if (takerOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) break;

                BigDecimal tradableQuantity = takerOrder.getQuantity().min(makerOrder.getQuantity());

                balanceService.adjustBalancesForTrade(takerOrder, makerOrder, tradableQuantity);
                updateOrderQuantities(takerOrder, makerOrder, tradableQuantity);

                removeIfZero(takerOrder);
                removeIfZero(makerOrder);

                recordTrade(takerOrder, makerOrder, tradableQuantity);
            }
        }
    }

    public void updateOrderQuantities(Order takerOrder, Order makerOrder, BigDecimal tradableQuantity) {
        takerOrder.reduceQuantity(tradableQuantity);
        makerOrder.reduceQuantity(tradableQuantity);

        orderBookRepository.getPoolByOrder(takerOrder).ifPresent(pool -> pool.reduceQuantity(tradableQuantity));
        orderBookRepository.getPoolByOrder(makerOrder).ifPresent(pool -> pool.reduceQuantity(tradableQuantity));

    }

    // public for testing purposes
    public void removeIfZero(Order order) {
        if (order.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            orderBookRepository.removeOrder(order);
        }
    }

    public void recordTrade(Order takerOrder, Order makerOrder, BigDecimal tradableQuantity) {
        BigDecimal tradablePrice;
        if (takerOrder.getSide() == Side.BUY) {
            tradablePrice = makerOrder.getPrice();
        } else {
            tradablePrice = takerOrder.getPrice();
        }
        Trade trade = new Trade(takerOrder, makerOrder, tradablePrice, tradableQuantity);
        tradeRepository.addTrade(trade);
    }

    public JsonArray getRecentTradeHistory(String userId) {
        return getRecentTradesHistory(userId, 100);
    }

    public JsonArray getRecentTradesHistory(String userId, int limit) {
        ConcurrentSkipListSet<Trade> userTrades = tradeRepository.getTradeHistory(userId);
        JsonArray tradesJson = new JsonArray();

        userTrades.stream()
                .sorted((a, b) -> b.getTradedAt().compareTo(a.getTradedAt()))
                .limit(limit)
                .forEach(trade -> tradesJson.add(trade.toJson()));

        return tradesJson;
    }
}