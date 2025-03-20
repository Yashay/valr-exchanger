package org.valr.service;

import com.google.inject.Inject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.valr.dto.PoolDTO;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.enums.TimeInForce;
import org.valr.repository.OrderBookRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

public class OrderBookService {
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
        if (isReserved) {
            Pair<ConcurrentSkipListSet<Pool>, Boolean> validPartialOrImmediatePools = matchingService.getPoolsMatchingVolumeRequestPartialOrImmediateFill(order);
            ConcurrentSkipListSet<Pool> validPools = validPartialOrImmediatePools.getLeft();
            Boolean isVolumeCompletelyFillable = validPartialOrImmediatePools.getRight();

            orderBookRepository.addOrder(order);

            if (order.getTimeInForce() == TimeInForce.FOK && isVolumeCompletelyFillable) {
                // will fill and auto remove
                tradeService.executeMatches(order, validPools);
            } else if (order.getTimeInForce() == TimeInForce.FOK) {
                // can't fill remove
                orderBookRepository.removeOrder(order);
            } else if (order.getTimeInForce() == TimeInForce.IOC) {
                // will remain in book need to manually remove & unreserve balances
                tradeService.executeMatches(order, validPools);
                orderBookRepository.removeOrder(order);
                balanceService.unreserveOnOrder(order);
            } else {
                // (order.getTimeInForce() == TimeInForce.GTC)
                // if filled will auto remove
                tradeService.executeMatches(order, validPools);
            }
        } else {
            return formatOrderConfirmation(order.getOrderId(), isReserved);
        }
        return new JsonObject();
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
        int limit = 40;
        JsonArray topOrders = new JsonArray();
        for (Map.Entry<BigDecimal, Pool> entry : poolMap.entrySet()) {
            Pool pool = entry.getValue();
            topOrders.add(PoolDTO.from(pool).toJson());
            if (limit < 1) break;
            limit--;
        }
        return topOrders;
    }

    private JsonObject formatOrderConfirmation(String orderId, Boolean isPlaced) {
        JsonObject orderConfirmation = new JsonObject();
        orderConfirmation.put("orderId", orderId);
        orderConfirmation.put("code", -6);
        orderConfirmation.put("placed", isPlaced);
        orderConfirmation.put("message", "Insufficient funds");
        return orderConfirmation;
    }
}