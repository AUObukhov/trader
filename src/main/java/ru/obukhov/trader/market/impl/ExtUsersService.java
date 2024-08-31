package ru.obukhov.trader.market.impl;


import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.market.model.Account;
import ru.obukhov.trader.market.model.transform.AccountMapper;
import ru.tinkoff.piapi.core.UsersService;

import java.util.List;

@Service
@AllArgsConstructor
public class ExtUsersService {

    private static final AccountMapper ACCOUNT_MAPPER = Mappers.getMapper(AccountMapper.class);
    private final UsersService usersService;

    public List<Account> getAccounts() {
        return usersService.getAccountsSync().stream()
                .map(ACCOUNT_MAPPER::map)
                .toList();
    }

}