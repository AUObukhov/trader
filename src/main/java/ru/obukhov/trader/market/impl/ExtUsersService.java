package ru.obukhov.trader.market.impl;

import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.core.UsersService;

import java.util.List;

/**
 * Service to get information about customer accounts
 */
public class ExtUsersService {

    private final UsersService usersService;

    public ExtUsersService(final UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * @return list of current customer accounts. Current customer is defined by token passed to Tinkoff.
     */
    public List<Account> getAccounts() {
        return usersService.getAccountsSync();
    }

}