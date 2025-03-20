package org.valr.service;

import com.google.inject.Inject;
import org.valr.model.Balance;
import org.valr.model.Order;
import org.valr.model.enums.Currency;
import org.valr.model.enums.Side;
import org.valr.repository.BalanceRepository;

import java.math.BigDecimal;

public class BalanceService {

    private final BalanceRepository balanceRepository;

    @Inject
    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public Balance getUserBalance(String userId) {
        return balanceRepository.getBalance(userId);
    }

    public void add(String userId, Currency currency, BigDecimal amount) {
        Balance balance = getUserBalance(userId);
        balance.getCurrentBalances().compute(currency, (key, value) ->
                value == null ? amount : value.add(amount)
        );
    }

    public void subtract(String userId, Currency currency, BigDecimal amount) {
        Balance balance = getUserBalance(userId);
        balance.getCurrentBalances().compute(currency, (key, value) -> {
            if (value == null) {
                return BigDecimal.ZERO.subtract(amount);
            }
            return value.subtract(amount);
        });
    }

    public boolean reserve(String userId, Currency currency, BigDecimal amount) {
        Balance balance = balanceRepository.getBalance(userId);
        if (balance.hasSufficientBalance(currency, amount)) {
            BigDecimal newCurrentBalance = balance.getCurrentBalances().get(currency).subtract(amount);
            balance.getCurrentBalances().put(currency, newCurrentBalance);

            BigDecimal newReserveBalance = balance.getReserveBalances().get(currency).add(amount);
            balance.getReserveBalances().put(currency, newReserveBalance);
            return true;
        }
        return false;
    }

    public boolean unreserve(String userId, Currency currency, BigDecimal amount) {
        Balance balance = balanceRepository.getBalance(userId);
        if (balance.hasSufficientReserveBalance(currency, amount)) {
            BigDecimal newReserveBalance = balance.getReserveBalances().get(currency).subtract(amount);
            balance.getReserveBalances().put(currency, newReserveBalance);

            BigDecimal newCurrentBalance = balance.getCurrentBalances().get(currency).add(amount);
            balance.getCurrentBalances().put(currency, newCurrentBalance);
            return true;
        }
        return false;
    }

    public boolean reserveOnOrder(Order order) {
        boolean isReserved;
        if (order.getSide() == Side.BUY) {
            // BUY 0.5 BTC -> when 1 BTC = 50000 -> ZAR 25000 purchase price
            // Reserve 25000 ZAR
            isReserved = reserve(order.getUserId(), order.getExchangePair().getQuote(), order.getQuantity().multiply(order.getPrice()));
        } else {
            // SELL 0.5 BTC -> when 1 BTC = 50000 -> ZAR 25000 selling price
            // Reserve 0.5 BTC
            isReserved = reserve(order.getOrderId(), order.getExchangePair().getBase(), order.getQuantity());
        }
        return isReserved;
    }

    public boolean unreserveOnOrder(Order order) {
        boolean isUnreserved;
        if (order.getSide() == Side.BUY) {
            // BUY 0.5 BTC -> when 1 BTC = 50000 -> ZAR 25000 purchase price
            // Reserve 25000 ZAR
            isUnreserved = unreserve(order.getUserId(), order.getExchangePair().getQuote(), order.getQuantity().multiply(order.getPrice()));
        } else {
            // SELL 0.5 BTC -> when 1 BTC = 50000 -> ZAR 25000 selling price
            // Reserve 0.5 BTC
            isUnreserved = unreserve(order.getUserId(), order.getExchangePair().getBase(), order.getQuantity());
        }
        return isUnreserved;
    }
}