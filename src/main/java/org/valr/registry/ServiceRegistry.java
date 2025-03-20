package org.valr.registry;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.valr.service.*;

public class ServiceRegistry {
    private static final Injector injector = Guice.createInjector(new DependencyModule());

    public static UserService getUserService() {
        return injector.getInstance(UserService.class);
    }

    public static BalanceService getBalanceService() {
        return injector.getInstance(BalanceService.class);
    }

    public static OrderBookService getOrderBookService() {
        return injector.getInstance(OrderBookService.class);
    }

    public static TradeService getTradeService() {
        return injector.getInstance(TradeService.class);
    }

    public static MatchingService getMatchingService() {
        return injector.getInstance(MatchingService.class);
    }
}
