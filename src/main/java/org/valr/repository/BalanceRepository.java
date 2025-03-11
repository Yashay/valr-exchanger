package org.valr.repository;

import org.valr.model.Balance;
import org.valr.service.BalanceService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BalanceRepository {
    private Map<String, Balance> userBalances = new ConcurrentHashMap<>();

    public BalanceRepository() {}

    public Balance getBalance(String userId) {
        return userBalances.compute(userId, (key, value) -> {
            if(!userBalances.containsKey(key)) {
                value = new Balance(key);
            }
            return value;
        });
    }
}