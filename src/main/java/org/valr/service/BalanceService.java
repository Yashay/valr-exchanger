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
            isReserved = reserve(order.getUserId(), order.getExchangePair().getQuote(), order.getQuantity().multiply(order.getPrice()));
        } else {
            isReserved = reserve(order.getUserId(), order.getExchangePair().getBase(), order.getQuantity());
        }
        return isReserved;
    }

    public boolean unreserveOnOrder(Order order) {
        boolean isUnreserved;
        if (order.getSide() == Side.BUY) {
            isUnreserved = unreserve(order.getUserId(), order.getExchangePair().getQuote(), order.getQuantity().multiply(order.getPrice()));
        } else {
            isUnreserved = unreserve(order.getUserId(), order.getExchangePair().getBase(), order.getQuantity());
        }
        return isUnreserved;
    }

    public void adjustBalancesForTrade(Order takerOrder, Order makerOrder, BigDecimal tradedQuantity) {
        BigDecimal takerTotalCost = tradedQuantity.multiply(takerOrder.getPrice());
        BigDecimal makerTotalCost = tradedQuantity.multiply(makerOrder.getPrice());

        if (takerOrder.getSide() == Side.BUY) {
            processBuySideTradeBalances(takerOrder, makerOrder, tradedQuantity, takerTotalCost, makerTotalCost);
        } else {
            processSellSideTradeBalances(takerOrder, makerOrder, tradedQuantity, makerTotalCost);
        }
    }

    private void processBuySideTradeBalances(Order takerOrder, Order makerOrder,
                                             BigDecimal tradedQuantity, BigDecimal takerTotalCost,
                                             BigDecimal makerTotalCost) {
        unreserve(takerOrder.getUserId(), takerOrder.getExchangePair().getQuote(), takerTotalCost);
        subtract(takerOrder.getUserId(), takerOrder.getExchangePair().getQuote(), makerTotalCost);

        unreserve(makerOrder.getUserId(), makerOrder.getExchangePair().getBase(), tradedQuantity);
        subtract(makerOrder.getUserId(), makerOrder.getExchangePair().getBase(), tradedQuantity);

        add(takerOrder.getUserId(), takerOrder.getExchangePair().getBase(), tradedQuantity);
        add(makerOrder.getUserId(), makerOrder.getExchangePair().getQuote(), makerTotalCost);
    }

    private void processSellSideTradeBalances(Order takerOrder, Order makerOrder,
                                              BigDecimal tradedQuantity, BigDecimal makerTotalCost) {
        unreserve(takerOrder.getUserId(), takerOrder.getExchangePair().getBase(), tradedQuantity);
        subtract(takerOrder.getUserId(), takerOrder.getExchangePair().getBase(), tradedQuantity);

        unreserve(makerOrder.getUserId(), makerOrder.getExchangePair().getQuote(), makerTotalCost);
        subtract(makerOrder.getUserId(), makerOrder.getExchangePair().getQuote(), makerTotalCost);

        add(takerOrder.getUserId(), takerOrder.getExchangePair().getQuote(), makerTotalCost);
        add(makerOrder.getUserId(), makerOrder.getExchangePair().getBase(), tradedQuantity);
    }
}