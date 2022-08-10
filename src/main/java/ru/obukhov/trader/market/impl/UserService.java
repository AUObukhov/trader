package ru.obukhov.trader.market.impl;

import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.UserAccount;

import java.util.List;

/**
 * Service to get information about customer accounts
 */
public class UserService {

    private final TinkoffService tinkoffService;

    public UserService(final TinkoffService tinkoffService) {
        this.tinkoffService = tinkoffService;
    }

    /**
     * @return list of current customer accounts. Current customer is defined by token passed to Tinkoff.
     */
    public List<UserAccount> getAccounts() {
        return tinkoffService.getAccounts();
    }

}