package org.valr.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.valr.auth.JwtAuthProvider;
import org.valr.middleware.AuthMiddleware;
import org.valr.repository.BalanceRepository;
import org.valr.repository.OrderBookRepository;
import org.valr.repository.TradeRepository;
import org.valr.repository.UserRepository;
import org.valr.service.*;
import org.valr.verticles.*;

public class AppModule extends AbstractModule {

    @Provides
    @Singleton
    public Vertx provideVertx() {
        return Vertx.vertx();
    }

    @Provides
    @Singleton
    public Router provideRouter(Vertx vertx) {
        return Router.router(vertx);
    }

    @Provides
    @Singleton
    public JwtAuthProvider provideJWTAuth(Vertx vertx) {
        return new JwtAuthProvider(vertx);  
    }

    @Provides
    @Singleton
    public AuthMiddleware provideAuthMiddleware(JwtAuthProvider jwtAuthProvider) {
        return new AuthMiddleware(jwtAuthProvider);  
    }

    @Provides
    @Singleton
    public UserRepository provideUserRepository() {
        return new UserRepository();  
    }

    @Provides
    @Singleton
    public UserService provideUserService(UserRepository userRepository) {
        return new UserService(userRepository);  
    }

    @Provides
    @Singleton
    public UserVerticle provideUserVerticle(Router router, JwtAuthProvider jwtAuthProvider, UserService userService) {
        return new UserVerticle(router, jwtAuthProvider, userService);
    }

    @Provides
    @Singleton
    public BalanceRepository provideBalanceRepository() {
        return new BalanceRepository();  
    }

    @Provides
    @Singleton
    public BalanceService provideBalanceService(BalanceRepository balanceRepository) {
        return new BalanceService(balanceRepository);  
    }

    @Provides
    @Singleton
    public BalanceVerticle provideBalanceVerticle(Router router, AuthMiddleware authMiddleware, BalanceService balanceService) {
        return new BalanceVerticle(router, authMiddleware, balanceService);
    }

    @Provides
    @Singleton
    public TradeRepository provideTradeRepository() {
        return new TradeRepository();  
    }

    @Provides
    @Singleton
    public TradeService provideTradeService(BalanceService balanceService, OrderBookRepository orderBookRepository, TradeRepository tradeRepository) {
        return new TradeService(balanceService, orderBookRepository, tradeRepository);  
    }

    @Provides
    @Singleton
    public TradeVerticle provideTradeVerticle(Router router, AuthMiddleware authMiddleware, TradeService tradeService) {
        return new TradeVerticle(router, authMiddleware, tradeService);
    }

    @Provides
    @Singleton
    public MatchingService provideMatchingService(OrderBookRepository orderBookRepository) {
        return new MatchingService(orderBookRepository);  
    }

    @Provides
    @Singleton
    public OrderBookRepository provideOrderBookRepository() {
        return new OrderBookRepository();  
    }

    @Provides
    @Singleton
    public OrderBookService provideOrderBookService(OrderBookRepository orderBookRepository) {
        return new OrderBookService(orderBookRepository);
    }

    @Provides
    @Singleton
    public OrderBookVerticle provideOrderBookVerticle(Router router, OrderBookService orderBookService) {
        return new OrderBookVerticle(router, orderBookService);
    }

    @Provides
    @Singleton
    public PlacementService providePlacementService(OrderBookRepository orderBookRepository, TradeService tradeService, BalanceService balanceService, MatchingService matchingService) {
        return new PlacementService(orderBookRepository, tradeService, balanceService, matchingService);
    }

    @Provides
    @Singleton
    public PlacementVerticle providePlacementVerticle(Router router, AuthMiddleware authMiddleware, PlacementService placementService) {
        return new PlacementVerticle(router, authMiddleware, placementService);
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}