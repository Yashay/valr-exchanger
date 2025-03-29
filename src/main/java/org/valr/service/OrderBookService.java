package org.valr.service;

import com.google.inject.Inject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.valr.dto.PoolDTO;
import org.valr.model.Pool;
import org.valr.repository.OrderBookRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class OrderBookService {
    private static final int ORDER_BOOK_DEPTH_LIMIT = 40;

    private final OrderBookRepository orderBookRepository;

    @Inject
    public OrderBookService(OrderBookRepository orderBookRepository) {
        this.orderBookRepository = orderBookRepository;
    }

    public JsonObject getOrderBookSnapshot() {
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
}