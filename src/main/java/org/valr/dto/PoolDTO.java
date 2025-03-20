package org.valr.dto;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.valr.model.Pool;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PoolDTO {
    private Side side;
    private BigDecimal price;
    private BigDecimal quantity;
    private ExchangePair currencyPair;
    private int orderCount;

    public static PoolDTO from(Pool pool) {
        return new PoolDTO(
                pool.getSide(),
                pool.getPrice(),
                pool.getVolume().get(),
                pool.getCurrencyPair(),
                pool.getOrders().size()
        );
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("side", side)
                .put("price", price.toPlainString())
                .put("quantity", quantity.toPlainString())
                .put("currencyPair", currencyPair.name())
                .put("orderCount", orderCount);
    }
}
