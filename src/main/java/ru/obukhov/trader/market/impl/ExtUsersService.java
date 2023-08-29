package ru.obukhov.trader.market.impl;


import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Account;
import ru.obukhov.trader.market.model.transform.AccountMapper;
import ru.tinkoff.piapi.core.UsersService;

import java.util.List;

/**
 * Service to get information about customer accounts
 */
public class ExtUsersService {

    private static final AccountMapper ACCOUNT_MAPPER = Mappers.getMapper(AccountMapper.class);
    private final UsersService usersService;

    public ExtUsersService(final UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * @return list of current customer accounts. Current customer is defined by token passed to Tinkoff.
     */
    public List<Account> getAccounts() {
        return usersService.getAccountsSync().stream()
                .map(ACCOUNT_MAPPER::map)
                .toList();
    }

}