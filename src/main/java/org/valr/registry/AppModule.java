package org.valr.registry;

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
import org.valr.verticles.BalanceVerticle;
import org.valr.verticles.OrderBookVerticle;
import org.valr.verticles.TradeVerticle;
import org.valr.verticles.UserVerticle;

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
    public UserVerticle provideUserVerticle(Router router, AuthMiddleware authMiddleware, UserService userService) {
        return new UserVerticle(router, authMiddleware, userService);
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
    public OrderBookService provideOrderBookRepository(OrderBookRepository orderBookRepository, TradeService tradeService, BalanceService balanceService, MatchingService matchingService) {
        return new OrderBookService(orderBookRepository, tradeService, balanceService, matchingService);  
    }

    @Provides
    @Singleton
    public OrderBookVerticle provideOrderBookVerticle(Router router, AuthMiddleware authMiddleware, OrderBookService orderBookService) {
        return new OrderBookVerticle(router, authMiddleware, orderBookService);
    }
}