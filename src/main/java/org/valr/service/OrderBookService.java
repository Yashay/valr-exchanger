package org.valr.service;

import com.google.inject.Inject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.valr.dto.PoolDTO;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.repository.OrderBookRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

public class OrderBookService {
    private static final int ORDER_BOOK_DEPTH_LIMIT = 40;

    private final OrderBookRepository orderBookRepository;
    private final BalanceService balanceService;
    private final TradeService tradeService;
    private final MatchingService matchingService;

    @Inject
    public OrderBookService(OrderBookRepository orderBookRepository, TradeService tradeService, BalanceService balanceService, MatchingService matchingService) {
        this.orderBookRepository = orderBookRepository;
        this.tradeService = tradeService;
        this.balanceService = balanceService;
        this.matchingService = matchingService;
    }

    public JsonObject placeLimitOrder(String userId, Order order) {
        //TODO: order userId should be set in the verticle
        order.setUserId(userId);

        boolean isReserved = balanceService.reserveOnOrder(order);
        if (!isReserved) {
            return formatOrderConfirmation(order.getOrderId(), isReserved);
        }

        Pair<ConcurrentSkipListSet<Pool>, Boolean> validPartialOrImmediatePools = matchingService.getPoolsForPartialOrImmediateMatch(order);
        ConcurrentSkipListSet<Pool> validPools = validPartialOrImmediatePools.getLeft();
        Boolean isVolumeCompletelyFillable = validPartialOrImmediatePools.getRight();

//        orderBookRepository.addOrder(order);
        switch (order.getTimeInForce()) {
            case FOK -> handleFOK(order, validPools, isVolumeCompletelyFillable);
            case IOC -> handleIOC(order, validPools);
            case GTC -> handleGTC(order, validPools);
        }
        return new JsonObject();
    }

    private void handleFOK(Order order, ConcurrentSkipListSet<Pool> validPools, boolean isVolumeCompletelyFillable) {
        if (isVolumeCompletelyFillable) {
            tradeService.executeMatches(order, validPools);
        } else { // cannot be filled
            balanceService.unreserveOnOrder(order);
        }
    }

    private void handleIOC(Order order, ConcurrentSkipListSet<Pool> validPools) {
        tradeService.executeMatches(order, validPools);
        if (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) { // partially filled
            balanceService.unreserveOnOrder(order);
        }
    }

    private void handleGTC(Order order, ConcurrentSkipListSet<Pool> validPools) {
        tradeService.executeMatches(order, validPools);
        if (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            orderBookRepository.addOrder(order);
        }
    }

    public JsonObject getOrderBookSnapshot(String currencyPair) {
        JsonObject orderBookSnapshot = new JsonObject();

        JsonArray asks = buildSideSnapshot(orderBookRepository.getSellPoolMap());
        JsonArray bids = buildSideSnapshot(orderBookRepository.getBuyPoolMap());

        orderBookSnapshot.put("Asks", asks);
        orderBookSnapshot.put("Bids", bids);
        orderBookSnapshot.put("sequence", orderBookRepository.getOrderCounter().longValue());
        orderBookSnapshot.put("createdAt", Instant.now());

        return orderBookSnapshot;
    }

    private JsonArray buildSideSnapshot(Map<BigDecimal, Pool> poolMap) {
        int count = 0;
        JsonArray topOrders = new JsonArray();
        for (Map.Entry<BigDecimal, Pool> entry : poolMap.entrySet()) {
            if (count >= ORDER_BOOK_DEPTH_LIMIT) break;
            Pool pool = entry.getValue();
            topOrders.add(PoolDTO.from(pool).toJson());
            count++;
        }
        return topOrders;
    }

    private JsonObject formatOrderConfirmation(String orderId, Boolean isPlaced) {
        JsonObject orderConfirmation = new JsonObject();
        orderConfirmation.put("orderId", orderId);
        orderConfirmation.put("placed", isPlaced);
        orderConfirmation.put("message", "Insufficient funds");
        return orderConfirmation;
    }
}