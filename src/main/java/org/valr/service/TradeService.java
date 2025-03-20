package org.valr.service;

import com.google.inject.Inject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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

                handleBalances(takerOrder, makerOrder, tradableQuantity);
                updateOrderQuantities(takerOrder, makerOrder, tradableQuantity);

                removeIfZero(takerOrder);
                removeIfZero(makerOrder);

                recordTrade(takerOrder, makerOrder, tradableQuantity);
            }
        }
    }

    private void handleBalances(Order takerOrder, Order makerOrder, BigDecimal tradableQuantity) {
        BigDecimal takerTotalPrice = tradableQuantity.multiply(takerOrder.getPrice());
        BigDecimal makerTotalPrice = tradableQuantity.multiply(makerOrder.getPrice());

        if (takerOrder.getSide() == Side.BUY) {
            processBuySideBalances(takerOrder, makerOrder, tradableQuantity, takerTotalPrice, makerTotalPrice);
        } else {
            processSellSideBalances(takerOrder, makerOrder, tradableQuantity, makerTotalPrice);
        }
    }

    private void processBuySideBalances(Order takerOrder, Order makerOrder,
                                        BigDecimal tradableQuantity, BigDecimal takerTotalPrice,
                                        BigDecimal makerTotalPrice) {
        balanceService.unreserve(takerOrder.getUserId(), takerOrder.getExchangePair().getQuote(), takerTotalPrice);
        balanceService.subtract(takerOrder.getUserId(), takerOrder.getExchangePair().getQuote(), makerTotalPrice);

        balanceService.unreserve(makerOrder.getUserId(), makerOrder.getExchangePair().getBase(), tradableQuantity);
        balanceService.subtract(makerOrder.getUserId(), makerOrder.getExchangePair().getBase(), tradableQuantity);

        balanceService.add(takerOrder.getUserId(), takerOrder.getExchangePair().getBase(), tradableQuantity);
        balanceService.add(makerOrder.getUserId(), makerOrder.getExchangePair().getQuote(), makerTotalPrice);
    }

    private void processSellSideBalances(Order takerOrder, Order makerOrder,
                                         BigDecimal tradableQuantity, BigDecimal makerTotalPrice) {
        balanceService.unreserve(takerOrder.getUserId(), takerOrder.getExchangePair().getBase(), tradableQuantity);
        balanceService.subtract(takerOrder.getUserId(), takerOrder.getExchangePair().getBase(), tradableQuantity);

        balanceService.unreserve(makerOrder.getUserId(), makerOrder.getExchangePair().getQuote(), makerTotalPrice);
        balanceService.subtract(makerOrder.getUserId(), makerOrder.getExchangePair().getQuote(), makerTotalPrice);

        balanceService.add(takerOrder.getUserId(), takerOrder.getExchangePair().getQuote(), makerTotalPrice);
        balanceService.add(makerOrder.getUserId(), makerOrder.getExchangePair().getBase(), tradableQuantity);
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
                .forEach(trade -> tradesJson.add(formatTradeRecord(trade)));

        return tradesJson;
    }

    public JsonObject formatTradeRecord(Trade trade) {
        JsonObject tradeJson = new JsonObject();
        tradeJson.put("price", trade.getPrice());
        tradeJson.put("quantity", trade.getQuantity());
        tradeJson.put("currencyPair", trade.getCurrenyExchangePair());
        tradeJson.put("tradedAt", trade.getTradedAt());
        tradeJson.put("takerSide", trade.getTakerSide());
        tradeJson.put("id", trade.getTradeId());
        tradeJson.put("quoteVolume", trade.getQuoteVolume());
        return tradeJson;
    }
}