package org.valr.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.middleware.AuthMiddleware;
import org.valr.middleware.ValidationMiddleware;
import org.valr.model.Order;
import org.valr.service.BalanceService;
import org.valr.service.OrderBookService;

public class OrderBookVerticle extends AbstractVerticle {
    private final OrderBookService orderBookService;
    private final BalanceService balanceService;
    private final AuthMiddleware authMiddleware;
    private final ObjectMapper objectMapper;


    @Inject
    public OrderBookVerticle(Router router, ObjectMapper mapper, AuthMiddleware authMiddleware, OrderBookService orderBookService, BalanceService balanceService) {
        this.objectMapper = mapper;
        this.authMiddleware = authMiddleware;
        this.orderBookService = orderBookService;
        this.balanceService = balanceService;
        setupRoutes(router);
    }

    private void setupRoutes(Router router) {
        router.get("/api/orderbook")
                .handler(this::getOrderBookSnapshot);
        router.post("/api/orders/limit")
                .handler(authMiddleware::authenticate)
                .handler(new ValidationMiddleware<>(Order.class)::validate)
                .handler(this::placeLimitOrderInQueue);
    }

    private void placeLimitOrderInQueue(RoutingContext context) {
        try {
            String userId = authMiddleware.getUserIdFromContext(context);
            Order order = context.get(Order.class.getSimpleName());
            order.setUserId(userId);

            boolean isReserved = balanceService.reserveOnOrder(order);

            if (isReserved) {
                byte[] orderBin = objectMapper.writeValueAsBytes(order);
                vertx.eventBus().request("order.queue", orderBin);
                context.response().setStatusCode(201).end("Created");
            } else {
                JsonObject orderInformation = formatOrderConfirmation(order.getOrderId(), isReserved);
                context.response().setStatusCode(400).end(orderInformation.encodePrettily());
            }
        } catch (Exception e) {
            context.response().setStatusCode(400).end("Failure");
            e.printStackTrace();
        }
    }

    private JsonObject formatOrderConfirmation(String orderId, Boolean isPlaced) {
        JsonObject orderConfirmation = new JsonObject();
        orderConfirmation.put("orderId", orderId);
        orderConfirmation.put("placed", isPlaced);
        orderConfirmation.put("message", "Insufficient funds");
        return orderConfirmation;
    }

    private void getOrderBookSnapshot(RoutingContext context) {
        JsonObject orderBookSnapshot = orderBookService.getOrderBookSnapshot();
        context.response().setStatusCode(201).end(orderBookSnapshot.encodePrettily());
    }
}