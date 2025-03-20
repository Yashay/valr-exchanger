package org.valr.simulator;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;

import java.math.BigDecimal;
import java.util.Random;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SimulatedOrder {
    private Side side;
    private ExchangePair exchangePair;
    private BigDecimal price;
    private BigDecimal quantity;

    public SimulatedOrder(Random random) {
        /**
         * Sell order: I want to sell 0.5 BTC at 50k
         * This will match against buyers willing to pay 50k for 0.5 BTC
         * What you get is: 50k zar
         * What the seller gets: 0.5 BTC
         * ---
         * Buy order: I want to buy 0.5 BTC at 50k
         * This will match against sellers willing to sell 0.5 BTC at 50k
         * What you get is: 0.5 BTC
         * What seller gets is: 50k zar
         */
        exchangePair = ExchangePair.BTCZAR;
        side = random.nextBoolean() ? Side.BUY : Side.SELL;
        price = BigDecimal.valueOf(random.nextInt(4990, 5000)); // 50000 - 51000
        quantity = BigDecimal.valueOf(random.nextInt(1, 100)); // 0.1 - 0.6 BTC
    }

    public JsonObject sellOrder() {
        exchangePair = ExchangePair.BTCZAR;
        side = Side.SELL;
        price = BigDecimal.valueOf(2500); // 50000 - 51000
        quantity = BigDecimal.valueOf(5); // 0.1 - 0.6 BTC
        return this.getOrderPayload();
    }

    public JsonObject buyOrder() {
        exchangePair = ExchangePair.BTCZAR;
        side = Side.BUY;
        price = BigDecimal.valueOf(10000); // 50000 - 51000
        quantity = BigDecimal.valueOf(10); // 0.1 - 0.6 BTC
        return this.getOrderPayload();
    }

    public JsonObject getOrderPayload() {
        return new JsonObject()
                .put("pair", exchangePair)
                .put("side", side)
                .put("price", price)
                .put("quantity", quantity);
    }
}
