package org.valr.service;

import com.google.inject.Inject;
import org.valr.model.Balance;
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

    public void addFiat(String userId, BigDecimal amount) {
        Balance balance = getUserBalance(userId);
        balance.setFiatBalance(balance.getFiatBalance().add(amount));
    }

    public void addCrypto(String userId, BigDecimal amount) {
        Balance balance = getUserBalance(userId);
        balance.setCryptoBalance(balance.getCryptoBalance().add(amount));
    }


    public boolean deductFiat(String userId, BigDecimal amount) {
        Balance balance = balanceRepository.getBalance(userId);
        if (balance.getFiatBalance().compareTo(amount) >= 0) {
            balance.setFiatBalance(balance.getFiatBalance().subtract(amount));
            return true;
        }
        return false;
    }

    public boolean deductCrypto(String userId, BigDecimal amount) {
        Balance balance = balanceRepository.getBalance(userId);
        if (balance.getCryptoBalance().compareTo(amount) >= 0) {
            balance.setCryptoBalance(balance.getCryptoBalance().subtract(amount));
            return true;
        }
        return false;
    }
}