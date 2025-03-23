package org.valr.simulator;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;

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
    private TimeInForce timeInForce;

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
        price = BigDecimal.valueOf(random.nextInt(500, 5000));
        quantity = BigDecimal.valueOf(random.nextInt(1, 100));
        timeInForce = TimeInForce.GTC;
    }

    public JsonObject sellOrder() {
        exchangePair = ExchangePair.BTCZAR;
        side = Side.SELL;
        return this.getOrderPayload();
    }

    public JsonObject buyOrder() {
        exchangePair = ExchangePair.BTCZAR;
        side = Side.BUY;
        return this.getOrderPayload();
    }

    public JsonObject getOrderPayload() {
        return new JsonObject()
                .put("exchangePair", exchangePair)
                .put("side", side)
                .put("price", price)
                .put("timeInForce", timeInForce)
                .put("quantity", quantity);
    }
}
