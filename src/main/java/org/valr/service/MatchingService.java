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

    @Inject
    public MatchingService(OrderBookRepository orderBookRepository) {
        this.orderBookRepository = orderBookRepository;
    }

    // PUBLIC for testing purposes
    public Pair<ConcurrentSkipListSet<Pool>, Boolean> getPoolsForPartialOrImmediateMatch(Order takerOrder) {
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
}
