package org.valr.service;

import com.google.inject.Inject;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.repository.OrderBookRepository;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentSkipListSet;

public class PlacementService {

    private final OrderBookRepository orderBookRepository;
    private final BalanceService balanceService;
    private final TradeService tradeService;
    private final MatchingService matchingService;

    @Inject
    public PlacementService(OrderBookRepository orderBookRepository, TradeService tradeService, BalanceService balanceService, MatchingService matchingService) {
        this.orderBookRepository = orderBookRepository;
        this.tradeService = tradeService;
        this.balanceService = balanceService;
        this.matchingService = matchingService;
    }

    public JsonObject placeLimitOrder(Order order) {
        boolean isReserved = balanceService.reserveOnOrder(order);
        if (!isReserved) {
            return formatOrderConfirmation(order.getOrderId(), isReserved);
        }

        Pair<ConcurrentSkipListSet<Pool>, Boolean> validPartialOrImmediatePools = matchingService.getPoolsForPartialOrImmediateMatch(order);
        ConcurrentSkipListSet<Pool> validPools = validPartialOrImmediatePools.getLeft();
        Boolean isVolumeCompletelyFillable = validPartialOrImmediatePools.getRight();

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

    private JsonObject formatOrderConfirmation(String orderId, Boolean isPlaced) {
        JsonObject orderConfirmation = new JsonObject();
        orderConfirmation.put("orderId", orderId);
        orderConfirmation.put("placed", isPlaced);
        orderConfirmation.put("message", "Insufficient funds");
        return orderConfirmation;
    }
}
