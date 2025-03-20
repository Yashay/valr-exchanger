package org.valr.repository;

import org.valr.model.Balance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BalanceRepository {
    private final Map<String, Balance> userBalances = new ConcurrentHashMap<>();

    public BalanceRepository() {}

    public Balance getBalance(String userId) {
        return userBalances.computeIfAbsent(userId, Balance::new);
    }
}