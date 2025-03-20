package org.valr.service;

import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.enums.Side;
import org.valr.repository.OrderBookRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

// [TODO] Add trade service, add test for one branch, add code for canceling GTC, FOK
public class MatchingService {
    private final OrderBookRepository orderBookRepository;
    private final TradeService tradeService;
    private final BalanceService balanceService;

    @Inject
    public MatchingService(OrderBookRepository orderBookRepository, TradeService tradeService, BalanceService balanceService) {
        this.balanceService = balanceService;
        this.orderBookRepository = orderBookRepository;
        this.tradeService = tradeService;
    }

    // PUBLIC for testing purposes
    public Pair<ConcurrentSkipListSet<Pool>, Boolean> getPoolsMatchingVolumeRequestPartialOrImmediateFill(Order takerOrder) {
        ConcurrentSkipListSet<Pool> pools = new ConcurrentSkipListSet<>();
        BigDecimal volumeRequired = takerOrder.getQuantity();
        for (Map.Entry<BigDecimal, Pool> entry : orderBookRepository.getPoolsOppositeSide(takerOrder.getSide()).entrySet()) {
            BigDecimal price = entry.getKey();
            Pool pool = entry.getValue();
            // if the cheapest sell order is greater than buy order break;
            if ((volumeRequired.compareTo(BigDecimal.ZERO) <= 0) ||
                (Side.BUY == takerOrder.getSide() && takerOrder.getPrice().compareTo(price) < 0) ||
                (Side.SELL == takerOrder.getSide() && takerOrder.getPrice().compareTo(price) > 0)) {
                break;
            }
            volumeRequired = volumeRequired.subtract(pool.getVolume().get());
            pools.add(pool);
        }
        Boolean isCompletelyFillable = false;
        if (volumeRequired.compareTo(BigDecimal.ZERO) <= 0) {
            isCompletelyFillable = true;
        }
        //TODO To execute FOK we need to know if the entire order can be filled immediately
        return Pair.of(pools, isCompletelyFillable);
    }

    public void executeMatches(Order takerOrder, ConcurrentSkipListSet<Pool> validVolumePools) {
        for (Pool currentPool : validVolumePools) {
            for (Order makerOrder : currentPool.getOrders()) {
                if (takerOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) break;

                BigDecimal tradableQuantity = takerOrder.getQuantity().min(makerOrder.getQuantity());

                handleBalances(takerOrder, makerOrder, tradableQuantity);
                updateOrderQuantities(takerOrder, makerOrder, tradableQuantity);
            }
        }
    }

    private void handleBalances(Order takerOrder, Order makerOrder, BigDecimal tradableQuantity) {
        BigDecimal takerTotalPrice = tradableQuantity.multiply(takerOrder.getPrice());
        BigDecimal makerTotalPrice = tradableQuantity.multiply(makerOrder.getPrice());

        if (takerOrder.getSide() == Side.BUY) {
            balanceService.unreserve(takerOrder.getUserId(), takerOrder.getExchangePair().getQuote(), takerTotalPrice);
            balanceService.subtract(takerOrder.getUserId(), takerOrder.getExchangePair().getQuote(), makerTotalPrice);

            balanceService.unreserve(makerOrder.getUserId(), makerOrder.getExchangePair().getBase(), tradableQuantity);
            balanceService.subtract(makerOrder.getUserId(), makerOrder.getExchangePair().getBase(), tradableQuantity);

            balanceService.add(takerOrder.getUserId(), takerOrder.getExchangePair().getBase(), tradableQuantity);
            balanceService.add(makerOrder.getUserId(), makerOrder.getExchangePair().getQuote(), makerTotalPrice);

        } else { // Side.SELL
            balanceService.unreserve(takerOrder.getUserId(), takerOrder.getExchangePair().getBase(), tradableQuantity);
            balanceService.subtract(takerOrder.getUserId(), takerOrder.getExchangePair().getBase(), tradableQuantity);

            balanceService.unreserve(makerOrder.getUserId(), makerOrder.getExchangePair().getQuote(), makerTotalPrice);
            balanceService.subtract(makerOrder.getUserId(), makerOrder.getExchangePair().getQuote(), makerTotalPrice);

            balanceService.add(takerOrder.getUserId(), takerOrder.getExchangePair().getQuote(), makerTotalPrice);
            balanceService.add(makerOrder.getUserId(), makerOrder.getExchangePair().getBase(), tradableQuantity);
        }
    }

    private void updateOrderQuantities(Order takerOrder, Order makerOrder, BigDecimal tradableQuantity) {
        takerOrder.reduceQuantity(tradableQuantity);
        makerOrder.reduceQuantity(tradableQuantity);

        orderBookRepository.getPoolByOrder(takerOrder)
                .ifPresent(pool -> pool.reduceQuantity(tradableQuantity));

        orderBookRepository.getPoolByOrder(makerOrder)
                .ifPresent(pool -> pool.reduceQuantity(tradableQuantity));

        removeIfZero(takerOrder);
        removeIfZero(makerOrder);
    }

    private void removeIfZero(Order order) {
        if (order.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            orderBookRepository.removeOrder(order);
        }
    }
}
