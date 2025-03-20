package org.valr.registry;

import com.google.inject.AbstractModule;
import org.valr.repository.BalanceRepository;
import org.valr.repository.OrderBookRepository;
import org.valr.repository.TradeRepository;
import org.valr.repository.UserRepository;
import org.valr.service.*;

public class DependencyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UserRepository.class).toInstance(new UserRepository());
        bind(UserService.class);

        bind(BalanceRepository.class).toInstance(new BalanceRepository());
        bind(BalanceService.class);

        bind(OrderBookRepository.class).toInstance(new OrderBookRepository());
        bind(OrderBookService.class);

        bind(TradeRepository.class).toInstance(new TradeRepository());
        bind(TradeService.class);

        bind(MatchingService.class);
    }
}
